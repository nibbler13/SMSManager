package com.nibbler.email2sms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Created by NIBBLER on 16/02/15.
 */
public class MyScheduleReceiver extends BroadcastReceiver {
    SharedPreferences sharedPreferences;
    @Override
    public void onReceive(Context context, Intent intent){
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.sharedSettingsName), Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(context.getString(R.string.automaticallyStartWithOS), true)) {
            long REPEAT_TIME = 1000 * sharedPreferences.getInt(context.getString(R.string.checkingInterval), 60);
            AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(context, MyStartServiceReceiver.class);
            PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, 10);
            service.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);
        }
    }
}
