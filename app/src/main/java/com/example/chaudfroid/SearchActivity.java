package com.example.chaudfroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends AppCompatActivity {
    private Timer timer;
    //private Timer timerService;
    private ImageView dashBoard;
    private ImageView arrow;
    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorMagnetic;
    private float[] magneticFieldValues = new float[3];
    private float[] accelerometerValues = new float[3];
    private float[] phoneAngleValues = new float[3];
    private float[] rotation = new float[9];
    private float rotationPhone;
    private int rssi = -100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, sensorMagnetic,SensorManager.SENSOR_DELAY_GAME);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        dashBoard = findViewById(R.id.imageView);
        arrow = findViewById(R.id.imageView3);
        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView2);
        textView.setText("Adresse MAC:"+message);
    }
    @Override
    protected void onStart() {
        super.onStart();
        /*timerService = new Timer();
        timerService.schedule(new TimerTask() {
            @Override
            public void run() {
                callService();
            }
        }, 0, 50);*/
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callService();
                dashBoard.setRotation(((float)rssi+100)*180f/60f);
                arrow.setRotation( 269 - rotationPhone);
                /*if(arrow.getRotation() < 300-0.05){
                    arrow.setRotation(arrow.getRotation() + 0.05f);
                }else {
                    arrow.setRotation(arrow.getRotation() + 0.05f - 60f);
                }*/
            }
        }, 0, 10);
    }
    @Override
    protected void onPause(){
        sensorManager.unregisterListener(listener);
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, sensorMagnetic,SensorManager.SENSOR_DELAY_GAME);
    }
    private final SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = event.values;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                accelerometerValues = event.values;
            calculateOrientation();
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void calculateOrientation() {
        SensorManager.getRotationMatrix(rotation, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(rotation, phoneAngleValues);

        rotationPhone = (float) Math.toDegrees(phoneAngleValues[0]);
    }

    private void callService(){
        Intent intentService=new Intent(this, SkanService.class);
        ServiceConnection serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                rssi=((SkanService.ScanBinder) service).scanResults.get(0).getRssi();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intentService, serviceConnection , BIND_AUTO_CREATE);
    }
}