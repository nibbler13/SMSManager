package com.example.smsmanager;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by nn-admin on 30.01.2015.
 */
public class LogViewActivity extends Activity{

    TextView logTextView;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logview_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "This is LogView onCreate");

        logTextView = (TextView)findViewById(R.id.logViewTextView);
        logTextView.setMovementMethod(new ScrollingMovementMethod());
    }
}
