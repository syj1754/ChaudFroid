package com.example.chaudfroid;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.lang.Object;

public class SkanService extends Service {
    private ScanBinder scanBinder;
    public SkanService() {
    }
    @Override
    public void onCreate() {
        super.onCreate();
        scanBinder=new ScanBinder();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        String msg[]={message};
        scanForPeripheralsWithAddresses(msg);
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.

        return scanBinder;
    }
    public class ScanBinder extends Binder {
        public String msg;

    }
    /**
     * Scan for peripherals that have any of the specified peripheral mac addresses.
     *
     * @param peripheralAddresses array of peripheral mac addresses to scan for
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void scanForPeripheralsWithAddresses(final String[] peripheralAddresses) {
        BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        List<ScanFilter> filters = null;
        ScanSettings.Builder settingBuilder = new ScanSettings.Builder();
        settingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);;
        ScanSettings settings = settingBuilder.build();
        PendingIntent callbackIntent = PendingIntent.getForegroundService(
                this,
                1,
                new Intent("com.example.chaudfroid").setPackage(getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT );
        if (peripheralAddresses != null) {
            filters = new ArrayList<>();
            for (String address : peripheralAddresses) {
                if (BluetoothAdapter.checkBluetoothAddress(address)) {
                    ScanFilter filter = new ScanFilter.Builder()
                            .setDeviceAddress(address)
                            .build();
                    filters.add(filter);
                }
            }
        }

        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, callbackIntent);
    }
}