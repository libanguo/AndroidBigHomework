package com.example.afinal;

import android.app.Activity;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//视频播放类
public class VideoPlay extends AppCompatActivity {
    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        String url =intent.getStringExtra("videoUrl");
        videoView.setVideoURI(Uri.parse(url));
        MediaController mediaController=new MediaController(this);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        ActionBar actionBar=((AppCompatActivity)VideoPlay.this).getSupportActionBar();
        WindowManager.LayoutParams layoutParams=getWindow().getAttributes();
        if(actionBar!=null){
            actionBar.hide();
        }
        layoutParams.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(layoutParams);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        videoView.start();
    }

}
