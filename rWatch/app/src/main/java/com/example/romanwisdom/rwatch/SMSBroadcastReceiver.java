package com.example.romanwisdom.rwatch;

/**
 * Created by Roman on 2/10/2017.
 */

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

@TargetApi(23)
public class SMSBroadcastReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "SMSBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Intent recieved: " + intent.getAction());
        Log.i(TAG, "Context: " + context.toString());
        //Toast.makeText(context, intent.getAction().toString(), Toast.LENGTH_LONG).show();

        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        SmsMessage smsMessage = msgs[0];

        Intent i = new Intent("smsBroadcast");
        i.putExtra("smsMessageBody", smsMessage.getMessageBody());
        i.putExtra("smsSender", smsMessage.getOriginatingAddress());

        context.sendBroadcast(i);
    }
}
