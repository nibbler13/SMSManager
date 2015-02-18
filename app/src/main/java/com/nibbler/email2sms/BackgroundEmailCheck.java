package com.nibbler.email2sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by NIBBLER on 16/02/15.
 */
public class BackgroundEmailCheck extends Service {

    private NotificationManager mNM;
    private int NOTIFICATION = 76667;
    ScheduledExecutorService scheduledExecutorService;
    ScheduledFuture<?> scheduledFuture;
    SharedPreferences sharedPreferences;

    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        Toast.makeText(this, "Congrats! My Service Created", Toast.LENGTH_LONG).show();
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        Log.d("nibbler", "BackgroundEmailCheck onCreate");

        sharedPreferences = this.getSharedPreferences(getString(R.string.sharedSettingsName), MODE_PRIVATE);
        long REPEAT_TIME = sharedPreferences.getInt(this.getString(R.string.checkingInterval), 60);
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkEmail();
            }
        }, 10, REPEAT_TIME, TimeUnit.SECONDS);
    }

    private void checkEmail(){
        Log.d("nibbler", "background service checkEmail");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID){
        Toast.makeText(this, "My service started", Toast.LENGTH_LONG).show();
        Log.d("nibbler", "BackgroundEmailCheck onStart");
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        Toast.makeText(this, "My service stopped", Toast.LENGTH_LONG).show();
        mNM.cancel(NOTIFICATION);
        scheduledFuture.cancel(false);
        Log.d("nibbler", "BackgroundEmailCheck onDestroy");
    }

    private void showNotification(){
        CharSequence text = "Email2SMS сервис запущен";

        @SuppressWarnings("deprecation")
        Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.setLatestEventInfo(this, "Нажмите, чтобы перейти к приложению", text, contentIntent);
        mNM.notify(NOTIFICATION, notification);
    }
}
