package com.example.smsmanager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class POP3SettingsActivity extends Activity {

    EditText pop3ServerNameET;
    EditText pop3ServerPortET;
    CheckBox sendLogFileToMailCB;
    EditText smtpServerNameET;
    EditText smtpPortET;
    CheckBox smtpAuthenticationCB;
    CheckBox useSSLCB;
    EditText loginET;
    EditText passwordET;
    EditText inboxFolderNameET;
    EditText checkIntervalET;
    Button checkConnection;

    String pop3ServerNameString;
    int pop3ServerPortInt;
    boolean sendLogFileToMailBool;
    String smtpServerNameString;
    int smtpPortInt;
    boolean smtpAuthenticationBool;
    boolean useSSLBool;
    String loginString;
    String passwordString;
    String inboxFolderNameString;
    int checkIntervalInt;

    SharedPreferences sharedPreferences;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop3settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler","POP3SettingsActivity activity onCreate");

        pop3ServerNameET = (EditText)findViewById(R.id.pop3ServerAddressEditText);
        pop3ServerPortET = (EditText)findViewById(R.id.pop3PortNumberEditText);
        sendLogFileToMailCB = (CheckBox)findViewById(R.id.logFileSendCheckBox);
        smtpServerNameET = (EditText)findViewById(R.id.smtpServerEditText);
        smtpPortET = (EditText)findViewById(R.id.smtpPortEditText);
        smtpAuthenticationCB = (CheckBox)findViewById(R.id.smtpAuthenticationCheckBox);
        useSSLCB = (CheckBox)findViewById(R.id.useSSLCheckBox);
        loginET = (EditText)findViewById(R.id.loginEditText);
        passwordET = (EditText)findViewById(R.id.passwordEditText);
        inboxFolderNameET = (EditText)findViewById(R.id.inboxFolderEditText);
        checkIntervalET = (EditText)findViewById(R.id.checkingIntervalEditBox);
        checkConnection = (Button)findViewById(R.id.checkConnectionButton);

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);

        pop3ServerNameString = sharedPreferences.getString(getString(R.string.pop3ServerName), "");
        pop3ServerPortInt = sharedPreferences.getInt(getString(R.string.pop3ServerPort), 0);
        sendLogFileToMailBool = sharedPreferences.getBoolean(getString(R.string.sendLogViaMail), true);
        smtpServerNameString = sharedPreferences.getString(getString(R.string.smtpServerName), "");
        smtpPortInt = sharedPreferences.getInt(getString(R.string.smtpServerPort), 0);
        smtpAuthenticationBool = sharedPreferences.getBoolean(getString(R.string.smtpAuthentication), true);
        boolean useSSLBool = sharedPreferences.getBoolean(getString(R.string.useSSL), true);
        loginString = sharedPreferences.getString(getString(R.string.login), "");
        passwordString = sharedPreferences.getString(getString(R.string.password), "");
        inboxFolderNameString = sharedPreferences.getString(getString(R.string.emailFolderName), "inbox");
        checkIntervalInt = sharedPreferences.getInt(getString(R.string.checkingInterval), 60);

        pop3ServerNameET.setText(pop3ServerNameString);

        if (pop3ServerPortInt == 0) {
            pop3ServerPortET.setText("");
        } else {
            pop3ServerPortET.setText(Integer.toString(pop3ServerPortInt));
        }

        sendLogFileToMailCB.setChecked(sendLogFileToMailBool);
        smtpServerNameET.setText(smtpServerNameString);

        if (smtpPortInt == 0) {
            smtpPortET.setText("");
        } else {
            smtpPortET.setText(Integer.toString(smtpPortInt));
        }

        smtpAuthenticationCB.setChecked(smtpAuthenticationBool);
        useSSLCB.setChecked(useSSLBool);
        loginET.setText(loginString);
        passwordET.setText(passwordString);
        inboxFolderNameET.setText(inboxFolderNameString);
        checkIntervalET.setText(Integer.toString(checkIntervalInt));
        checkConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("nibbler", "checkConnection onClick");
            }
        });
    }
}
