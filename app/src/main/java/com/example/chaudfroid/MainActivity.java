package com.example.chaudfroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.chaudfroid.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void startSearch(View view) {
        FrameLayout frameCenter = (FrameLayout) findViewById(R.id.Frame_center);
        frameCenter.setVisibility(View.VISIBLE);
        if(frameCenter.getVisibility()==View.VISIBLE){
            Intent intent = new Intent(this, SearchActivity.class);
            EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);
            String message = editText.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }
    }
}