package com.Thanh.memos;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gowtham.library.utils.CompressOption;
import com.gowtham.library.utils.TrimType;
import com.gowtham.library.utils.TrimVideo;
import com.marsad.stylishdialogs.StylishAlertDialog;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StoryAddActivity extends AppCompatActivity {

    VideoView videoView;
    FirebaseUser user;
    ImageButton uploadButton;
    StylishAlertDialog alertDialog;
    ImageView imageView;

    private static final int SELECT_VIDEO = 101;

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK &&
                        result.getData() != null) {
                    Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.getData()));
                    videoView.setVideoURI(uri);
                    videoView.start();

                    uploadButton.setVisibility(View.VISIBLE);
                    uploadButton.setOnClickListener(view -> {
                        uploadButton.setVisibility(View.GONE);
                        videoView.pause();
                        uploadFileToStorage(uri, "video");
                    });

                } else {
                    Toast.makeText(this, "Data is null", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_add);

        init();

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/* video/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, "image/* video/*");
        startActivityForResult(intent, SELECT_VIDEO);
    }

    void uploadFileToStorage(Uri uri, String type){

        alertDialog = new StylishAlertDialog(this, StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Uploading...").setCancelable(false);
        alertDialog.show();

        File file = new File(uri.getPath());

        String fileName;
        if(type.contains("image")){
            fileName = System.currentTimeMillis() + ".png";
            uploadImagetoStorage(fileName, uri, type);
        }else {
            fileName = System.currentTimeMillis()+".mp4";
            uploadVideotoStorage(new File(fileName), uri, type);
        }

    }

    void uploadImagetoStorage(String fileName, Uri uri, String type){
        StorageReference storageReference =  FirebaseStorage.getInstance().getReference().child("Stories/" + fileName);


        storageReference.putFile(uri).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                assert task.getResult() != null;
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
                    uploadVideoDatatoFirestore(String.valueOf(uri1), type);
                });
            }else {
                alertDialog.dismissWithAnimation();
                assert task.getException() != null;
                String error = task.getException().getMessage();
                Toast.makeText(StoryAddActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void uploadVideotoStorage(File fileName, Uri uri, String type){
        File file = new File(uri.getPath());
        StorageReference storageReference =  FirebaseStorage.getInstance().getReference().child("Stories/" + fileName);


        storageReference.putFile(Uri.fromFile(file)).addOnCompleteListener(task -> {
            if (task.isSuccessful()){

                assert task.getResult() != null;
                task.getResult().getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
                    uploadVideoDatatoFirestore(String.valueOf(uri1), type);
                });
            }else {
                alertDialog.dismissWithAnimation();
                assert task.getException() != null;
                String error = task.getException().getMessage();
                Toast.makeText(StoryAddActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void uploadVideoDatatoFirestore(String url, String type){

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Stories");

        String id = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("url", url);
        map.put("id", id);
        map.put("uid", user.getUid());
        map.put("type", type);
        map.put("name", user.getDisplayName());

        reference.document(id)
                .set(map);

        alertDialog.dismissWithAnimation();
        finish();
    }

    void init(){
        videoView = findViewById(R.id.videoView);
        uploadButton = findViewById(R.id.uploadStory);
        imageView = findViewById(R.id.imageView);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECT_VIDEO){

            Uri uri = data.getData();

            if(uri.toString().contains("image")){
                //image selected
                videoView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);

                Glide.with(StoryAddActivity.this)
                        .load(uri)
                        .into(imageView);

                uploadButton.setVisibility(View.VISIBLE);
                uploadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uploadButton.setVisibility(View.GONE);
                        uploadFileToStorage(uri, "image");
                    }
                });

            }else if(uri.toString().contains("video")){
                TrimVideo.activity(String.valueOf(uri))
                        .setCompressOption(new CompressOption())
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setMinToMax(5, 30)
                        .setHideSeekBar(true)
                        .start(this,startForResult);
            }
        }else {
            Toast.makeText(StoryAddActivity.this, "Cannot trim this video.", Toast.LENGTH_SHORT).show();
        }
    }
}