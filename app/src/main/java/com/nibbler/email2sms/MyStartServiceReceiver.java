package com.nibbler.email2sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by NIBBLER on 17/02/15.
 */
public class MyStartServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        Intent service = new Intent(context, BackgroundEmailCheck.class);
        context.startService(service);
    }
}
