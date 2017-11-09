package com.devunitec.standarbluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ListDeviceBluetoothActivity extends AppCompatActivity {

    private static final String LOG_TAG = ListDeviceBluetoothActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private String EXTRA_DEVICE_ADDRESS = "device_address";
    
    private BluetoothAdapter mBluetoothAdapter;

    private ArrayAdapter<String> mListDeviceArrayAdapter;

    private Button mBtnSearchBt;
    private TextView mTxtStateBt;
    private ListView mListDeviceBt;
    private ProgressBar mProgressBar;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mListDeviceArrayAdapter.add("Name device: " + device.getName() + "\n" + "MAC: " + device.getAddress());

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Log.e(LOG_TAG, String.valueOf(mListDeviceArrayAdapter.getCount()));

                for(int i = 0; i < mListDeviceArrayAdapter.getCount(); i++) {
                    Log.e(LOG_TAG, mListDeviceArrayAdapter.getItem(i));
                }

                mTxtStateBt.setText("Finished...");
                mBtnSearchBt.setText("Search");
                mProgressBar.setVisibility(View.GONE);

                if (!mListDeviceArrayAdapter.isEmpty()) {
                    mListDeviceBt.setAdapter(mListDeviceArrayAdapter);
                } else {
                    mListDeviceArrayAdapter.add("Devices not found");
                    mListDeviceBt.setAdapter(mListDeviceArrayAdapter);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device_bluetooth);
        checkStateBt();

        mTxtStateBt = (TextView) findViewById(R.id.txt_state_bt);
        mTxtStateBt.setText(" ");

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        mListDeviceArrayAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_device);

        mListDeviceBt = (ListView) findViewById(R.id.list_device_bt);

        mBtnSearchBt = (Button) findViewById(R.id.btn_search_bt);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mBtnSearchBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(broadcastReceiver, filter);

                if(!mListDeviceArrayAdapter.isEmpty()) {
                    mListDeviceArrayAdapter.clear();
                    mListDeviceBt.setAdapter(mListDeviceArrayAdapter);
                }

                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                    mListDeviceArrayAdapter.clear();
                    mListDeviceBt.setAdapter(mListDeviceArrayAdapter);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permissionCheck();
                    }

                    if (mBluetoothAdapter.startDiscovery()) {
                        mListDeviceArrayAdapter = new ArrayAdapter<String>(getBaseContext(), R.layout.list_device);
                        mTxtStateBt.setText("Discovery...");
                        mBtnSearchBt.setText("Cancel");
                        mProgressBar.setVisibility(View.VISIBLE);
                    } else {
                        mProgressBar.setVisibility(View.GONE);
                        mTxtStateBt.setText("Error to start discovery device...");
                    }
                }

                filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(broadcastReceiver, filter);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);

    }

    private void checkStateBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableIntentBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntentBt, REQUEST_ENABLE_BT);
            }
        } else {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void permissionCheck() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }
}