package com.example.chaudfroid;

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.lang.Object;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class SkanService extends Service {
    private ScanBinder scanBinder;private Context context =this;
    private BluetoothGatt bluetoothGatt;
    private Timer timer;
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt.discoverServices();
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            bluetoothGatt.readRemoteRssi();
                        }
                    }, 500, 500);

                } else {
                    bluetoothGatt.close();
                    bluetoothGatt = scanResult.getDevice().connectGatt(getApplicationContext(), false, gattCallback);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> gattServicesList = bluetoothGatt.getServices();
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            // TODO Auto-generated method stub
            super.onReadRemoteRssi(gatt, rssi, status);
            if (scanBinder.scanResults.size() == 5) {
                scanBinder.scanResults.remove(4);
            }
            scanBinder.scanResults.add(0, rssi);
        }


    };
    private ScanResult scanResult;

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
        Bundle bundle=intent.getExtras();
        String message = bundle.getString("message");
        String msg[]={message};
        scanForPeripheralsWithAddresses(msg);
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        /*if(bluetoothGatt!=null){
            bluetoothGatt.readRemoteRssi();
        }*/
        return scanBinder;
    }
    public class ScanBinder extends Binder {
        public ScanBinder(){
            super();
            msg= new String();
            scanResults= new ArrayList<>();
        }
        public String msg;

        public ArrayList<Integer> scanResults;

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
        settingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        settingBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        settingBuilder.setMatchMode(ScanSettings.MATCH_MODE_STICKY);
        ScanSettings settings = settingBuilder.build();
        if (peripheralAddresses != null) {
            filters = new ArrayList<>();
            /*ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceAddress("BE:AC:10:00:00:02")
                    .build();
            filters.add(filter);*/
            for (String address : peripheralAddresses) {
                if(BluetoothAdapter.checkBluetoothAddress(address)) {
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
                bluetoothGatt = result.getDevice().connectGatt(context, false, gattCallback);
                scanResult = result;
                if(scanBinder.scanResults.size()==5){
                    scanBinder.scanResults.remove(4);
                }
                scanBinder.scanResults.add(0,result.getRssi());
                ScanCallback stopscanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                    }
                };
                bluetoothAdapter.getBluetoothLeScanner().stopScan(stopscanCallback);

            }
        };
        //bluetoothAdapter.getBluetoothLeScanner().startScan(null, settings, scanCallback);
        bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
        BluetoothLeScanner x=bluetoothAdapter.getBluetoothLeScanner();
        Log.i("break",x.toString());
    }

}