package com.example.romanwisdom.rwatch;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Set;

@TargetApi(21)
public class MainActivity extends AppCompatActivity{

    public final String TAG = "Main";

    private Bluetooth bt;
    TextView status;

    public static final int TRANSACTION_SET_TIME = 0;
    public static final int TRANSACTION_SMS = 1;
    SMSBroadcastReceiver smsBR = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.statusText)).setText("Not connected.");
        setupBluetooth();

        smsBR = new SMSBroadcastReceiver();

    }

    public void onBTSendMessageButtonTap(View v){
        Calendar c = Calendar.getInstance();

        String dateTime = "0," +
                String.valueOf(c.get(Calendar.MONTH)+1) + "," +
                String.valueOf(c.get(Calendar.DAY_OF_MONTH)) + "," +
                String.valueOf(c.get(Calendar.YEAR)) + "," +
                String.valueOf(c.get(Calendar.DAY_OF_WEEK)) + "," +
                String.valueOf(c.get(Calendar.AM_PM)) + "," +
                String.valueOf(c.get(Calendar.HOUR)) + "," +
                String.valueOf(c.get(Calendar.MINUTE)) + "," +
                String.valueOf(c.get(Calendar.SECOND));

        bt.sendMessage(dateTime);

        SystemClock.sleep(2000);

        BatteryManager bm = (BatteryManager)getSystemService(BATTERY_SERVICE);
        int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        ((TextView)findViewById(R.id.notificationTextView)).setText("1," + batLevel);

        bt.sendMessage("1," + batLevel);
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

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Bluetooth.MESSAGE_STATE_CHANGE:
                    Log.d(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
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
}