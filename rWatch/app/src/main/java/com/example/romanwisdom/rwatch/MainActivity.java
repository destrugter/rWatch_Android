package com.example.romanwisdom.rwatch;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;

@TargetApi(23)
public class MainActivity extends AppCompatActivity{

    public final String TAG = "Main";

    private Bluetooth bt;
    TextView status;

    public static final int TRANSACTION_SET_MONTH = 0;
    public static final int TRANSACTION_SET_DAY = 1;
    public static final int TRANSACTION_SET_YEAR = 2;
    public static final int TRANSACTION_SET_DAYOFWEEK = 3;
    public static final int TRANSACTION_SET_AMPM = 4;
    public static final int TRANSACTION_SET_HOUR = 5;
    public static final int TRANSACTION_SET_MINUTE = 6;
    public static final int TRANSACTION_SET_SECOND = 7;
    public static final int TRANSACTION_SET_BATTERY = 8;
    public static final int TRANSACTION_SET_WEATHER = 9;
    public static final int TRANSACTION_SEND_NOTIFICATION = 98;
    public static final int TRANSACTION_SEND_SMS = 99;

    // Bluetooth Connection Codes
    public static final int BT_STATE_NONE = 0; // we're doing nothing
    public static final int BT_STATE_LISTEN = 1; // now listening for incoming connections
    public static final int BT_STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int BT_STATE_CONNECTED = 3; // now connected to a remote device

    public static final int SMS_RECEIVE_PERMISSION = 0;

    private static MainActivity inst;

    public static MainActivity instance(){
        return inst;
    }

    public static CountDownTimer countdownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handleSMSPermissions();
        setupBluetooth();
        setupTimers();
        setupReceivers();
        //sendInitialData();
    }

    public void onBTSendMessageButtonTap(View v){
        ((TextView)findViewById(R.id.notificationTextView)).setText(Integer.toString(bt.getState()));
    }

    public void sendInitialData(){
        if(bt.getState() == BT_STATE_CONNECTED) {
            Calendar c = Calendar.getInstance();
            String month = String.valueOf(c.get(Calendar.MONTH) + 1);
            String dayOfMonth = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
            String year = String.valueOf(c.get(Calendar.YEAR));
            String dayOfWeek = String.valueOf(c.get(Calendar.DAY_OF_WEEK));
            String amPm = String.valueOf(c.get(Calendar.AM_PM));
            String hour = String.valueOf(c.get(Calendar.HOUR));
            String minute = String.valueOf(c.get(Calendar.MINUTE));
            String second = String.valueOf(c.get(Calendar.SECOND));
            String batLevel = Integer.toString(((BatteryManager) getSystemService(BATTERY_SERVICE)).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY));

            String initialData =
                    TextUtils.join(",", new String[]{
                            Integer.toString(TRANSACTION_SET_MONTH), Integer.toString(month.length()), month,
                            Integer.toString(TRANSACTION_SET_DAY), Integer.toString(dayOfMonth.length()), dayOfMonth,
                            Integer.toString(TRANSACTION_SET_YEAR), Integer.toString(year.length()), year,
                            Integer.toString(TRANSACTION_SET_DAYOFWEEK), Integer.toString(dayOfWeek.length()), dayOfWeek,
                            Integer.toString(TRANSACTION_SET_AMPM), Integer.toString(amPm.length()), amPm,
                            Integer.toString(TRANSACTION_SET_HOUR), Integer.toString(hour.length()), hour,
                            Integer.toString(TRANSACTION_SET_MINUTE), Integer.toString(minute.length()), minute,
                            Integer.toString(TRANSACTION_SET_SECOND), Integer.toString(second.length()), second,
                            Integer.toString(TRANSACTION_SET_BATTERY), Integer.toString(batLevel.length()), batLevel,
                    });

            bt.sendMessage(initialData);
        }
    }

    public void setupBluetooth() {
        bt = new Bluetooth(this, mHandler);

        try {
            status = (TextView)findViewById(R.id.statusText);
            status.setText("Connecting...");
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                bt.start();
                bt.connectDevice("HC-06");
                Log.d(TAG, "Btservice started - listening");
                status.setText("Connected");
            } else {
                Log.w(TAG, "Btservice started - bluetooth is not enabled");
                status.setText("Bluetooth Not enabled");
            }
        } catch(Exception e){
            Log.e(TAG, "Unable to start bt ",e);
            status.setText("Unable to connect " +e);
        }
    }

    public void setupTimers(){
        countdownTimer = new CountDownTimer(900000, 1000){
            public void onTick(long millisecondsUntilFinished){
                //Log.d(TAG, "Seconds left: " + millisecondsUntilFinished / 1000);
            }

            public void onFinish() {
                Log.d(TAG, "Battery Level: " + Integer.toString(((BatteryManager) getSystemService(BATTERY_SERVICE)).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)));
                //countdownTimer.start();
            }
        }.start();
    }

    public void setupReceivers(){
        registerReceiver(broadcastReceiver, new IntentFilter("smsBroadcast"));
        registerReceiver(broadcastReceiver, new IntentFilter("notification"));
        registerReceiver(broadcastReceiver, new IntentFilter("bluetoothDisconnectedBroadcast"));
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    TextView statusTextView = ((TextView) findViewById(R.id.statusText));

                    switch(msg.arg1){
                        case BT_STATE_LISTEN:
                            statusTextView.setText("Disconnected");
                            break;
                        case BT_STATE_CONNECTING:
                            statusTextView.setText("Connecting...");
                            break;
                        case BT_STATE_CONNECTED:
                            statusTextView.setText("Connected");
                            break;
                    }
                    break;
                case Bluetooth.MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE ");
                    break;
                case Bluetooth.MESSAGE_READ:
                    Log.d(TAG, "MESSAGE_READ ");
                    break;
                case Bluetooth.MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MESSAGE_DEVICE_NAME "+msg);
                    break;
                case Bluetooth.MESSAGE_TOAST:
                    Log.d(TAG, "MESSAGE_TOAST "+msg);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SMS_RECEIVE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(this, "SMS permission granted", Toast.LENGTH_LONG).show();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "SMS permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String btMessage = "";
            //((TextView)findViewById(R.id.notificationTextView)).setText(intent.getAction());

            if (intent.getAction().toString().equals("smsBroadcast")) {
                if (bt.getState() == BT_STATE_CONNECTED) {
                    Bundle b = intent.getExtras();
                    String messageBody = b.getString("smsMessageBody");
                    String messageSender = b.getString("smsSender");
                    String messageFull = messageSender + ":" + messageBody;

                    btMessage = TextUtils.join(",", new String[]{
                            Integer.toString(TRANSACTION_SEND_SMS), Integer.toString(messageFull.length()), messageFull
                    });
                }
            } else if (intent.getAction().toString().equals("notification")) {
                if (bt.getState() == BT_STATE_CONNECTED) {
                    Bundle b = intent.getExtras();

                    if (!b.getString("package").equals("com.android.mms")) {
                        String messageFull = b.getString("ticker");

                        btMessage = TextUtils.join(",", new String[]{
                                Integer.toString(TRANSACTION_SEND_NOTIFICATION), Integer.toString(messageFull.length()), messageFull
                        });
                    }
                }
            } else if (intent.getAction().toString().equals("bluetoothDisconnectedBroadcast")) {
                ((TextView)findViewById(R.id.statusText)).setText("Disconnected");
                //setupBluetooth();
            }

            if (btMessage != "") {
                //bt.sendMessage(btMessage);
                ((TextView)findViewById(R.id.notificationTextView)).setText(btMessage);
            }
        }
    };

    public void handleSMSPermissions(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.RECEIVE_SMS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.RECEIVE_SMS},
                        SMS_RECEIVE_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    public void btConnectButtonClick(){
        if (bt.getState() == BT_STATE_LISTEN){
            setupBluetooth();
        }
    }
}
