package com.example.chaudfroid;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
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
import java.util.Queue;

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
        public ScanBinder(){
            super();
            msg= new String();
            scanResults= new ArrayList<>();
        }
        public String msg;

        public ArrayList<ScanResult> scanResults;

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
        if (peripheralAddresses != null) {
            filters = new ArrayList<>();
            /*ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceAddress("BE:AC:10:00:00:02")
                    .build();
            filters.add(filter);*/
            for (String address : peripheralAddresses) {
                if (BluetoothAdapter.checkBluetoothAddress(address)) {
                    ScanFilter filter = new ScanFilter.Builder()
                            .setDeviceAddress(address)
                            .build();
                    filters.add(filter);
                }else{
                    filters=null;
                }
            }
        }
        ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                if(scanBinder.scanResults.size()==5){
                    scanBinder.scanResults.remove(4);
                }
                scanBinder.scanResults.add(0,result);

            }
        };
        //bluetoothAdapter.getBluetoothLeScanner().startScan(null, settings, scanCallback);
        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        BluetoothLeScanner x=bluetoothAdapter.getBluetoothLeScanner();
        Log.i("break",x.toString());
    }
}