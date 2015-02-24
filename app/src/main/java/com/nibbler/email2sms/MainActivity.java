package com.nibbler.email2sms;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends Activity implements OnClickListener {
	
	private Button startServiceButton;
	private TextView totalSmsLabel;
	private TextView totalMessagesLabel;
	private TextView totalEmailsReceivedLabel;
    private Boolean isServiceRun = false;
    private Context context = this;
    private SharedPreferences sharedPreferences;
    private LogFile logFile;

    private SharedPreferences.OnSharedPreferenceChangeListener listener;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	Log.d("nibbler", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logFile = new LogFile(this);
        logFile.writeToLog("Запуск приложения");
        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);
        
        startServiceButton = (Button) findViewById(R.id.sendSMS);
        startServiceButton.setOnClickListener(this);
        
        totalSmsLabel = (TextView) findViewById(R.id.totalSMSLabel);
        totalMessagesLabel = (TextView) findViewById(R.id.totalMessagesLabel);
        totalEmailsReceivedLabel = (TextView) findViewById(R.id.totalEmailReceivedLabel);

        boolean checking = sharedPreferences.getBoolean(getString(R.string.isThisFirstTime), true);
        Log.d("nibbler", "onCreate checking=" + checking);
        if (checking) {
            setStandardSettings();
            sharedPreferences.edit().putBoolean(getString(R.string.isThisFirstTime), false).apply();
        }

        if (isMyServiceRunning(BackgroundEmailCheck.class)){
            setMainButton(true);
        } else {
            setMainButton(false);
        }

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                Log.d("nibbler", "Settings key changed: " + key);
                if (key.equals(getString(R.string.totalMessagesCounter)) ||
                    key.equals(getString(R.string.totalEmailCounter)) ||
                    key.equals(getString(R.string.totalSmsCounter))) {
                    updateLabel();
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);

        updateLabel();
    }

    public void setStandardSettings(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //general settings defaults value
        editor.putBoolean(getString(R.string.automaticallyStartWithOS), true);
        editor.putBoolean(getString(R.string.writeLogFileToSD), true);
        editor.putBoolean(getString(R.string.deleteLogOlderThanDays), true);
        editor.putInt(getString(R.string.deleteLogOlderValue), 3);
        editor.putInt(getString(R.string.maxSymbolsInSMS), 350);
        editor.putString(getString(R.string.encodingLogFile), "CP1251");
        //pop3 settings default value
        editor.putBoolean(getString(R.string.sendLogViaMail), true);
        editor.putBoolean(getString(R.string.smtpAuthentication), true);
        editor.putBoolean(getString(R.string.pop3UseSSL), true);
        editor.putBoolean(getString(R.string.smtpUseSSL), true);
        editor.putString(getString(R.string.emailFolderName), "inbox");
        editor.putInt(getString(R.string.checkingInterval), 60);
        //addresses settings default value
        Set<String> addressesStrings = new HashSet<String>(Arrays.asList(new String[]{
                "Нижний=89601811873",
                "Санкт-петербург=89216516778",
                "Казань=89674659975",
                "Краснодар=89673034433",
                "Уфа=89603848641",
                "Новосибирск=89095327069",
                "Красноярск=89659031324"
        }));
        editor.putStringSet(getString(R.string.addresses_list), addressesStrings).apply();
        //excluded settings default value
        Set<String> excludedStrings = new HashSet<String>(Arrays.asList(new String[]{
                "Увольнение сотрудника из организации!",
                "Прием на работу нового сотрудника!",
                "На всех админов!  Зайти в консоль Nod 32",
                "Коллеги, наступил момент снятия отчетов по печати",
                "Перемещение сотрудника!"
        }));
        editor.putStringSet(getString(R.string.excluded_list), excludedStrings).apply();
        //unread settings default value
        Set<String> unreadableStrings = new HashSet<String>(Arrays.asList(new String[]{
                "<BR>",
                "&lt,",
                "&gt,",
                "<a href=\"mailto:"
        }));
        editor.putStringSet(getString(R.string.unreadable_list), unreadableStrings).apply();
        //timetable settings default value
        int numberOfWeekdays = 7;
        for (int i = 0; i < numberOfWeekdays; i++) {
            editor.putBoolean(TimeTableElement.weekDays[i] + "_checkbox", true);
            editor.putInt(TimeTableElement.weekDays[i] + "_hour_start", 0);
            editor.putInt(TimeTableElement.weekDays[i] + "_hour_end", 23);
            editor.putInt(TimeTableElement.weekDays[i] + "_minute_start", 0);
            editor.putInt(TimeTableElement.weekDays[i] + "_minute_end", 59);
        }
        //set counters to 0
        editor.putInt(getString(R.string.totalEmailCounter), 0);
        editor.putInt(getString(R.string.totalMessagesCounter), 0);
        editor.putInt(getString(R.string.totalSmsCounter), 0);

        editor.apply();
    }
    
    public void onClick(View view){
    	Log.d("nibbler", "mainActivity onClick");
        if(!isServiceRun) {
            logFile.writeToLog("Запуск сервиса");
            setMainButton(true);
            startService(new Intent(this, BackgroundEmailCheck.class));
        } else {
            logFile.writeToLog("Остановка сервиса");
            setMainButton(false);
            stopService(new Intent(this, BackgroundEmailCheck.class));
        }
    }

    private void setMainButton(boolean status){
        if (status) {
            startServiceButton.setText("Остановить");
            isServiceRun = true;
        } else {
            startServiceButton.setText("Запустить");
            isServiceRun = false;
        }
    }
    
    public void updateLabel(){
    	totalSmsLabel.setText(Integer.toString(sharedPreferences.getInt(getString(R.string.totalSmsCounter), 0)));
    	totalMessagesLabel.setText(Integer.toString(sharedPreferences.getInt(getString(R.string.totalMessagesCounter), 0)));
    	totalEmailsReceivedLabel.setText(Integer.toString(sharedPreferences.getInt(getString(R.string.totalEmailCounter), 0)));
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
            builder.setMessage(getString(R.string.mainActivitySettingsStartErrorText)).setPositiveButton("Ок", dialogClickListenerInfo).show();
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
        builder.setMessage(getString(R.string.mainActivityAboutActionBarText)).setPositiveButton("Ок", dialogClickListenerInfo).show();
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
        builder.setMessage(getString(R.string.mainActivityHelpActionBarText)).setPositiveButton("Ок", dialogClickListenerInfo).show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass){
        ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if (serviceClass.getName().equals(service.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    public void onPause() {
        super.onPause();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void onResume() {
        super.onResume();
        updateLabel();
    }
}
