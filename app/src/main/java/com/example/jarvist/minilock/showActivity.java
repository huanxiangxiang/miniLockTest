package com.example.jarvist.minilock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;



public class showActivity extends AppCompatActivity {

    private EditText editText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        editText = (EditText)findViewById(R.id.scanResult);
        Intent intent = getIntent();
        String deviceID = intent.getStringExtra("data");

    }




}
