package com.nibbler.email2sms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class POP3SettingsActivity extends Activity {

    EditText pop3ServerNameET;
    EditText pop3ServerPortET;
    EditText pop3LoginET;
    EditText pop3PasswordET;
    CheckBox pop3UseSSLCB;
    CheckBox sendLogFileToMailCB;
    EditText smtpServerNameET;
    EditText smtpPortET;
    CheckBox smtpAuthenticationCB;
    CheckBox smtpUseSSLCB;
    EditText smtpLoginET;
    EditText smtpPasswordET;
    EditText inboxFolderNameET;
    EditText checkIntervalET;
    Button checkConnection;

    String pop3ServerNameString;
    int pop3ServerPortInt;
    String pop3LoginString;
    String pop3PasswordString;
    Boolean pop3UseSSLBool;
    boolean sendLogFileToMailBool;
    String smtpServerNameString;
    int smtpPortInt;
    boolean smtpAuthenticationBool;
    boolean smtpUseSSLBool;
    String smtpLoginString;
    String smtpPasswordString;
    String inboxFolderNameString;
    int checkIntervalInt;

    SharedPreferences sharedPreferences;

    final Context context = this;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop3settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler","POP3SettingsActivity activity onCreate");

        pop3ServerNameET = (EditText)findViewById(R.id.pop3ServerAddressEditText);
        pop3ServerPortET = (EditText)findViewById(R.id.pop3PortNumberEditText);
        pop3LoginET = (EditText)findViewById(R.id.pop3LoginEditText);
        pop3PasswordET = (EditText)findViewById(R.id.pop3PasswordEditText);
        pop3UseSSLCB = (CheckBox)findViewById(R.id.pop3UseSSLCheckBox);
        sendLogFileToMailCB = (CheckBox)findViewById(R.id.logFileSendCheckBox);
        smtpServerNameET = (EditText)findViewById(R.id.smtpServerEditText);
        smtpPortET = (EditText)findViewById(R.id.smtpPortEditText);
        smtpAuthenticationCB = (CheckBox)findViewById(R.id.smtpAuthenticationCheckBox);
        smtpUseSSLCB = (CheckBox)findViewById(R.id.smtpUseSSLCheckBox);
        smtpLoginET = (EditText)findViewById(R.id.smtpLoginEditText);
        smtpPasswordET = (EditText)findViewById(R.id.smtpPasswordEditText);
        inboxFolderNameET = (EditText)findViewById(R.id.inboxFolderEditText);
        checkIntervalET = (EditText)findViewById(R.id.checkingIntervalEditBox);
        checkConnection = (Button)findViewById(R.id.checkConnectionButton);

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);

        pop3ServerNameString = sharedPreferences.getString(getString(R.string.pop3ServerName), "");
        pop3ServerPortInt = sharedPreferences.getInt(getString(R.string.pop3ServerPort), 0);
        pop3LoginString = sharedPreferences.getString(getString(R.string.pop3Login), "");
        pop3PasswordString = sharedPreferences.getString(getString(R.string.pop3Password), "");
        pop3UseSSLBool = sharedPreferences.getBoolean(getString(R.string.pop3UseSSL), true);
        sendLogFileToMailBool = sharedPreferences.getBoolean(getString(R.string.sendLogViaMail), true);
        smtpServerNameString = sharedPreferences.getString(getString(R.string.smtpServerName), "");
        smtpPortInt = sharedPreferences.getInt(getString(R.string.smtpServerPort), 0);
        smtpAuthenticationBool = sharedPreferences.getBoolean(getString(R.string.smtpAuthentication), true);
        smtpUseSSLBool = sharedPreferences.getBoolean(getString(R.string.smtpUseSSL), true);
        smtpLoginString = sharedPreferences.getString(getString(R.string.smtpLogin), "");
        smtpPasswordString = sharedPreferences.getString(getString(R.string.smtpPassword), "");
        inboxFolderNameString = sharedPreferences.getString(getString(R.string.emailFolderName), "inbox");
        checkIntervalInt = sharedPreferences.getInt(getString(R.string.checkingInterval), 60);

        updateWidgets();

        sendLogFileToMailCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeEditTextStatus(isChecked);
            }
        });

        checkConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("nibbler", "checkConnection onClick");
                savePreferences();
                updateWidgets();
                String errors = "";
                if (pop3ServerNameString.equalsIgnoreCase("")) {
                    errors += "Адрес POP3 сервера не может быть пустым\n";
                }
                if (pop3ServerPortInt == 0) {
                    errors += "Порт POP3 сервера не может быть пустым\n";
                }
                if (pop3LoginString.equalsIgnoreCase("")) {
                    errors += "POP3 логин не может быть пустым\n";
                }
                if (pop3PasswordString.equalsIgnoreCase("")) {
                    errors += "POP3 пароль не может быть пустым\n";
                }
                if (sendLogFileToMailBool){
                    if (smtpServerNameString.equalsIgnoreCase("")) {
                        errors += "Адрес SMTP сервера не может быть пустым\n";
                    }
                    if (smtpPortInt == 0) {
                        errors += "Порт SMTP сервера не может быть пустым\n";
                    }
                    if (smtpLoginString.equalsIgnoreCase("")) {
                        errors += "SMTP логин не может быть пустым\n";
                    }
                    if (smtpPasswordString.equalsIgnoreCase("")) {
                        errors += "SMTP пароль не может быть пустым\n";
                    }
                }

                Log.d("nibbler", "errorsString: " + errors);

                if (!errors.equalsIgnoreCase("")) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(errors).setPositiveButton("Устранить ошибки", dialogClickListener).show();
                } else {
                    ///////////
                    //There are no errors, need to make checking for connection
                    ///////////
                }
            }
        });
    }

    public void updateWidgets() {
        pop3ServerNameET.setText(pop3ServerNameString);

        if (pop3ServerPortInt == 0) {
            pop3ServerPortET.setText("");
        } else {
            pop3ServerPortET.setText(Integer.toString(pop3ServerPortInt));
        }

        sendLogFileToMailCB.setChecked(sendLogFileToMailBool);

        changeEditTextStatus(sendLogFileToMailBool);

        smtpServerNameET.setText(smtpServerNameString);

        if (smtpPortInt == 0) {
            smtpPortET.setText("");
        } else {
            smtpPortET.setText(Integer.toString(smtpPortInt));
        }

        smtpAuthenticationCB.setChecked(smtpAuthenticationBool);
        smtpUseSSLCB.setChecked(smtpUseSSLBool);
        smtpLoginET.setText(smtpLoginString);
        smtpPasswordET.setText(smtpPasswordString);
        pop3UseSSLCB.setChecked(pop3UseSSLBool);
        pop3LoginET.setText(pop3LoginString);
        pop3PasswordET.setText(pop3PasswordString);
        inboxFolderNameET.setText(inboxFolderNameString);
        checkIntervalET.setText(Integer.toString(checkIntervalInt));

    }

    public void changeEditTextStatus(boolean isChecked){
        int modifier = (isChecked) ? 0 : 1;
        int color = Color.rgb(150 * modifier, 150 * modifier, 150 * modifier);
        smtpServerNameET.setEnabled(isChecked);
        smtpServerNameET.setTextColor(color);
        smtpPortET.setEnabled(isChecked);
        smtpPortET.setTextColor(color);
        smtpAuthenticationCB.setEnabled(isChecked);
        smtpLoginET.setEnabled(isChecked);
        smtpLoginET.setTextColor(color);
        smtpPasswordET.setEnabled(isChecked);
        smtpPasswordET.setTextColor(color);
        smtpUseSSLCB.setEnabled(isChecked);
    }

    public void onPause() {
        super.onPause();

        savePreferences();
    }

    public void savePreferences() {
        pop3ServerNameString = pop3ServerNameET.getText().toString();

        try {
            pop3ServerPortInt = Integer.parseInt(pop3ServerPortET.getText().toString());
        } catch (NumberFormatException e) {
            pop3ServerPortInt = 0;
        }

        if (pop3ServerPortInt > 65535) {
            pop3ServerPortInt = 0;
        }

        sendLogFileToMailBool = sendLogFileToMailCB.isChecked();
        smtpServerNameString = smtpServerNameET.getText().toString();

        try {
            smtpPortInt = Integer.parseInt(smtpPortET.getText().toString());
        } catch (NumberFormatException e) {
            smtpPortInt = 0;
        }

        if (smtpPortInt > 65535) {
            smtpPortInt = 0;
        }

        smtpAuthenticationBool = smtpAuthenticationCB.isChecked();
        smtpUseSSLBool = smtpUseSSLCB.isChecked();
        smtpLoginString = smtpLoginET.getText().toString();
        smtpPasswordString = smtpPasswordET.getText().toString();

        pop3UseSSLBool = pop3UseSSLCB.isChecked();
        pop3LoginString = pop3LoginET.getText().toString();
        pop3PasswordString = pop3PasswordET.getText().toString();

        inboxFolderNameString = inboxFolderNameET.getText().toString();

        try {
            checkIntervalInt = Integer.parseInt(checkIntervalET.getText().toString());
        } catch (NumberFormatException e) {
            checkIntervalInt = 60;
        }

        if (checkIntervalInt < 10) {
            checkIntervalInt = 10;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.pop3ServerName), pop3ServerNameString);
        editor.putInt(getString(R.string.pop3ServerPort), pop3ServerPortInt);
        editor.putBoolean(getString(R.string.sendLogViaMail), sendLogFileToMailBool);
        editor.putString(getString(R.string.smtpServerName), smtpServerNameString);
        editor.putInt(getString(R.string.smtpServerPort), smtpPortInt);
        editor.putBoolean(getString(R.string.smtpAuthentication), smtpAuthenticationBool);
        editor.putBoolean(getString(R.string.smtpUseSSL), smtpUseSSLBool);
        editor.putString(getString(R.string.smtpLogin), smtpLoginString);
        editor.putString(getString(R.string.smtpPassword), smtpPasswordString);
        editor.putBoolean(getString(R.string.pop3UseSSL), pop3UseSSLBool);
        editor.putString(getString(R.string.pop3Login), pop3LoginString);
        editor.putString(getString(R.string.pop3Password), pop3PasswordString);
        editor.putString(getString(R.string.emailFolderName), inboxFolderNameString);
        editor.putInt(getString(R.string.checkingInterval), checkIntervalInt);
        editor.apply();
    }
}
