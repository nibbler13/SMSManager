package com.example.smsmanager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class TimetableActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "TimetableActivity activity onCreate");
    }
}
