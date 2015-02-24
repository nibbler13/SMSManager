package com.nibbler.email2sms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
        phoneNumberET.setText(sharedPreferences.getString(getString(R.string.notificationPhoneNumber), ""));
        configureAddress(sendEmailIfErrorCB.isChecked());
        configureNumber(sendSMSIfErrorCB.isChecked());
        sendEmailIfErrorCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configureAddress(isChecked);
            }
        });
        sendSMSIfErrorCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                configureNumber(isChecked);
            }
        });
    }

    public void configureAddress(boolean isChecked){
        float modifier = 1.0f - 0.5f * ((isChecked) ? 0 : 1);
        emailAddressET.setEnabled(isChecked);
        emailAddressET.setAlpha(modifier);
        emailAddressTV.setAlpha(modifier);
    }

    public void configureNumber(boolean isChecked){
        float modifier = 1.0f - 0.5f * ((isChecked) ? 0 : 1);
        phoneNumberET.setEnabled(isChecked);
        phoneNumberET.setAlpha(modifier);
        phoneNumberTV.setAlpha(modifier);
    }

    public void onPause(){
        super.onPause();

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.sendMailIfError), sendEmailIfErrorCB.isChecked());
        editor.putString(getString(R.string.notificationEmailAddress), emailAddressET.getText().toString());
        editor.putBoolean(getString(R.string.sendSMSIfError), sendSMSIfErrorCB.isChecked());
        editor.putString(getString(R.string.notificationPhoneNumber), phoneNumberET.getText().toString());
        editor.apply();
    }
}
