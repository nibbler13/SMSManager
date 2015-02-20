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
import java.util.HashSet;
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
    private String[] addresses_list;
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

        addresses_list = addressesSet.toArray(new String[0]);
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
        Log.d("nibbler", "background service checkEmail");

        PerformCheck performCheck = new PerformCheck();
        performCheck.execute();
        boolean isAnythingFounded = false;
        try {
            String[] messages = performCheck.get();
            logFile.writeToLog("Обнаружено сообщений: " + messages.length/2);

            for (int i = 0; i < messages.length; i += 2){
                if (messages[i] != null) {
                    isAnythingFounded = true;
                    Log.d("nibbler", "BackgroundEmailCheck checkEmail --- message " + i/2 + " subject: " + messages[i] + " content: " + messages[i+1]);
                    try{
                        SmsManager smsManager = SmsManager.getDefault();
                        ArrayList<String> msgArray = smsManager.divideMessage(messages[i+1]);
                        smsManager.sendMultipartTextMessage(messages[i], null, msgArray, null, null);
                        totalSmsCounter += msgArray.size();
                        totalMessagesCounter += 1;
                        logFile.writeToLog("Сообщения для;" + messages[i] + ";со следующим содержанием;" + messages[i+1] + ";было отправлено, кол-во СМС;" + msgArray.size());
                    } catch (Exception ex) {
                        Log.d("nibbler", "BackgroundEmailCheck checkEmail SmsManager Exception");
                        logFile.writeToLog("Сообщение не было отправлено;SmsManager Exception");
                        ex.printStackTrace();
                    }
                }
            }
            if (isAnythingFounded){
                logFile.writeToLog("Информация;СМС отправлено: " + Integer.toString(totalSmsCounter) + ", messagesCounter: " + Integer.toString(totalMessagesCounter) + ", Сообщений получено: " + Integer.toString(totalEmailCounter));
            }
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
            localPropertiesSmtp.setProperty("mail.smtp.port", "465");
            localPropertiesSmtp.setProperty("mail.smtp.connectiontimeout", "4000");
            localPropertiesSmtp.setProperty("mail.smtp.timeout", "10000");
            localPropertiesSmtp.setProperty("mail.smtp.host", "smtp.yandex.com");
            localPropertiesSmtp.setProperty("mail.smtp.auth", "true");
            localPropertiesSmtp.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            localPropertiesSmtp.setProperty("mail.smtp.socketFactory.port", "465");
            localPropertiesSmtp.setProperty("mail.smtp.socketFactory.fallback", "false");
            Session session = Session.getInstance(localPropertiesSmtp, new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication("nibble2", "N!i9b8b7##");
                }
            });
            try {
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress("nibble2@yandex.ru"));
                if (addresses[0] != null) {
                    message.addRecipient(Message.RecipientType.TO, addresses[0]);
                } else {
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress("nibble@yandex.ru"));
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
                Log.d("nibbler", "message successfully sent");
            } catch (MessagingException mex) {
                mex.printStackTrace();
                Log.d("nibbler", "!!!MessagingException");
            }
            return null;
        }
    }

    public class PerformCheck extends AsyncTask<Void, Void, String[]>{
        String toLogFile = "";
        @SuppressLint("DefaultLocale")
        @Override
        protected String[] doInBackground(Void... params){
            try {
                Properties localProperties = System.getProperties();
                localProperties.setProperty("mail.pop3.port", "995");
                localProperties.setProperty("mail.pop3.connectiontimeout", "4000");
                localProperties.setProperty("mail.pop3.timeout", "10000");
                localProperties.setProperty("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                localProperties.setProperty("mail.pop3.socketFactory.port", "995");
                localProperties.setProperty("mail.pop3.socketFactory.fallback", "false");
                Store localStore = Session.getDefaultInstance(localProperties).getStore("pop3");
                localStore.connect("pop.yandex.com", "nibble2", "N!i9b8b7##");
                Folder localFolder = localStore.getFolder("inbox");
                Message[] arrayOfMessage;
                String[] messagesToSMS;
                int messagesTotal = 0;
                if (localFolder != null) {
                    Log.d("nibbler", "localFolder is notNULL");
                    //toLogFile += timeStamp() + "LocalFolder is notNull\r\n";
                    localFolder.open(Folder.READ_WRITE);
                    arrayOfMessage = localFolder.getMessages();
                    messagesTotal = arrayOfMessage.length;
                    messagesToSMS = new String[messagesTotal * 2];
                    Log.d("nibbler", "Inbox contains: " + messagesTotal);
                    //toLogFile += timeStamp() + "Inbox contains," + messagesTotal + "\r\n";
                    if (messagesTotal <= 0) {
                        localFolder.close(true);
                        localStore.close();
                    } else {
                        //emailsCounter += messagesTotal;

                        for (int i = 0; i < messagesTotal; i++) {
                            try {
                                Object content = arrayOfMessage[i].getContent();
                                String contentType = arrayOfMessage[i].getContentType().toLowerCase();
                                String subject = arrayOfMessage[i].getSubject().toLowerCase();

                                if (subject.contains("log")){
                                    SendLogFile sendLog = new SendLogFile();
                                    sendLog.execute(arrayOfMessage[i].getFrom());
                                }

                                toLogFile += "Получено сообщение с темой;" + subject;

                                if (contentType.contains("text")){
                                    if (subject.contains("������")) {
                                        //������� ����� ��������
                                        messagesToSMS[i*2] = "89601811873";
                                    } else if (subject.contains("������")){
                                        //������� ������ �����������
                                        messagesToSMS[i*2] = "89674659975";
                                    } else if (subject.contains("���������")){
                                        //���������� ����� ���������
                                        messagesToSMS[i*2] = "89673034433";
                                    } else if (subject.contains("���")){
                                        //������� ������� ����������
                                        messagesToSMS[i*2] = "89603848641";
                                    } else if (subject.contains("���������")){
                                        //������ �������� ���������
                                        messagesToSMS[i*2] = "89216516778";
                                    } else if (subject.contains("�����������")){
                                        //������� �������� ��������
                                        messagesToSMS[i*2] = "89095327069";
                                    } else if (subject.contains("����������")){
                                        //������ ��������� �������������
                                        messagesToSMS[i*2] = "89659031324";
                                    }

                                    if (messagesToSMS[i*2] != null) {
                                        messagesToSMS[i*2+1] = content.toString()
                                                .replace("\n", "")
                                                .replace("\r", "")
                                                .replace("<BR>", " ")
                                                .replace(";", ",")
                                                .replace("&lt,", "")
                                                .replace("&gt,", "")
                                                .replace("<a href=\"mailto:", "");
                                        toLogFile += ";с текстом;" + messagesToSMS[i*2+1];
                                        if (messagesToSMS[i*2+1].length() > 350) {
                                            messagesToSMS[i*2+1] = messagesToSMS[i*2+1].substring(0, 350);
                                        }
                                    }
                                }
                                toLogFile += "\r\n";
                                arrayOfMessage[i].setFlag(Flags.Flag.DELETED, true);
                            } catch (MessagingException ex) {
                                Log.d("nibbler", "ME");
                                toLogFile += "MessagingException ME";
                            } catch (Exception ex) {
                                Log.d("nibbler", "EE");
                                toLogFile += "Exception EE";
                            }
                        }
                        localFolder.close(true);
                        localStore.close();
                        if (messagesTotal > 0) {
                            logFile.writeToLog(toLogFile);
                            Log.d("nibbler", "messagesTotal: " + messagesTotal);
                            //toLogFile = "Информация;СМС отправлено: " + smsCounter.toString() + ", messagesCounter: " + messagesCounter.toString() + ", Сообщений получено: " + emailsCounter.toString();
                            //logFile.writeToLog(toLogFile);
                        }
                        return messagesToSMS;
                    }
                } else {
                    localStore.close();
                }
            } catch (Exception localException) {
                localException.printStackTrace();
                Log.d("nibbler", "EXCEPTION");
                toLogFile += "Exception localException";
                logFile.writeToLog(toLogFile);
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
