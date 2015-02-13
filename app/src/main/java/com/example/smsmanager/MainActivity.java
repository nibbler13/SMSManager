package com.example.smsmanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
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


public class MainActivity extends Activity implements OnClickListener {
	
	private Button startServiceButton;
	private TextView totalSmsLabel;
	private TextView totalMessagesLabel;
	private TextView totalEmailsReceivedLabel;
	private TextView serviceLabel;
	private Integer smsCounter = 0;
    private Integer messagesCounter = 0;
    private Integer emailsCounter = 0;
    private Boolean isServiceRun = false;
    Context context = this;
	ScheduledExecutorService scheduledExecutorService;
    ScheduledFuture<?> scheduledFuture;
    SharedPreferences sharedPreferences;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	String toLogFile = timeStamp() + "Starting application\r\n";
    	writeToLog(toLogFile);
    	Log.d("nibbler", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);
        
        startServiceButton = (Button) findViewById(R.id.sendSMS);
        startServiceButton.setOnClickListener(this);
        
        totalSmsLabel = (TextView) findViewById(R.id.totalSMSLabel);
        totalMessagesLabel = (TextView) findViewById(R.id.totalMessagesLabel);
        totalEmailsReceivedLabel = (TextView) findViewById(R.id.totalEmailReceivedLabel);
        serviceLabel = (TextView) findViewById(R.id.serviceLabel);
        serviceLabel.setBackgroundColor(Color.RED);
    	updateLabel();

        boolean checking = sharedPreferences.getBoolean(getString(R.string.isThisFirstTime), true);
        Log.d("nibbler", "onCreate checking=" + checking);
        if (checking) {
            setStandardSettings();
            sharedPreferences.edit().putBoolean(getString(R.string.isThisFirstTime), false).apply();
        }
    }

    public void setStandardSettings(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //general settings defaults value
        editor.putBoolean(getString(R.string.automaticallyStartWithOS), false);
        editor.putBoolean(getString(R.string.writeLogFileToSD), true);
        editor.putBoolean(getString(R.string.deleteLogOlderThanDays), true);
        editor.putInt(getString(R.string.deleteLogOlderValue), 60);
        editor.putInt(getString(R.string.maxSymbolsInSMS), 350);
        //pop3 settings default value
        editor.putBoolean(getString(R.string.sendLogViaMail), true);
        editor.putBoolean(getString(R.string.smtpAuthentication), true);
        editor.putBoolean(getString(R.string.useSSL), true);
        editor.putString(getString(R.string.emailFolderName), "inbox");
        editor.putInt(getString(R.string.checkingInterval), 60);

        editor.apply();
    }
    
    public void onClick(View view){
    	Log.d("nibbler", "onClick");
        if(!isServiceRun) {
            String toLogFile = timeStamp() + "Starting service\r\n";
            writeToLog(toLogFile);

            Toast.makeText(MainActivity.this, "Сервис запущен", Toast.LENGTH_LONG).show();
            scheduledExecutorService = Executors.newScheduledThreadPool(1);
            scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            checkEmail();
                        }
                    });
                }
            }, 5, 60, TimeUnit.SECONDS);

            startServiceButton.setText("Остановить");
            serviceLabel.setText("Сервис выполняется");
            serviceLabel.setBackgroundColor(Color.GREEN);
            isServiceRun = true;
        } else {
            startServiceButton.setText("Запустить");
            serviceLabel.setText("Сервис остановлен");
            serviceLabel.setBackgroundColor(Color.RED);
            scheduledFuture.cancel(false);
            isServiceRun = false;
        }
    }
    
    public boolean isExternalStorageWritable(){
    	String state = Environment.getExternalStorageState();
    	if (Environment.MEDIA_MOUNTED.equals(state)){
    		return true;
    	}
    	return false;
    }
    
    public void checkEmail(){
    	PerformCheck performCheck = new PerformCheck();
    	performCheck.execute();
    	String toLogFile = "";
    	boolean isAnythingFinded = false;
    	try {
			String[] messages = performCheck.get();
			Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, "Total messages found: " + messages.length/2, Toast.LENGTH_LONG);
			toast.show();
			
			for (int i = 0; i < messages.length; i += 2){
				if (messages[i] != null) {
					isAnythingFinded = true;
					Log.d("nibbler", "---message " + i/2 + " subject: " + messages[i] + " content: " + messages[i+1]);
					try{
						toLogFile += (timeStamp() + "Message to;" + messages[i] + ";with content;" + messages[i+1]);
			        	SmsManager smsManager = SmsManager.getDefault();
			        	ArrayList<String> msgArray = smsManager.divideMessage(messages[i+1]);
				    	smsManager.sendMultipartTextMessage(messages[i], null, msgArray, null, null);
				    	smsCounter += msgArray.size();
				    	messagesCounter += 1;
				    	Toast.makeText(getApplicationContext(), "SMS sent and consist of " + msgArray.size(), Toast.LENGTH_LONG).show();
				    	toLogFile += (";was sent, parts spent;" + msgArray.size() + "\r\n");
			    	} catch (Exception ex) {
			    		Log.d("nibbler", "SmsManager Exception");
			    		Toast.makeText(getApplicationContext(), "Message could't be send", Toast.LENGTH_LONG).show();
				    	toLogFile += (timeStamp() + "message could't be send;SmsManager Exception\r\n");
			    		ex.printStackTrace();
			    	}
				}
			}
    		if (isAnythingFinded){
	    		toLogFile += timeStamp() + "Statistics;smsCounter: " + smsCounter.toString() + ", messagesCounter: " + messagesCounter.toString() + ", emailsCounter: " + emailsCounter.toString() + "\r\n";
		    	writeToLog(toLogFile);
    		}
		} catch (InterruptedException e) {
			Log.d("nibbler", "MESSAGES CATCH InterruptedException");
    		Toast.makeText(getApplicationContext(), "MESSAGES CATCH InterruptedException", Toast.LENGTH_LONG).show();
    		toLogFile += timeStamp() + "MESSAGES CATCH InterruptedException\r\n";
    		writeToLog(toLogFile);
			e.printStackTrace();
		} catch (ExecutionException e) {
			Log.d("nibbler", "MESSAGES CATCH ExecutionException");
    		Toast.makeText(getApplicationContext(), "MESSAGES CATCH ExecutionException", Toast.LENGTH_LONG).show();
    		toLogFile += timeStamp() + "MESSAGES CATCH ExecutionException\r\n";
    		writeToLog(toLogFile);
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.d("nibbler", "MESSAGES CATCH NullPointerException");
    		Toast.makeText(getApplicationContext(), "There is no new emails", Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
    	updateLabel();
    }
    
    public void writeToLog(String info){
    	if (isExternalStorageWritable()) {
    		if (info.endsWith("\r\n")){
    			info = info.substring(0, info.length()-1);
    		}
			File root = android.os.Environment.getExternalStorageDirectory();
			File dir = new File(root.getAbsolutePath() + "/Email2SMS");
			if (!dir.exists()){
				dir.mkdirs();
			}
			File file = new File(dir, "/Log.csv");
			
			try {
				FileOutputStream f = new FileOutputStream(file, true);
				/*PrintWriter pw = new PrintWriter(f);
				pw.println(info);
				pw.flush();
				pw.close();*/
				
				OutputStreamWriter os = new OutputStreamWriter(f, "CP1251");
				os.write(info);
				os.flush();
				os.close();
				
				
				f.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.d("nibbler", "FileNotFoundException");
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("nibbler", "IOException");
			} catch (IllegalArgumentException e){
				Log.d("nibbler", "IllegalArgumentException");
			}
			
		} else {
			Log.d("nibbler", "isExternalStorageWritable = false");
		}
    }
    
    public String timeStamp(){
		Calendar c = Calendar.getInstance();
		return c.getTime().toString() + ";";
    }
    
    public void updateLabel(){
    	totalSmsLabel.setText(smsCounter.toString());
    	totalMessagesLabel.setText(messagesCounter.toString());
    	totalEmailsReceivedLabel.setText(emailsCounter.toString());
    }
    
    public class SendLogFile extends AsyncTask<javax.mail.Address, Void, Void>{
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
				messageBodyPart.setText(timeStamp());
				Multipart multipart = new MimeMultipart();
				multipart.addBodyPart(messageBodyPart);
				messageBodyPart = new MimeBodyPart();
				File root = android.os.Environment.getExternalStorageDirectory();
				File file = new File(root.getAbsolutePath() + "/Email2SMS/Log.csv");
				DataSource source = new FileDataSource(file);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName("Log.csv");
				multipart.addBodyPart(messageBodyPart);
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
						emailsCounter += messagesTotal;

						for (int i = 0; i < messagesTotal; i++) {
							try {
								Object content = arrayOfMessage[i].getContent();
								String contentType = arrayOfMessage[i].getContentType().toLowerCase();
								String subject = arrayOfMessage[i].getSubject().toLowerCase();

								if (subject.contains("log")){
									SendLogFile sendLog = new SendLogFile();
									sendLog.execute(arrayOfMessage[i].getFrom());
								}
								
								toLogFile += timeStamp() + "Email received with subject;" + subject;
								
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
										toLogFile += ";with content;" + messagesToSMS[i*2+1];
										if (messagesToSMS[i*2+1].length() > 350) {
											messagesToSMS[i*2+1] = messagesToSMS[i*2+1].substring(0, 350);
										}
									}
								}
								toLogFile += "\r\n";
								arrayOfMessage[i].setFlag(Flags.Flag.DELETED, true);
							} catch (MessagingException ex) {
								Log.d("nibbler", "ME");
								toLogFile += timeStamp() + "MessagingException ME\r\n";
							} catch (Exception ex) {
								Log.d("nibbler", "EE");
								toLogFile += timeStamp() + "Exception EE\r\n";
							}
						}
						localFolder.close(true);
						localStore.close();
						if (messagesTotal > 0) {
							writeToLog(toLogFile);
							Log.d("nibbler", "messagesTotal: " + messagesTotal);
					    	toLogFile = timeStamp() + "Statistics;smsCounter: " + smsCounter.toString() + ", messagesCounter: " + messagesCounter.toString() + ", emailsCounter: " + emailsCounter.toString() + "\r\n";
					    	writeToLog(toLogFile);
						}
						return messagesToSMS;
					}
				} else {
					localStore.close();
				}
			} catch (Exception localException) {
				localException.printStackTrace();
				Log.d("nibbler", "EXCEPTION");
				toLogFile += timeStamp() + "Exception localException\r\n";
				writeToLog(toLogFile);
			}
			return null;
		}
	}


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void settingsActionBarClicked(MenuItem item){
        Log.d("nibbler", "settingsActionBarClicked " + startServiceButton.getText().toString());

        if (startServiceButton.getText().toString().equalsIgnoreCase("Остановить")){
            DialogInterface.OnClickListener dialogClickListenerInfo = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Настройки можно изменять только когда сервис остановлен.").setPositiveButton("Ок", dialogClickListenerInfo).show();
            return;
        }


        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void aboutActionBarClicked(MenuItem item){
        Log.d("nibbler", "aboutActionBarClicked");

        DialogInterface.OnClickListener dialogClickListenerInfo = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Приложение периодически проверяет указанный в настройках электронный ящик, в случае если письмо удовлетворяет требованиям настроек, то сообщение пересылается получателю через СМС.\n\nАвтор: Грашкин Павел\nnn-admin@nnkk.budzdorov.su\n2015 Нижний Новгород\nv1.0beta\n\nВозможности:\n" +
                "-Получение писем через POP3 протокол\n" +
                "-Возможность использовать SSL\n" +
                "-Номера телефонов получателей СМС задаются на основе кодового слова в теме письма\n" +
                "-Часть символов (ненужных) можно исключить из СМС сообщения\n" +
                "-Игнорирование письма, если оно содержит определенную строку\n" +
                "-Возможность работы по заданному расписанию\n" +
                "-Ведение лог-файла в формате CSV для выгрузки в Excel\n" +
                "-Лог файл может быть выслан по почте\n" +
                "-Изменение время опроса почтового сервера.").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }

    public void logViewActionBarClicked(MenuItem item){
        Log.d("nibbler", "logViewActionBarClicked");
        Intent intent = new Intent(this, LogViewActivity.class);
        startActivity(intent);
    }

    public void helpActionBarClicked(MenuItem item){
        Log.d("nibbler", "helpActionBarClicked");

        DialogInterface.OnClickListener dialogClickListenerInfo = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("При первом запуске приложения необходимо настроить параметры почтового ящика из которого нужно забирать письма. Для корректной работы приложения требуется наличие выхода в сеть Интернет и рабочая сим-карта с возможностью отправки СМС. Для обеспечения бесперебойной работы необходимо включить режим разработчика (в настройках телефона в разделе о телефоне семь раз кликнуть на версию) и в нем поставить галочку \"Не выключать экран при подключенном питании\".").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }

}
