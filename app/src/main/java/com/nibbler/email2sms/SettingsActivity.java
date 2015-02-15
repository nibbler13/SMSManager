package com.nibbler.email2sms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Created by nn-admin on 30.01.2015.
 */
public class SettingsActivity extends Activity{
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "This is Settings onCreate");
    }

    public void pop3SettingsOnClick(View view){
        Log.d("nibbler", "pop3SettingsOnClick");
        Intent intent = new Intent(this, POP3SettingsActivity.class);
        startActivity(intent);
    }

    public void addressesListOnClick(View view){
        Log.d("nibbler", "addressesListOnClick");
        Intent intent = new Intent(this, AddressesListActivity.class);
        startActivity(intent);
    }

    public void excludedListOnClick(View view){
        Log.d("nibbler", "excludedListOnClick");
        Intent intent = new Intent(this, ExcludedListActivity.class);
        startActivity(intent);
    }

    public void timetableOnClick(View view){
        Log.d("nibbler", "timetableOnClick");
        Intent intent = new Intent(this, TimetableActivity.class);
        startActivity(intent);
    }

    public void generalSettingsOnClick(View view){
        Log.d("nibbler", "generalSettingsOnClick");
        Intent intent = new Intent(this, GeneralSettingsActivity.class);
        startActivity(intent);
    }

    public void unreadSymbolsButtonOnClick(View view){
        Log.d("nibbler", "unreadSymbolsButtonOnClick");
        Intent intent = new Intent(this, UnreadSymbolsActivity.class);
        startActivity(intent);
    }
}
