package com.example.chaudfroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends AppCompatActivity {
    private Timer timer;
    private ImageView dashBoard;
    private ImageView arrow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(dashBoard.getRotation() < 180-0.05){
                    dashBoard.setRotation(dashBoard.getRotation() + 0.05f);
                }else{
                    dashBoard.setRotation(dashBoard.getRotation() + 0.05f - 180f);
                }
                if(arrow.getRotation() < 300-0.05){
                    arrow.setRotation(arrow.getRotation() + 0.05f);
                }else {
                    arrow.setRotation(arrow.getRotation() + 0.05f - 60f);
                }
            }
        }, 0, 10);
    }
    private void callService(){
        Intent intentService=new Intent(this, SkanService.class);
        ServiceConnection serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intentService, serviceConnection , BIND_AUTO_CREATE);
    }
}