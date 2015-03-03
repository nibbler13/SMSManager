package com.nibbler.email2sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.telephony.SmsManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Created by NIBBLER on 16/02/15.
 */
public class BackgroundEmailCheck extends Service {

    private NotificationManager mNM;
    private int NOTIFICATION = 76667;
    private ScheduledFuture<?> scheduledFuture;
    private ScheduledFuture<?> scheduledFutureLogFile;
    private SharedPreferences sharedPreferences;
    private LogFile logFile;

    //general counters
    private int totalSmsCounter;
    private int totalMessagesCounter;
    private boolean configIsCorrect;
    private MailSystem mailSystem;

    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        Log.d("nibbler", "BackgroundEmailCheck onCreate");
        logFile = new LogFile(this);
        logFile.writeToLog("Сервис создан");
        mailSystem = new MailSystem(this);

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);

        String pop3ServerName = sharedPreferences.getString(getString(R.string.pop3ServerName), "");
        int pop3ServerPort = sharedPreferences.getInt(getString(R.string.pop3ServerPort), 0);
        boolean sendLogViaMail = sharedPreferences.getBoolean(getString(R.string.sendLogViaMail), true);
        String smtpServerName = sharedPreferences.getString(getString(R.string.smtpServerName), "");
        int smtpServerPort = sharedPreferences.getInt(getString(R.string.smtpServerPort), 0);
        boolean smtpAuthentication = sharedPreferences.getBoolean(getString(R.string.smtpAuthentication), true);
        String pop3Login = sharedPreferences.getString(getString(R.string.pop3Login), "");
        String pop3Password = sharedPreferences.getString(getString(R.string.pop3Password), "");
        String smtpLogin = sharedPreferences.getString(getString(R.string.smtpLogin), "");
        String smtpPassword = sharedPreferences.getString(getString(R.string.smtpPassword), "");

        totalSmsCounter = sharedPreferences.getInt(getString(R.string.totalSmsCounter), 0);
        totalMessagesCounter = sharedPreferences.getInt(getString(R.string.totalMessagesCounter), 0);


        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        configIsCorrect = true;
        if (pop3Login.equalsIgnoreCase("") ||
            pop3Password.equalsIgnoreCase("") ||
            pop3ServerName.equalsIgnoreCase("") ||
            pop3ServerPort < 1 ||
            pop3ServerPort > 63535) {
            configIsCorrect = false;
        }

        if (!mailSystem.testPopConnection()) configIsCorrect = false;

        if (sendLogViaMail) {
            if (smtpServerName.equalsIgnoreCase("") ||
                smtpServerPort < 1 ||
                smtpServerPort > 63535) {
                configIsCorrect = false;
            }

            if (smtpAuthentication) {
                if (smtpLogin.equalsIgnoreCase("") ||
                    smtpPassword.equalsIgnoreCase("")) {
                    configIsCorrect = false;
                }
            }

            if (!mailSystem.testSmtpConnection()) configIsCorrect = false;
        }

        showNotificationNormalState(configIsCorrect);
        if (!configIsCorrect) return;

        int checkingInterval = sharedPreferences.getInt(getString(R.string.checkingInterval), 60);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkEmail();
            }
        }, 10, checkingInterval, TimeUnit.SECONDS);
    }

    private void checkEmail(){
        Log.d("nibbler", "BackgroundEmailCheck checkEmail");
        String[] messages = mailSystem.checkEmail();
        Log.d("nibbler", "BackgroundEmailCheck checkEmail, messages.length: " + messages.length);

        if (sharedPreferences.getBoolean(getString(R.string.needToSendLogToEmail), false)) {
            Log.d("nibbler", "BackgroundEmailCheck checkEmail needToSendLogToEmail");
            try {
                mailSystem.sendLogFileTo(new InternetAddress(sharedPreferences.getString(getString(R.string.notificationEmailAddress), "nibble@yandex.ru")));
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(getString(R.string.needToSendLogToEmail), false);
                editor.apply();
            } catch (AddressException e) {
                logFile.writeToLog("Ошибка;Не удалось выполнить автоматическое отправление лог-файла;BackgroundEmailCheck checkEmail AddressException");
                Log.d("nibbler", "BackgroundEmailCheck checkEmail can't auto send log file");
            }
        }

        if (messages.length == 0) return;
        logFile.writeToLog("Обнаружено сообщений: " + messages.length/2);

        boolean needToNotification = false;
        for (int i = 0; i < messages.length; i += 2){
            if (messages[i] != null) {
                //isAnythingFounded = true;
                Log.d("nibbler", "BackgroundEmailCheck checkEmail --- message " + i/2 + " subject: " + messages[i] + " content: " + messages[i+1]);
                try{
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> msgArray = smsManager.divideMessage(messages[i+1]);
                    Log.d("nibbler", "TRYING to send SMS to: " + messages[i] + " text: " + messages[i+1]);
                    smsManager.sendMultipartTextMessage(messages[i], null, msgArray, null, null);
                    totalSmsCounter += msgArray.size();
                    totalMessagesCounter += 1;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(getString(R.string.totalSmsCounter), totalSmsCounter);
                    editor.putInt(getString(R.string.totalMessagesCounter), totalMessagesCounter);
                    editor.apply();
                    logFile.writeToLog("Сообщения для;" + messages[i] + ";со следующим содержанием;" + messages[i+1] + ";было отправлено, кол-во СМС;" + msgArray.size());
                } catch (Exception ex) {
                    needToNotification = true;
                    Log.d("nibbler", "BackgroundEmailCheck checkEmail SMSManager Exception");
                    logFile.writeToLog("Сообщение не было отправлено;SMSManager Exception");
                    ex.printStackTrace();
                }
            }
        }

        if (needToNotification) {
            mailSystem.sendEmailNotification("Не удалось отправить SMS сообщение, ошибка SMSManager Exception");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Log.d("nibbler", "BackgroundEmailCheck onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        mNM.cancel(NOTIFICATION);
        if (configIsCorrect) scheduledFuture.cancel(false);
        Log.d("nibbler", "BackgroundEmailCheck onDestroy");
    }

    private void showNotificationNormalState(boolean status){
        CharSequence text;
        int icon;
        if (status) {
            text = "Сервис запущен";
            icon = R.drawable.ic_launcher;
        } else {
            text = "Неверные настройки почты!";
            icon = R.drawable.ic_launcher_red;

            if (sharedPreferences.getBoolean(getString(R.string.sendMailIfError), false)) {
                ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
                scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                        startSendingErrorNotification();
                    }
                }, 0, 60, TimeUnit.MINUTES);
            }
        }

        @SuppressWarnings("deprecation")
        Notification notification = new Notification(icon, text, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(this, "Email2SMS", text, contentIntent);
        mNM.notify(NOTIFICATION, notification);
    }

    public void startSendingErrorNotification(){
        mailSystem.sendEmailNotification("Не удается запустить сервис Email2SMS, ошибка в подключении к серверу почты (POP3|SMTP).");
    }
}
