package com.example.averygrimes.phone_wallet_keys;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class NotificationsList extends AppCompatActivity implements View.OnClickListener
{

    private static final int MY_PERMISSION_REQUEST = 1;
    ArrayList<String> arrayList;
    ListView listView;
    ArrayAdapter<String> adapter;

    Button btn_Change_Notification_Sound;
    String deviceAddress;
    Uri uriSound;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications_list);

        btn_Change_Notification_Sound = (Button) findViewById(R.id.Btn_Change_Notification_Sound);
        btn_Change_Notification_Sound.setOnClickListener(this);

        Bundle extrasForDeviceSettings = getIntent().getExtras();

        deviceAddress = extrasForDeviceSettings.getString("deviceAddress");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if(resultCode == RESULT_OK && requestCode == 10)
        {
            uriSound = data.getData();

            Ringtone ringtone = RingtoneManager.getRingtone(this, uriSound);

            btn_Change_Notification_Sound.setText("Current Notification Sound: " + ringtone.getTitle(this));

            String content = "";
            File file = getFileStreamPath("NotificationSound.txt");

            try
            {
                if (!file.exists())
                {
                    file.createNewFile();
                }
                else
                {
                    FileInputStream reader = openFileInput(file.getName());

                    byte[] input = new byte[reader.available()];
                    while (reader.read(input) != -1) {}

                    content += new String(input);

                    String[] notificationList = content.split(",");

                    String[] temp;
                    for(int i = 0; i < notificationList.length; i++)
                    {
                        temp = notificationList[i].split("\\|");

                        if(temp[0].equals(deviceAddress))
                        {
                            content = content.replaceAll(temp[0] + "\\|" + temp[1] + ",", "");
                        }
                    }
                }

            }
            catch (IOException e)
            {
                Log.e("Exception", "File Read failed: " + e.toString());
            }

            String newContent = content + deviceAddress + "|" + uriSound + ",";

            try
            {
                FileOutputStream writer = openFileOutput(file.getName(), Context.MODE_PRIVATE);

                byte[] bytesArray = newContent.getBytes();

                writer.write(bytesArray);

                writer.close();
            }
            catch (IOException e)
            {
                Log.e("Exception", "File write failed: " + e.toString());
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.Btn_Change_Notification_Sound:
            {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 10);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        /*Context context = this.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("deviceAddress", deviceAddress);
        context.startActivity(intent);*/
        finish();
    }

}