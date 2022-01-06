package com.example.chaudfroid;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.graphics.drawable.RotateDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private Timer timer;
    private int status = 0;
    private Point boxInit[] = new Point[5];
    private boolean timeInit = false;
    private Handler handler = new Handler();
    private int rssi = -100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        class MacFilter implements InputFilter {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source.length() <= 0) {
                    return "";
                }
                int deleteLength=0;
                for (int i = 0; i < source.length(); i++) {
                    int chr = source.charAt(i);
                    if(!((chr>=65 && chr<=70) || (chr>=48 && chr<=57) || (chr>=97 && chr<=102))){
                        deleteLength++;
                    }
                }
                if(source.length()-deleteLength==0){
                    return "";
                }
                int length=source.length()-deleteLength+(source.length()-deleteLength+dstart%3)/2;

                if((dstart%3)==2){
                    length++;
                }else if(dstart+source.length()==17){
                    length--;
                }
                char[] newChar = new char[length];
                int j=0,k=0,f=0;
                if((dstart%3)==2){
                    newChar[k++] = ':';
                }
                for (int i = 0; i < source.length(); i++) {
                    int chr = source.charAt(i);
                    boolean hex = (chr >= 65 && chr <= 70) || (chr >= 48 && chr <= 57) || (chr >= 97 && chr <= 102);
                    if(hex){
                        newChar[i+j+k+f] = (char) chr;
                    }
                    if((i+1+j+f+dstart%3)%2==0 && (dest.length()+i+j+f+1<17) && (dest.length()==dend || dest.charAt(dend)!=':')){
                        newChar[++j+i+k+f] = ':';
                    }
                    if(!(hex)){
                        f--;
                    }
                }
                /*int x[]=new int[length];
                String test=new String();
                for(int i=0;i<length;i++){
                    x[i]=(int)newChar[i];
                    test+=x[i];
                    test+=" ";

                }
                TextView textViewRssi= (TextView)findViewById(R.id.device_name);
                textViewRssi.setText(test);*/
                return new String(newChar);
            }
        };
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17),new MacFilter()});
        editText.addTextChangedListener(new TextWatcher() {


            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(s.length()>=17){
                    SkanStart();
                }
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    private void SkanStart() {
        Intent intentService=new Intent(this, SkanService.class);
        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
        String message = editText.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString("message",message);
        intentService.putExtras(bundle);
        startService(intentService);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callService();
            }
        }, 50, 50);
    }

    public void startSearch(View view) {
        FrameLayout frameCenter = (FrameLayout) findViewById(R.id.Frame_center);
        timeInit=false;
        TextView tip = (TextView) findViewById(R.id.textView3);
        if(status == 5){
            Intent intent = new Intent(this, SearchActivity.class);
            Float rssiInit[]= new Float[6];
            //float rssiInit[] = {-81,-80,-82,-84,-85,-100};
            float direction = 0;
            int pointProche = 5;
            int pointProche2 = 5;
            int pointProche3 = 5;
            rssiInit[5]= Float.valueOf(-100);
            for(int i=0;i<5;i++){
                rssiInit[i]=((float)boxInit[i].x)/boxInit[i].y;

                if(rssiInit[i]>rssiInit[pointProche]){
                    pointProche3=pointProche2;
                    pointProche2=pointProche;
                    pointProche=i;
                }else if(rssiInit[i]>rssiInit[pointProche2]){
                    pointProche3=pointProche2;
                    pointProche2=i;
                }else if(rssiInit[i]>pointProche3){
                    pointProche3=i;
                }
            }
            if(pointProche == 4){
                pointProche=pointProche2;
                pointProche2=pointProche3;
            }

            float intrval2 = rssiInit[pointProche] - rssiInit[pointProche2];
            if(pointProche2 != 4){
                intrval2 = rssiInit[pointProche] - rssiInit[(2 * pointProche - pointProche2 + 4) % 4];
            }
            direction = 90*pointProche + (-pointProche + pointProche2)%4*45*(intrval2 -(rssiInit[pointProche]-rssiInit[pointProche2]))/ intrval2;
            Log.i(pointProche3+"",direction+"");
            Log.i(pointProche+"",pointProche2+"");
            EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
            String message = editText.getText().toString();
            Bundle bundle = new Bundle();
            bundle.putString("message",message);
            bundle.putFloat("direction",direction);
            intent.putExtras(bundle);
            startActivity(intent);
        }else if(status == 0){
            frameCenter.setVisibility(View.VISIBLE);
            ((TextView)view).setText("Suivant");
            tip.setText(R.string.etape_1);
        }else if(status == 4){
            ((TextView)view).setText("Fin");
            tip.setText(R.string.etape_5);
        }else if(status == 1){
            tip.setText(R.string.etape_2);
        }else if(status == 2){
            tip.setText(R.string.etape_3);
        }else if(status == 3){
            tip.setText(R.string.etape_4);
        }
        status++;
        TextView timesScan= (TextView)findViewById(R.id.textView4);
        timesScan.setText("Nombre du Scan:"+0+" fois");
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                timeInit = true;

            }
        }, 2000);
    }
    private void callService(){
        Intent intentService=new Intent(this, SkanService.class);
        ServiceConnection serviceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if(((SkanService.ScanBinder) service).scanResults.isEmpty()){
                    TextView textViewRssi= (TextView)findViewById(R.id.signal_strength);
                    textViewRssi.setText(0 + " dbm");
                    TextView textViewMac= (TextView)findViewById(R.id.mac_address);
                    textViewMac.setText("NULL");
                }else{
                    if( status > 0 && status <= 5 && timeInit && rssi!=((SkanService.ScanBinder) service).scanResults.get(0) && rssi!=-100){
                        if(boxInit[status-1]==null){
                            boxInit[status-1]= new Point(0,0);
                        }
                        Log.i(boxInit[status-1].x+"",status+"");
                        rssi=((SkanService.ScanBinder) service).scanResults.get(0);
                        boxInit[status-1].x+=rssi;
                        boxInit[status-1].y++;
                        TextView timesScan= (TextView)findViewById(R.id.textView4);
                        timesScan.setText("Nombre du Scan:"+boxInit[status-1].y+" fois");
                    }else{
                        rssi=((SkanService.ScanBinder) service).scanResults.get(0);
                        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                    TextView textViewRssi= (TextView)findViewById(R.id.signal_strength);
                    textViewRssi.setText(rssi + " dbm");
                    EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
                    String mac = editText.getText().toString();
                    TextView textViewMac= (TextView)findViewById(R.id.mac_address);
                    textViewMac.setText(mac);

                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intentService, serviceConnection , BIND_AUTO_CREATE);
    }


}