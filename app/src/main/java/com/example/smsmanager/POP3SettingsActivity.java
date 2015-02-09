package com.example.smsmanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class POP3SettingsActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop3settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler","POP3SettingsActivity activity onCreate");
    }
}
