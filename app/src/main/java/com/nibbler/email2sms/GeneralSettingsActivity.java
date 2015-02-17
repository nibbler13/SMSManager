package com.nibbler.email2sms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by NIBBLER on 08/02/15.
 */
public class GeneralSettingsActivity extends Activity{

    SharedPreferences sharedPreferences;
    CheckBox autoStartCB;
    CheckBox writeLogToSDCB;
    CheckBox deleteOlderCB;
    EditText deleteValueET;
    EditText maxSymbolsET;

    Context context = this;

    boolean autoStartBool;
    boolean writeToSDBool;
    boolean deleteOlderBool;
    int deleteOlderValueInt;
    int maxSymbolsInt;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "GeneralSettingsActivity activity onCreate");

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);

        autoStartCB = (CheckBox)findViewById(R.id.startOnBootBox);
        writeLogToSDCB = (CheckBox)findViewById(R.id.writeLogToSDCheckBox);
        deleteOlderCB = (CheckBox)findViewById(R.id.deleteOlderThanCheckBox);
        deleteValueET = (EditText)findViewById(R.id.deleteOlderValueEditText);
        maxSymbolsET = (EditText)findViewById(R.id.maxSymbolsToSendEditText);

        autoStartBool = sharedPreferences.getBoolean(getString(R.string.automaticallyStartWithOS), true);
        writeToSDBool = sharedPreferences.getBoolean(getString(R.string.writeLogFileToSD), true);
        deleteOlderBool = sharedPreferences.getBoolean(getString(R.string.deleteLogOlderThanDays), true);
        deleteOlderValueInt = sharedPreferences.getInt(getString(R.string.deleteLogOlderValue), 60);
        maxSymbolsInt = sharedPreferences.getInt(getString(R.string.maxSymbolsInSMS), 350);

        autoStartCB.setChecked(autoStartBool);
        writeLogToSDCB.setChecked(writeToSDBool);
        deleteOlderCB.setChecked(deleteOlderBool);
        deleteValueET.setText(Integer.toString(deleteOlderValueInt));
        maxSymbolsET.setText(Integer.toString(maxSymbolsInt));

        changeEditTextStatus(deleteOlderBool);

        deleteOlderCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeEditTextStatus(isChecked);
            }
        });
    }

    public void changeEditTextStatus(boolean isChecked){
        if (!isChecked){
            deleteValueET.setEnabled(false);
            deleteValueET.setTextColor(Color.rgb(150, 150, 150));
        } else {
            deleteValueET.setEnabled(true);
            deleteValueET.setTextColor(Color.rgb(0, 0, 0));
        }
    }

    public void onPause(){
        super.onPause();

        autoStartBool = autoStartCB.isChecked();
        writeToSDBool = writeLogToSDCB.isChecked();
        deleteOlderBool = deleteOlderCB.isChecked();

        try {
            deleteOlderValueInt = Integer.parseInt(deleteValueET.getText().toString());
        } catch (NumberFormatException e) {
            deleteOlderValueInt = 60;
        }

        try {
            maxSymbolsInt = Integer.parseInt(maxSymbolsET.getText().toString());
        } catch (NumberFormatException e) {
            maxSymbolsInt = 350;
        }

        if (deleteOlderValueInt < 1) {
            deleteOlderValueInt = 1;
        }

        if (deleteOlderValueInt > 365) {
            deleteOlderValueInt = 365;
        }

        if (maxSymbolsInt < 70) {
            maxSymbolsInt = 70;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.automaticallyStartWithOS), autoStartBool);
        editor.putBoolean(getString(R.string.writeLogFileToSD), writeToSDBool);
        editor.putBoolean(getString(R.string.deleteLogOlderThanDays), deleteOlderBool);
        editor.putInt(getString(R.string.deleteLogOlderValue), deleteOlderValueInt);
        editor.putInt(getString(R.string.maxSymbolsInSMS), maxSymbolsInt);
        editor.apply();

        Log.d("nibbler", "GeneralSettingsActivity onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return true;
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
        builder.setMessage("Одно СМС сообщение на латинице допускает максимум 160 символов, в кириллице максимум 70 символов. При превышении указанных размеров сообщение будет отправлено в виде нескольких СМС и соответственно будет дороже стоить.").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }
}
