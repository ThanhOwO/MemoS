package com.Thanh.memos.adapter;

import static com.Thanh.memos.ViewStoryActivity.FILE_TYPE;
import static com.Thanh.memos.ViewStoryActivity.VIDEO_URL_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.Thanh.memos.R;
import com.Thanh.memos.StoryAddActivity;
import com.Thanh.memos.ViewStoryActivity;
import com.Thanh.memos.model.StoriesModel;
import com.bumptech.glide.Glide;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoriesAdapter extends RecyclerView.Adapter<StoriesAdapter.StoriesHolder> {

    List<StoriesModel> list;
    Activity activity;

    public StoriesAdapter(List<StoriesModel> list, Activity activity) {
        this.list = list;
        this.activity = activity;
    }

    @NonNull
    @Override
    public StoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stories_layout, parent, false);
        return new StoriesHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoriesHolder holder, @SuppressLint("RecyclerView") int position) {

        //upload stories video
        if (position == 0){
            Glide.with(activity)
                    .load(activity.getResources().getDrawable(R.drawable.ic_add))
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activity.startActivity(new Intent(activity, StoryAddActivity.class));
                }
            });
        }else{
            //load stories video
            Glide.with(activity)
                    .load(list.get(position).getUrl())
                    .timeout(6500)
                    .into(holder.imageView);
            holder.imageView.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.R)
                @Override
                public void onClick(View view) {
                    if(position == 0){
                        //new story
                        Dexter.withContext(activity)
                                .withPermissions(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.READ_MEDIA_VIDEO
                                )
                                .withListener(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        if (report.areAllPermissionsGranted()) {
                                            // Permissions granted, start activity
                                            activity.startActivity(new Intent(activity, StoryAddActivity.class));
                                        } else {
                                            // Permissions denied, show error message
                                            Toast.makeText(activity, "Please allow permission to continue.", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                        // Show rationale dialog
                                        token.continuePermissionRequest();
                                    }
                                }).check();
                    }else{
                        //open story
                        Intent intent = new Intent(activity, ViewStoryActivity.class);
                        intent.putExtra(VIDEO_URL_KEY, list.get(position).getUrl());
                        intent.putExtra(FILE_TYPE, list.get(position).getType());
                        activity.startActivity(intent);
                    }

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class StoriesHolder extends RecyclerView.ViewHolder{

        private CircleImageView imageView;

        public StoriesHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (CircleImageView) itemView.findViewById(R.id.imageView);
        }
    }
}
