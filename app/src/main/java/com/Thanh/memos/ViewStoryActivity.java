package com.Thanh.memos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewStoryActivity extends AppCompatActivity {

    PlayerView exoPlayer;
    public static final String VIDEO_URL_KEY = "videoURL";
    public static final String FILE_TYPE = "file type";
    public static final String USER_NAME = "user name";
    public static final String UID= "uid";
    ImageView imageView;
    CircleImageView storyProfileImage;
    TextView storyUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story);

        init();

        String url = getIntent().getStringExtra(VIDEO_URL_KEY);
        String type = getIntent().getStringExtra(FILE_TYPE);
        String name = getIntent().getStringExtra(USER_NAME);
        String uid = getIntent().getStringExtra(UID);
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

            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .addSnapshotListener((value, error) -> {
                        if (error != null)
                            return;

                        if (!value.exists())
                            return;

                        Glide.with(getApplicationContext()).load(value.getString("profileImage")).into(storyProfileImage);
                    });

            storyUserName.setText(name);

        }else {
            storyUserName.setText(name);

            FirebaseFirestore.getInstance().collection("Users").document(uid)
                    .addSnapshotListener((value, error) -> {
                        if (error != null)
                            return;

                        if (!value.exists())
                            return;

                        Glide.with(getApplicationContext()).load(value.getString("profileImage")).into(storyProfileImage);
                    });

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
        storyProfileImage = findViewById(R.id.storyprofileImage);
        storyUserName = findViewById(R.id.storynameTV);
    }
}