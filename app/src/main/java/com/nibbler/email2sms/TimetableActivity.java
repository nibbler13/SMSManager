package com.nibbler.email2sms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Arrays;

/**
 * Created by NIBBLER on 07/02/15.
 */
public class TimetableActivity extends Activity {

    final Context context = this;
    final static int numberOfWeekDays = 7;
    SharedPreferences sharedPreferences;
    TimeTableElement[] timetable;
    EditText[] startsFromArray;
    EditText[] endToArray;
    CheckBox[] checkBoxesArray;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timetable_layout);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("nibbler", "TimetableActivity activity onCreate");

        startsFromArray = new EditText[]{
                (EditText)findViewById(R.id.mondayStartEditText),
                (EditText)findViewById(R.id.tuesdayStartEditText),
                (EditText)findViewById(R.id.wednesdayStartEditText),
                (EditText)findViewById(R.id.thursdayStartEditText),
                (EditText)findViewById(R.id.fridayStartEditText),
                (EditText)findViewById(R.id.saturdayStartEditText),
                (EditText)findViewById(R.id.sundayStartEditText)
        };

        endToArray = new EditText[]{
                (EditText)findViewById(R.id.mondayEndEditText),
                (EditText)findViewById(R.id.tuesdayEndEditText),
                (EditText)findViewById(R.id.wednesdayEndEditText),
                (EditText)findViewById(R.id.thursdayEndEditText),
                (EditText)findViewById(R.id.fridayEndEditText),
                (EditText)findViewById(R.id.saturdayEndEditText),
                (EditText)findViewById(R.id.sundayEndEditText)
        };

        checkBoxesArray = new CheckBox[]{
                (CheckBox)findViewById(R.id.mondayCheckBox),
                (CheckBox)findViewById(R.id.tuesdayCheckBox),
                (CheckBox)findViewById(R.id.wednesdayCheckBox),
                (CheckBox)findViewById(R.id.thursdayCheckBox),
                (CheckBox)findViewById(R.id.fridayCheckBox),
                (CheckBox)findViewById(R.id.saturdayCheckBox),
                (CheckBox)findViewById(R.id.sundayCheckBox),
        };

        sharedPreferences = context.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);
        timetable = new TimeTableElement[]{
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement(),
                new TimeTableElement()};

        for (int i = 0; i < numberOfWeekDays; i++){
            timetable[i].setHour_end(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_hour_end", 23));
            timetable[i].setHour_start(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_hour_start", 0));
            timetable[i].setMinute_end(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_minute_end", 59));
            timetable[i].setMinute_start(sharedPreferences.getInt(TimeTableElement.weekDays[i] + "_minute_start", 0));
            timetable[i].setEnable(sharedPreferences.getBoolean(TimeTableElement.weekDays[i] + "_checkbox", true));

            checkBoxesArray[i].setChecked(timetable[i].getEnable());
            configureEditTextLine(i, timetable[i].getEnable());
        }
    }

    private void configureEditTextLine(int position, boolean status){
        startsFromArray[position].setText(timetable[position].getStartString());
        endToArray[position].setText(timetable[position].getEndString());
        startsFromArray[position].setEnabled(status);
        endToArray[position].setEnabled(status);
        startsFromArray[position].setTextColor(Color.rgb(150 * ((status) ? 0 : 1), 150 * ((status) ? 0 : 1), 150 * ((status) ? 0 : 1)));
        endToArray[position].setTextColor(Color.rgb(150 * ((status) ? 0 : 1), 150 * ((status) ? 0 : 1), 150 * ((status) ? 0 : 1)));
        if (timetable[position].getHour_start() > timetable[position].getHour_end() ||
                timetable[position].getHour_start() == timetable[position].getHour_end() &&
                timetable[position].getMinute_start() > timetable[position].getMinute_end()) {
            startsFromArray[position].setBackgroundColor(Color.RED);
            endToArray[position].setBackgroundColor(Color.RED);
        } else {
            startsFromArray[position].setBackgroundColor(android.R.drawable.editbox_background);
            endToArray[position].setBackgroundColor(android.R.drawable.editbox_background);
        }
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

    public void showStartTimePicker(View view){
        final int i = Arrays.asList(startsFromArray).indexOf((EditText)view);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timetable[i].setHour_start(hourOfDay);
                timetable[i].setMinute_start(minute);
                configureEditTextLine(i, checkBoxesArray[i].isChecked());
            }
        }, timetable[i].getHour_start(), timetable[i].getMinute_start(), true);
        timePickerDialog.show();
    }

    public void showEndTimePicker(View view){
        final int i = Arrays.asList(endToArray).indexOf((EditText)view);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                timetable[i].setHour_end(hourOfDay);
                timetable[i].setMinute_end(minute);
                configureEditTextLine(i, checkBoxesArray[i].isChecked());
            }
        }, timetable[i].getHour_end(), timetable[i].getMinute_end(), true);
        timePickerDialog.show();
    }

    public void checkboxClicked(View view){
        int i = Arrays.asList(checkBoxesArray).indexOf((CheckBox)view);
        configureEditTextLine(i, ((CheckBox) view).isChecked());
    }

    public void onPause(){
        super.onPause();
        Log.d("nibbler", "timetable onPause");

        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (int i = 0; i < numberOfWeekDays; i++){
            editor.putBoolean(TimeTableElement.weekDays[i] + "_checkbox", checkBoxesArray[i].isChecked());
            editor.putInt(TimeTableElement.weekDays[i] + "_hour_start", timetable[i].getHour_start());
            editor.putInt(TimeTableElement.weekDays[i] + "_hour_end", timetable[i].getHour_end());
            editor.putInt(TimeTableElement.weekDays[i] + "_minute_start", timetable[i].getMinute_start());
            editor.putInt(TimeTableElement.weekDays[i] + "_minute_end", timetable[i].getMinute_end());
        }
        editor.apply();
    }

}


//([01]?[0-9]|2[0-3]):[0-5][0-9]
//regex to match 24-time