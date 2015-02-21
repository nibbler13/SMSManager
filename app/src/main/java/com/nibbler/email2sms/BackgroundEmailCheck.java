package com.nibbler.email2sms;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by NIBBLER on 16/02/15.
 */
public class BackgroundEmailCheck extends Service {

    private NotificationManager mNM;
    private int NOTIFICATION = 76667;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> scheduledFuture;
    private SharedPreferences sharedPreferences;
    private LogFile logFile;

    private final int NUMBER_OF_WEEK_DAYS = 7;

    //settings
    private int maxSymbolsInSMS;
    private String pop3ServerName;
    private int pop3ServerPort;
    private boolean pop3UseSSL;
    private String pop3Login;
    private String pop3Password;
    private boolean sendLogViaMail;
    private String smtpServerName;
    private int smtpServerPort;
    private boolean smtpAuthentication;
    private String smtpLogin;
    private String smtpPassword;
    private boolean smtpUseSSL;
    private String emailFolderName;
    private String[] addresses_name;
    private String[] addresses_number;
    private String[] excluded_list;
    private String[] unreadable_list;
    private TimeTableElement[] timeTableElements;

    //general counters
    private int totalSmsCounter;
    private int totalMessagesCounter;
    private int totalEmailCounter;

    private boolean configIsCorrect;

    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        Log.d("nibbler", "BackgroundEmailCheck onCreate");
        logFile = new LogFile(this);
        logFile.writeToLog("Сервис создан");

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);

        maxSymbolsInSMS = sharedPreferences.getInt(getString(R.string.maxSymbolsInSMS), 350);
        pop3ServerName = sharedPreferences.getString(getString(R.string.pop3ServerName), "");
        pop3ServerPort = sharedPreferences.getInt(getString(R.string.pop3ServerPort), 0);
        sendLogViaMail = sharedPreferences.getBoolean(getString(R.string.sendLogViaMail), true);
        smtpServerName = sharedPreferences.getString(getString(R.string.smtpServerName), "");
        smtpServerPort = sharedPreferences.getInt(getString(R.string.smtpServerPort), 0);
        smtpAuthentication = sharedPreferences.getBoolean(getString(R.string.smtpAuthentication), true);
        pop3UseSSL = sharedPreferences.getBoolean(getString(R.string.pop3UseSSL), true);
        pop3Login = sharedPreferences.getString(getString(R.string.pop3Login), "");
        pop3Password = sharedPreferences.getString(getString(R.string.pop3Password), "");
        smtpLogin = sharedPreferences.getString(getString(R.string.smtpLogin), "");
        smtpPassword = sharedPreferences.getString(getString(R.string.smtpPassword), "");
        smtpUseSSL = sharedPreferences.getBoolean(getString(R.string.smtpUseSSL), true);
        emailFolderName = sharedPreferences.getString(getString(R.string.emailFolderName), "");

        totalSmsCounter = sharedPreferences.getInt(getString(R.string.totalSmsCounter), 0);
        totalMessagesCounter = sharedPreferences.getInt(getString(R.string.totalMessagesCounter), 0);
        totalEmailCounter = sharedPreferences.getInt(getString(R.string.totalEmailCounter), 0);

        Set<String> addressesSet = sharedPreferences.getStringSet(getString(R.string.addresses_list), new HashSet<String>(Arrays.asList(new String[]{""})));
        Set<String> excludedSet = sharedPreferences.getStringSet(getString(R.string.excluded_list), new HashSet<String>(Arrays.asList(new String[]{""})));
        Set<String> unreadableSet = sharedPreferences.getStringSet(getString(R.string.unreadable_list), new HashSet<String>(Arrays.asList(new String[]{""})));

        String[] addresses_list = addressesSet.toArray(new String[0]);
        addresses_name = new String[addresses_list.length];
        addresses_number = new String[addresses_list.length];
        for (int i = 0; i < addresses_list.length; i++) {
            String[] separatedString = addresses_list[i].split("=", 2);
            addresses_name[i] = separatedString[0].toLowerCase();
            addresses_number[i] = separatedString[1];
        }

        excluded_list = excludedSet.toArray(new String[0]);
        unreadable_list = unreadableSet.toArray(new String[0]);

        timeTableElements = new TimeTableElement[]{
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement()};

        for (int i = 0; i < NUMBER_OF_WEEK_DAYS; i++) {
            timeTableElements[i].setHour_end(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_hour_end", 23));
            timeTableElements[i].setHour_start(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_hour_start", 0));
            timeTableElements[i].setMinute_end(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_minute_end", 59));
            timeTableElements[i].setMinute_start(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_minute_start", 0));
            timeTableElements[i].setEnable(sharedPreferences.getBoolean(TimeTableElement.weekDays[i] + "_checkbox", true));
        }


        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        configIsCorrect = true;
        if (pop3Login.equalsIgnoreCase("") ||
            pop3Password.equalsIgnoreCase("") ||
            pop3ServerName.equalsIgnoreCase("") ||
            pop3ServerPort < 1 ||
            pop3ServerPort > 63535) {
            configIsCorrect = false;
        }

        if (sendLogViaMail) {
            if (smtpLogin.equalsIgnoreCase("") ||
                smtpPassword.equalsIgnoreCase("") ||
                smtpServerName.equalsIgnoreCase("") ||
                smtpServerPort < 1 ||
                smtpServerPort > 63535) {
                configIsCorrect = false;
            }
        }
        showNotificationNormalState(configIsCorrect);
        if (!configIsCorrect) return;
        ////////
        //need to send notification to admin
        ////////

        int checkingInterval = sharedPreferences.getInt(getString(R.string.checkingInterval), 60);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkEmail();
            }
        }, 10, checkingInterval, TimeUnit.SECONDS);
    }

    private void checkEmail(){
        Log.d("nibbler", "BackgroundEmailCheck checkEmail");

        PerformCheck performCheck = new PerformCheck();
        performCheck.execute();
        //boolean isAnythingFounded = false;
        try {
            String[] messages = performCheck.get();
            if (messages.length == 0) return;
            logFile.writeToLog("Обнаружено сообщений: " + messages.length/2);

            for (int i = 0; i < messages.length; i += 2){
                if (messages[i] != null) {
                    //isAnythingFounded = true;
                    Log.d("nibbler", "BackgroundEmailCheck checkEmail --- message " + i/2 + " subject: " + messages[i] + " content: " + messages[i+1]);
                    try{
                        SmsManager smsManager = SmsManager.getDefault();
                        ArrayList<String> msgArray = smsManager.divideMessage(messages[i+1]);
                        smsManager.sendMultipartTextMessage(messages[i], null, msgArray, null, null);
                        totalSmsCounter += msgArray.size();
                        totalMessagesCounter += 1;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(getString(R.string.totalSmsCounter), totalSmsCounter);
                        editor.putInt(getString(R.string.totalMessagesCounter), totalMessagesCounter);
                        editor.apply();
                        logFile.writeToLog("Сообщения для;" + messages[i] + ";со следующим содержанием;" + messages[i+1] + ";было отправлено, кол-во СМС;" + msgArray.size());
                    } catch (Exception ex) {
                        Log.d("nibbler", "BackgroundEmailCheck checkEmail SMSManager Exception");
                        logFile.writeToLog("Сообщение не было отправлено;SMSManager Exception");
                        ex.printStackTrace();
                    }
                }
            }
            /*if (isAnythingFounded){
                logFile.writeToLog("Информация;СМС отправлено: " + Integer.toString(totalSmsCounter) + ", messagesCounter: " + Integer.toString(totalMessagesCounter) + ", Сообщений получено: " + Integer.toString(totalEmailCounter));
            }*/
        } catch (InterruptedException e) {
            Log.d("nibbler", "BackgroundEmailCheck checkEmail --- MESSAGES CATCH InterruptedException");
            logFile.writeToLog("BackgroundEmailCheck checkEmail --- MESSAGES CATCH InterruptedException");
            e.printStackTrace();
        } catch (ExecutionException e) {
            Log.d("nibbler", "BackgroundEmailCheck checkEmail --- MESSAGES CATCH ExecutionException");
            logFile.writeToLog("BackgroundEmailCheck checkEmail --- MESSAGES CATCH ExecutionException");
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d("nibbler", "BackgroundEmailCheck checkEmail --- There is no new emails");
            e.printStackTrace();
        }
    }

    public class SendLogFile extends AsyncTask<Address, Void, Void> {
        protected Void doInBackground(javax.mail.Address... addresses){
            Properties localPropertiesSmtp = System.getProperties();
            localPropertiesSmtp.setProperty("mail.smtp.port", Integer.toString(smtpServerPort));
            localPropertiesSmtp.setProperty("mail.smtp.connectiontimeout", "4000");
            localPropertiesSmtp.setProperty("mail.smtp.timeout", "10000");
            localPropertiesSmtp.setProperty("mail.smtp.host", smtpServerName);
            if (smtpAuthentication) localPropertiesSmtp.setProperty("mail.smtp.auth", "true");
            if (smtpUseSSL) {
                localPropertiesSmtp.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                localPropertiesSmtp.setProperty("mail.smtp.socketFactory.port", Integer.toString(smtpServerPort));
                localPropertiesSmtp.setProperty("mail.smtp.socketFactory.fallback", "false");
            }
            Session session;
            if (smtpAuthentication) {
                session = Session.getInstance(localPropertiesSmtp, new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpLogin, smtpPassword);
                    }
                });
            } else {
                session = Session.getInstance(localPropertiesSmtp);
            }

            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(smtpLogin));
                if (addresses[0] != null) {
                    message.addRecipient(Message.RecipientType.TO, addresses[0]);
                } else {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress("nibble@yandex.ru"));
                    /////
                    //Here are should be administrator email
                    /////
                }
                message.setSubject("Email2SMS LogFile");
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setText(logFile.timeStamp());
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                messageBodyPart = new MimeBodyPart();
                if (logFile.getLogFile() != null) {
                    DataSource source = new FileDataSource(logFile.getLogFile());
                    messageBodyPart.setDataHandler(new DataHandler(source));
                    messageBodyPart.setFileName("Log.csv");
                    multipart.addBodyPart(messageBodyPart);
                }
                message.setContent(multipart);
                Transport.send(message);
                logFile.writeToLog("Лог-файл был успешно выслан на адрес;" + InternetAddress.toString(addresses));
                Log.d("nibbler", "message successfully sent");
            } catch (MessagingException mex) {
                mex.printStackTrace();
                logFile.writeToLog("Не удалось отправить лог-файл");
                Log.d("nibbler", "!!!MessagingException");
            }
            return null;
        }
    }

    public class PerformCheck extends AsyncTask<Void, Void, String[]>{
        @SuppressLint("DefaultLocale")
        @Override
        protected String[] doInBackground(Void... params){
            try {
                Properties localProperties = System.getProperties();

                //Log.d("nibbler", "BackgroundEmailCheck PerformCheck port: " + Integer.toString(pop3ServerPort) + " serverName: " + pop3ServerName + " login: " + pop3Login + " password: " + pop3Password + " ssl: " + Integer.toString((pop3UseSSL) ? 1 : 0) + " emailFolder: " + emailFolderName);

                localProperties.setProperty("mail.pop3.port", Integer.toString(pop3ServerPort));
                localProperties.setProperty("mail.pop3.connectiontimeout", "4000");
                localProperties.setProperty("mail.pop3.timeout", "10000");
                if (pop3UseSSL) {
                    localProperties.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    localProperties.setProperty("mail.pop3.socketFactory.port", Integer.toString(pop3ServerPort));
                    localProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
                }
                Store localStore = Session.getDefaultInstance(localProperties).getStore("pop3");
                localStore.connect(pop3ServerName, pop3Login, pop3Password);
                Folder localFolder = localStore.getFolder(emailFolderName);

                Message[] arrayOfMessage;
                List<String> messagesToSMS = new ArrayList<>();
                int messagesTotal = 0;
                if (localFolder != null) {
                    Log.d("nibbler", "localFolder is notNULL");
                    localFolder.open(Folder.READ_WRITE);
                    arrayOfMessage = localFolder.getMessages();
                    messagesTotal = arrayOfMessage.length;
                    Log.d("nibbler", "Inbox contains: " + messagesTotal);
                    if (messagesTotal <= 0) {
                        localFolder.close(true);
                        localStore.close();
                    } else {
                        totalEmailCounter += messagesTotal;
                        Calendar calendar = Calendar.getInstance();
                        int currentDay = calendar.get(Calendar.DAY_OF_WEEK);
                        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
                        int currentMinute = calendar.get(Calendar.MINUTE);
                        currentDay -= 2;
                        if (currentDay < 0) currentDay = 6;
                        //Log.d("nibbler", "BackgroundEmailCheck PerformCheck --- currentDay: " + Integer.toString(currentDay) + " currentHour: " + Integer.toString(currentHour) + " currentMinute: " + Integer.toString(currentMinute));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(getString(R.string.totalEmailCounter), totalEmailCounter);
                        editor.apply();

                        for (int i = 0; i < messagesTotal; i++) {
                            try {
                                Object content = arrayOfMessage[i].getContent();
                                String contentType = arrayOfMessage[i].getContentType().toLowerCase();
                                String subject = arrayOfMessage[i].getSubject().toLowerCase();

                                if (subject.contains("log") && sendLogViaMail){
                                    SendLogFile sendLog = new SendLogFile();
                                    sendLog.execute(arrayOfMessage[i].getFrom());
                                    logFile.writeToLog("Получено письмо с темой;" + subject +";отправитель;" + InternetAddress.toString(arrayOfMessage[i].getFrom()));
                                } else {
                                    logFile.writeToLog("Получено письмо с темой;" + subject);
                                }

                                if (contentType.contains("text")){

                                    boolean needToIgnore = false;


                                    if (timeTableElements[currentDay].getEnable() &&
                                        currentHour >= timeTableElements[currentDay].getHour_start() &&
                                        currentHour <= timeTableElements[currentDay].getHour_end() &&
                                        currentMinute >= timeTableElements[currentDay].getMinute_start() &&
                                        currentMinute <= timeTableElements[currentDay].getMinute_end()) {

                                        for (int count = 0; count < excluded_list.length; count++) {
                                        if (content.toString().contains(excluded_list[count])) {
                                            needToIgnore = true;
                                            logFile.writeToLog("Письмо содержит ключевую фразу из списка исключений и будет проигнорировано;" + excluded_list[count]);
                                        }
                                    }
                                        //Log.d("nibbler", "BackgroundEmailCheck PerformCheck time is OK");
                                    } else {
                                        needToIgnore = true;
                                        Log.d("nibbler", "BackgroundEmailCheck PerformCheck time is out of range");
                                        logFile.writeToLog("Время получения письма выходит за границы заданного расписания, письмо будет проигнорировано");
                                    }

                                    for (int count = 0; count < addresses_name.length; count++) {
                                        if (subject.contains(addresses_name[count]) && !needToIgnore) {
                                            messagesToSMS.add(addresses_name[count]);
                                            logFile.writeToLog("Письмо будет выслано адресату;" + addresses_name[count] + ";на номер;" + addresses_number[count]);

                                            String resultingMessage = content.toString().replace("\r", "").replace("\n", "").replace(";", ",");
                                            for (int count2 = 0; count2 < unreadable_list.length; count2++) {
                                                resultingMessage = resultingMessage.replace(unreadable_list[count2], "");
                                            }

                                            if (resultingMessage.length() > maxSymbolsInSMS) {
                                                resultingMessage = resultingMessage.substring(0, maxSymbolsInSMS);
                                            }

                                            messagesToSMS.add(resultingMessage);
                                        }
                                    }
                                }
                                arrayOfMessage[i].setFlag(Flags.Flag.DELETED, true);
                            } catch (MessagingException ex) {
                                logFile.writeToLog("Ошибка;BackgroundEmailCheck PerformCheck MessagingException");
                                Log.d("nibbler", "BackgroundEmailCheck PerformCheck MessagingException");
                            } catch (Exception ex) {
                                logFile.writeToLog("Ошибка;BackgroundEmailCheck PerformCheck Exception");
                                Log.d("nibbler", "BackgroundEmailCheck PerformCheck Exception");
                            }
                        }
                        localFolder.close(true);
                        localStore.close();
                        if (messagesTotal > 0) {
                            Log.d("nibbler", "BackgroundEmailCheck PerformCheck messagesTotal: " + messagesTotal);
                        }
                        return messagesToSMS.toArray(new String[0]);
                    }
                } else {
                    localStore.close();
                }
            } catch (Exception localException) {
                //localException.printStackTrace();
                Log.d("nibbler", "BackgroundEmailCheck PerformCheck EXCEPTION");
                logFile.writeToLog("Ошибка;BackgroundEmailCheck PerformCheck EXCEPTION");
            }
            return null;
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
            text = "Неверные настройки!";
            icon = R.drawable.ic_launcher_red;
        }

        @SuppressWarnings("deprecation")
        Notification notification = new Notification(icon, text, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(this, "Email2SMS", text, contentIntent);
        mNM.notify(NOTIFICATION, notification);
    }
}
