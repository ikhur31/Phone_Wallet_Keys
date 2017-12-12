package com.example.averygrimes.phone_wallet_keys;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView.BufferType;


public class Themes extends AppCompatActivity implements View.OnClickListener
{
    Button btn_UHCL, btn_Dark, btn_Beach, btn_Fall;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);

        btn_UHCL =(Button)findViewById(R.id.btn_UHCL);
        btn_Dark =(Button)findViewById(R.id.btn_Dark);
        btn_Beach=(Button)findViewById(R.id.btn_Beach);
        btn_Fall =(Button)findViewById(R.id.btn_Fall);

        btn_UHCL.setOnClickListener(this);
        btn_Dark.setOnClickListener(this);
        btn_Beach.setOnClickListener(this);
        btn_Fall.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        String t = "1";
        //changes the color of background depending on theme class
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.activity_main_layout);
        ActionBar actionBar = getSupportActionBar();

        switch (view.getId())
        {
            case R.id.btn_UHCL:
            {
                break;
            }
            case R.id.btn_Dark:
            {
                t = "2";
                break;
            }
            case R.id.btn_Beach:
            {
                t = "3";
                break;
            }
            case R.id.btn_Fall:
            {
                t = "4";
                break;
            }
        }

        try
        {
            File file = getFileStreamPath("themes.txt");

            if (!file.exists())
            {
                file.createNewFile();
            }

            FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);
            byte[] bytesArray = t.getBytes();

            writer.write(bytesArray);

            writer.close();

            FileInputStream reader = openFileInput(file.getName());

            String content = "";

            byte[] input = new byte[reader.available()];
            while (reader.read(input) != -1) {}
            content += new String(input);

            Log.d("myTag",content);
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        finish();
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }
}