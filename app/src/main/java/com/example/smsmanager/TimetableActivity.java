package com.example.smsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class TimetableActivity extends Activity {

    final Context context = this;
    SharedPreferences sharedPreferences;
    timeTableElement[] timetable;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "TimetableActivity activity onCreate");

        sharedPreferences = context.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);
        timetable = new timeTableElement[]{
                new timeTableElement(),
                new timeTableElement(),
                new timeTableElement(),
                new timeTableElement(),
                new timeTableElement(),
                new timeTableElement(),
                new timeTableElement()};

        for (int i = 0; i < 7; i++){
            timetable[i].setHour_end(sharedPreferences.getInt(timeTableElement.weekDays[i] + "_hour_end", 23));
            timetable[i].setHour_start(sharedPreferences.getInt(timeTableElement.weekDays[i] + "_hour_start", 0));
            timetable[i].setMinute_end(sharedPreferences.getInt(timeTableElement.weekDays[i] + "_minute_end", 59));
            timetable[i].setMinute_start(sharedPreferences.getInt(timeTableElement.weekDays[i] + "_minute_start", 0));
            timetable[i].setEnable(sharedPreferences.getBoolean(timeTableElement.weekDays[i] + "_checkbox", true));
        }
    }

    public void updateVisualElements(){

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

    public void showTimePicker(View view){
        Log.d("nibbler", "showTimePicker: " + Integer.toString(view.getId()));


        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Log.d("nibbler", "hour: " + Integer.toString(hourOfDay) + " minute: " + Integer.toString(minute));
            }
        }, 0, 0, false);
        timePickerDialog.show();
    }
}


//([01]?[0-9]|2[0-3]):[0-5][0-9]
//regex to match 24-time