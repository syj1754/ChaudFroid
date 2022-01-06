package com.example.chaudfroid;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

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
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
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
    private float direction = 0;
    private float currentDegree = 269;
    private double distance_x = 0;
    private double distance_y = 0;
    private Sensor sensorStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorStep = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, sensorMagnetic,SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(listener, sensorStep,SensorManager.SENSOR_DELAY_GAME);
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        Bundle bundle=intent.getExtras();
        String message = bundle.getString("message");
        direction = bundle.getFloat("direction");
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
                arrow.setRotation( 269 + direction - rotationPhone);
                /*if(arrow.getRotation() < 300-0.05){
                    arrow.setRotation(arrow.getRotation() + 0.05f);
                }else {
                    arrow.setRotation(arrow.getRotation() + 0.05f - 60f);
                }*/
                /*RotateAnimation ra = new RotateAnimation(arrow.getRotation(),269 + direction - rotationPhone, Animation.RELATIVE_TO_PARENT,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
                ra.setDuration(50);
                arrow.startAnimation(ra);
                currentDegree= 269 + direction - rotationPhone;*/
            }
        }, 0, 100);
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
        sensorManager.registerListener(listener, sensorStep,SensorManager.SENSOR_DELAY_GAME);
    }

    private float step = 0;
    private final SensorEventListener listener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                magneticFieldValues = event.values;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                accelerometerValues = event.values;
                calculateOrientation();
            }
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER){
                if(step!=event.values[0] && step!=0){
                    calculateDistance();
                }
                step=event.values[0];
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void calculateDistance() {
        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        distance_x+=0.4*sin(Math.toRadians(rotationPhone));
        distance_y+=0.4*cos(Math.toRadians(rotationPhone));

    }

    private void calculateOrientation() {
        SensorManager.getRotationMatrix(rotation, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(rotation, phoneAngleValues);

        rotationPhone = (float) Math.toDegrees(phoneAngleValues[0]);
        //arrow.setRotation( 269 + direction - rotationPhone);
        /*RotateAnimation ra = new RotateAnimation(arrow.getRotation(),269 + direction - rotationPhone, Animation.RELATIVE_TO_PARENT,0.5f,
                Animation.RELATIVE_TO_SELF,0.5f);
        ra.setDuration(50);
        arrow.startAnimation(ra);
        currentDegree= 269 + direction - rotationPhone;*/
    }

    private void callService(){
        Intent intentService=new Intent(this, SkanService.class);
        ServiceConnection serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if(!((SkanService.ScanBinder) service).scanResults.isEmpty()){
                    double A = 0;
                    double a=0,b=0,c=0;
                    if(rssi!=-100 && sqrt(distance_x*distance_x+distance_y*distance_y)>1){
                        a = sqrt(distance_x*distance_x+distance_y*distance_y);
                        b = pow(10,((abs(rssi) - 59f) / (10 * 2)));
                        c = pow(10,((abs(((SkanService.ScanBinder) service).scanResults.get(0)) - 59f) / (10*2)));
                        A = 180*acos((b*b+c*c-a*a)/(2*b*c+0.01))/Math.PI;
                        direction -= A;
                        distance_x = 0;
                        distance_y = 0;
                    }
                    rssi=((SkanService.ScanBinder) service).scanResults.get(0);
                    TextView textViewRssi= (TextView)findViewById(R.id.textView5);
                    double d = pow(10,((abs(rssi) - 59f) / (10*2)));
                    textViewRssi.setText(rssi + " dbm" +"\n" +distance_x+" "+distance_y+" "+step +"\n"+ String.format("%.2f", d)+" m");
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intentService, serviceConnection , BIND_AUTO_CREATE);
    }
}