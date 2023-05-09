package com.Thanh.memos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

public class ViewStoryActivity extends AppCompatActivity {

    PlayerView exoPlayer;
    public static final String VIDEO_URL_KEY = "videoURL";
    public static final String FILE_TYPE = "file type";
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story);

        init();

        String url = getIntent().getStringExtra(VIDEO_URL_KEY);
        String type = getIntent().getStringExtra(FILE_TYPE);
        if(url == null || url.isEmpty()){
            finish();
        }

        if(type.contains("image")){
            //image
            imageView.setVisibility(View.VISIBLE);
            exoPlayer.setVisibility(View.GONE);

            Glide.with(getApplicationContext())
                    .load(url)
                    .into(imageView);

        }else {
            //video
            exoPlayer.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            MediaItem item = MediaItem.fromUri(url);
            SimpleExoPlayer player = new SimpleExoPlayer.Builder(this).build();
            player.setMediaItem(item);
            exoPlayer.setPlayer(player);

            player.play();
        }

    }

    void init(){
        exoPlayer = findViewById(R.id.videoView);
        imageView = findViewById(R.id.imageView);
    }
}