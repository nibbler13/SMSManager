package com.nibbler.email2sms;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by NIBBLER on 22/02/15.
 */
public class MailSystem {

    private final int NUMBER_OF_WEEK_DAYS = 7;
    private SharedPreferences sharedPreferences;
    private LogFile logFile;
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
    private Context context;

    //general counters
    private int totalEmailCounter;

    //testing
    boolean needToTestConnection;
    boolean testingResult;

    public MailSystem(Context newContext) {
        context = newContext;
        logFile = new LogFile(context);
        //needToTestConnection = false;
        //Log.d("nibbler", "MailSystem Constructor needToTestConnection: " + needToTestConnection);
        testingResult = false;

        sharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedSettingsName), Context.MODE_PRIVATE);

        maxSymbolsInSMS = sharedPreferences.getInt(context.getString(R.string.maxSymbolsInSMS), 350);
        pop3ServerName = sharedPreferences.getString(context.getString(R.string.pop3ServerName), "");
        pop3ServerPort = sharedPreferences.getInt(context.getString(R.string.pop3ServerPort), 0);
        sendLogViaMail = sharedPreferences.getBoolean(context.getString(R.string.sendLogViaMail), true);
        smtpServerName = sharedPreferences.getString(context.getString(R.string.smtpServerName), "");
        smtpServerPort = sharedPreferences.getInt(context.getString(R.string.smtpServerPort), 0);
        smtpAuthentication = sharedPreferences.getBoolean(context.getString(R.string.smtpAuthentication), true);
        pop3UseSSL = sharedPreferences.getBoolean(context.getString(R.string.pop3UseSSL), true);
        pop3Login = sharedPreferences.getString(context.getString(R.string.pop3Login), "");
        pop3Password = sharedPreferences.getString(context.getString(R.string.pop3Password), "");
        smtpLogin = sharedPreferences.getString(context.getString(R.string.smtpLogin), "");
        smtpPassword = sharedPreferences.getString(context.getString(R.string.smtpPassword), "");
        smtpUseSSL = sharedPreferences.getBoolean(context.getString(R.string.smtpUseSSL), true);
        emailFolderName = sharedPreferences.getString(context.getString(R.string.emailFolderName), "");

        totalEmailCounter = sharedPreferences.getInt(context.getString(R.string.totalEmailCounter), 0);

        Set<String> addressesSet = sharedPreferences.getStringSet(context.getString(R.string.addresses_list), new HashSet<String>(Arrays.asList(new String[]{""})));
        Set<String> excludedSet = sharedPreferences.getStringSet(context.getString(R.string.excluded_list), new HashSet<String>(Arrays.asList(new String[]{""})));
        Set<String> unreadableSet = sharedPreferences.getStringSet(context.getString(R.string.unreadable_list), new HashSet<String>(Arrays.asList(new String[]{""})));

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
    }

    private class SendLogFile extends AsyncTask<Address, Void, Boolean> {
        protected Boolean doInBackground(javax.mail.Address... addresses){
            Log.d("nibbler", "MailSystem SendLogFile needToCheck: " + needToTestConnection);
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

                BodyPart messageBodyPart = new MimeBodyPart();
                if (needToTestConnection) {
                    message.setSubject("Email2SMS SMTP connection testing");
                } else {
                    message.setSubject("Email2SMS LogFile");
                }
                messageBodyPart.setText(logFile.timeStamp());
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);
                if (!needToTestConnection) {
                    messageBodyPart = new MimeBodyPart();
                    if (logFile.getLogFile() != null) {
                        DataSource source = new FileDataSource(logFile.getLogFile());
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        messageBodyPart.setFileName("Log.csv");
                        multipart.addBodyPart(messageBodyPart);
                    }
                }
                message.setContent(multipart);

                Transport.send(message);
                if (needToTestConnection) {
                    logFile.writeToLog("Тестовое сообщение было успешно выслано на адрес;" + InternetAddress.toString(addresses));
                } else {
                    logFile.writeToLog("Лог-файл был успешно выслан на адрес;" + InternetAddress.toString(addresses));
                }
                Log.d("nibbler", "message successfully sent");
                return true;
            } catch (MessagingException mex) {
                mex.printStackTrace();
                logFile.writeToLog("Не удалось отправить письмо");
                Log.d("nibbler", "MailSystem SendLogFile !!!MessagingException");
            }
            return false;
        }
    }

    private class PerformCheck extends AsyncTask<Void, Void, String[]>{
        @SuppressLint("DefaultLocale")
        @Override
        protected String[] doInBackground(Void... params){
            testingResult = false;
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
                    Log.d("nibbler", "MailSystem PerformCheck localFolder is notNULL");
                    Log.d("nibbler", "MailSystem PerformCheck needToTestConnection: " + needToTestConnection);
                    localFolder.open(Folder.READ_WRITE);
                    if (needToTestConnection) {
                        testingResult = true;
                        needToTestConnection = false;
                        return null;
                    }
                    arrayOfMessage = localFolder.getMessages();
                    messagesTotal = arrayOfMessage.length;
                    Log.d("nibbler", "MailSystem PerformCheck Inbox contains: " + messagesTotal);
                    if (messagesTotal <= 0) {
                        localFolder.close(true);
                        localStore.close();
                        Log.d("nibbler", "MailSystem PerformCheck closing localFolder & localStore");
                        return null;
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
                        editor.putInt(context.getString(R.string.totalEmailCounter), totalEmailCounter);
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
                                        Log.d("nibbler", "MailSystem PerformCheck time is out of range");
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
                                logFile.writeToLog("Ошибка;MailSystem PerformCheck MessagingException");
                                Log.d("nibbler", "MailSystem PerformCheck MessagingException");
                            } catch (Exception ex) {
                                logFile.writeToLog("Ошибка;MailSystem PerformCheck Exception");
                                Log.d("nibbler", "MailSystem PerformCheck Exception");
                            }
                        }
                        localFolder.close(true);
                        localStore.close();
                        if (messagesTotal > 0) {
                            Log.d("nibbler", "MailSystem PerformCheck messagesTotal: " + messagesTotal);
                        }
                        Log.d("nibbler", "MailSystem PerformCheck returning messaging array");
                        return messagesToSMS.toArray(new String[0]);
                    }
                } else {
                    localStore.close();
                }
            } catch (Exception localException) {
                //localException.printStackTrace();
                Log.d("nibbler", "MailSystem PerformCheck EXCEPTION");
                logFile.writeToLog("Ошибка;MailSystem PerformCheck EXCEPTION");
            }
            needToTestConnection = false;
            return null;
        }
    }

    public String[] checkEmail () {
        Log.d("nibbler", "MailSystem checkMail");
        PerformCheck performCheck = new PerformCheck();
        performCheck.execute();
        try {
            String[] strings = performCheck.get();
            if (strings == null) return new String[0];
            Log.d("nibbler", "MailSystem checkMail return strings.length: " + strings.length);
            return strings;
        } catch (ExecutionException e) {
            Log.d("nibbler", "MailSystem PerformCheck ExecutionException");
        } catch (InterruptedException e) {
            Log.d("nibbler", "MailSystem PerformCheck InterruptedException");
        }
        Log.d("nibbler", "MailSystem checkMail return new String[0]");
        return new String[0];
    }

    public boolean sendLogFileTo(Address address){
        if (address == null) return false;
        SendLogFile sendLogFile = new SendLogFile();
        sendLogFile.execute(address);
        try {
            return sendLogFile.get();
        } catch (ExecutionException e) {
            Log.d("nibbler", "MailSystem sendLogFileTo ExecutionException");
        } catch (InterruptedException e) {
            Log.d("nibbler", "MailSystem sendLogFileTo InterruptedException");
        }
        return false;
    }

    public boolean testSmtpConnection() {
        needToTestConnection = true;
        Log.d("nibbler", "MailSystem testSmtpConnection needToTestConnection: " + needToTestConnection);
        Address address;
        try {
            address = new InternetAddress("nibble@yandex.ru");
        } catch (AddressException e) {
            Log.d("nibbler", "MailSystem testSmtpConnection AddressException");
            needToTestConnection = false;
            return false;
        }
        SendLogFile sendLogFile = new SendLogFile();
        sendLogFile.execute(address);
        try {
            return sendLogFile.get();
        } catch (ExecutionException e) {
            Log.d("nibbler", "MailSystem sendLogFileTo ExecutionException");
        } catch (InterruptedException e) {
            Log.d("nibbler", "MailSystem sendLogFileTo InterruptedException");
        }
        needToTestConnection = false;
        return false;
    }

    public boolean testPopConnection() {
        needToTestConnection = true;
        PerformCheck performCheck = new PerformCheck();
        performCheck.execute();
        try {
            String[] strings = performCheck.get();
        } catch (ExecutionException e) {
            Log.d("nibbler", "MailSystem PerformCheck ExecutionException");
        } catch (InterruptedException e) {
            Log.d("nibbler", "MailSystem PerformCheck InterruptedException");
        }
        Log.d("nibbler", "MailSystem testPopConnection needToTestConnection: " + needToTestConnection);
        Log.d("nibbler", "MailSystem testPopConnection testingResult: " + testingResult);
        return testingResult;
    }
}
