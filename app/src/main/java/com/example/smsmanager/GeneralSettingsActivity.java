package com.example.smsmanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by NIBBLER on 08/02/15.
 */
public class GeneralSettingsActivity extends Activity{
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "GeneralSettingsActivity activity onCreate");
    }
}
