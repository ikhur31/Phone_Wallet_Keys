package com.example.averygrimes.phone_wallet_keys;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * Created by Avery Grimes on 11/22/2017.
 */

public class Help extends AppCompatActivity {
    Button btn1,backBtn;
    VideoView videov;
    MediaController mediaC;


    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_popup);

        btn1 = (Button) findViewById(R.id.playButton);
        backBtn = (Button) findViewById(R.id.backButton);
        videov = (VideoView) findViewById(R.id.videoView);
        mediaC = new MediaController(this);
    }

    public void videoClick(View v)
    {
        String videoPath = "android.resource://com.example.averygrimes.phone_wallet_keys/"+
                R.raw.test1;
        Uri uri = Uri.parse(videoPath);
        videov.setVideoURI(uri);
        videov.setMediaController(mediaC);
        mediaC.setAnchorView(videov);
        videov.start();
    }

    public void backClick(View v)
    {
        finish();
    }
}