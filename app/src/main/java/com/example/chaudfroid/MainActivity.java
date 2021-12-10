package com.example.chaudfroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.Console;

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
                    }
                }
            });
            String message = editText.getText().toString();
            intent.putExtra(EXTRA_MESSAGE, message);
            startActivity(intent);
        }
    }
}