package com.example.romanwisdom.rwatch;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Roman on 2/18/2017.
 */
@TargetApi(23)
public class NotificationReceiver extends NotificationListenerService {
    Context context;

    @Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String pack = sbn.getPackageName();
        String ticker = sbn.getNotification().tickerText.toString();
        Bundle extras = sbn.getNotification().extras;
        String title = extras.getString("android.title");
        String text = extras.getCharSequence("android.text").toString();

        Intent i = new Intent("notification");
        i.putExtra("package", pack);
        i.putExtra("ticker", ticker);
        i.putExtra("title", title);
        i.putExtra("text", text);

        context.sendBroadcast(i);
    }
}
