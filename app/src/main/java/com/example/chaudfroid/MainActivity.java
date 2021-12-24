package com.example.chaudfroid;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.Console;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.chaudfroid.MESSAGE";
    private Timer timer;
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
                    ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                    progressBar.setVisibility(View.VISIBLE);
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
        intentService.putExtra(EXTRA_MESSAGE, message);
        startService(intentService);
        timer =new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                callService();
            }
        }, 50, 50);
    }

    public void startSearch(View view) {
        FrameLayout frameCenter = (FrameLayout) findViewById(R.id.Frame_center);
        if(frameCenter.getVisibility()==View.VISIBLE){
            Intent intent = new Intent(this, SearchActivity.class);
            EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
            String message = editText.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }else{
            frameCenter.setVisibility(View.VISIBLE);
        }
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
                    int rssi=((SkanService.ScanBinder) service).scanResults.get(0).getRssi();
                    TextView textViewRssi= (TextView)findViewById(R.id.signal_strength);
                    textViewRssi.setText(rssi + " dbm");
                    EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
                    String mac = ((SkanService.ScanBinder) service).scanResults.get(0).getDevice().getAddress();
                    //String mac = editText.getText().toString();
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