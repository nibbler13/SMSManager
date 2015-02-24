package com.nibbler.email2sms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by NIBBLER on 22/02/15.
 */
public class SystemNotificationActivity extends Activity {

    private Context context = this;
    private SharedPreferences sharedPreferences;
    private CheckBox sendEmailIfErrorCB;
    private CheckBox sendSMSIfErrorCB;
    private TextView emailAddressTV;
    private TextView phoneNumberTV;
    private EditText emailAddressET;
    private EditText phoneNumberET;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.system_notification_layout);
        Log.d("nibbler", "SystemNotificationActivity onCreate");

        sharedPreferences = context.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);

        sendEmailIfErrorCB = (CheckBox)findViewById(R.id.sendMailIfErrorCheckBox);
        sendSMSIfErrorCB = (CheckBox)findViewById(R.id.sendSMSIfErrorCheckBox);
        emailAddressTV = (TextView)findViewById(R.id.notificationEmailTextView);
        phoneNumberTV = (TextView)findViewById(R.id.notificationPhoneTextView);
        emailAddressET = (EditText)findViewById(R.id.notificationEmailEditText);
        phoneNumberET = (EditText)findViewById(R.id.notificationPhoneEditText);

        sendEmailIfErrorCB.setChecked(sharedPreferences.getBoolean(getString(R.string.sendMailIfError), false));
        sendSMSIfErrorCB.setChecked(sharedPreferences.getBoolean(getString(R.string.sendSMSIfError), false));
        emailAddressET.setText(sharedPreferences.getString(getString(R.string.notificationEmailAddress), ""));
        phoneNumberET.setText(Integer.toString(sharedPreferences.getInt(getString(R.string.notificationPhoneNumber), 0)));
        if (phoneNumberET.getText().toString().equals("0")) phoneNumberET.setText("");
    }
}
