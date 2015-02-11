package com.example.smsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class TimetableActivity extends Activity {

    final Context context = this;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "TimetableActivity activity onCreate");
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
        builder.setMessage("В этом окне можно настроить расписание по которому будут обрабатываться сообщения, поступающие на почтовый ящик.").setPositiveButton("Ок", dialogClickListenerInfo).show();
    }
}


//([01]?[0-9]|2[0-3]):[0-5][0-9]
//regex to match 24-time