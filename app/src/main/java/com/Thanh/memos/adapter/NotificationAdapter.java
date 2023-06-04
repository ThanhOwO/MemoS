package com.Thanh.memos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.Thanh.memos.R;
import com.Thanh.memos.fragments.Notification;
import com.Thanh.memos.model.NotificationModel;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationHolder> {

    Context context;
    List<NotificationModel> list;


    public NotificationAdapter(Context context, List<NotificationModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_items, parent, false);
        return new NotificationHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull NotificationHolder holder, int position) {
        holder.notification.setText(list.get(position).getNotification());
        holder.time.setText(calculateTime(list.get(position).getTime()));

        String oppositeID = list.get(position).getOppositeID();
        FirebaseFirestore.getInstance()
                .collection("Users")
                .document(oppositeID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the profileImage URL from the document
                        String profileImageURL = documentSnapshot.getString("profileImage");

                        // Load the profileImage using Glide library
                        Glide.with(context)
                                .load(profileImageURL)
                                .into(holder.profileImage);
                    }
                })
                .addOnFailureListener(e -> {
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    String calculateTime(Date date){
        long millis = date.toInstant().toEpochMilli();
        return DateUtils.getRelativeTimeSpanString(millis, System.currentTimeMillis(), 6000, DateUtils.FORMAT_ABBREV_TIME).toString();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NotificationHolder extends RecyclerView.ViewHolder{

        TextView time, notification;
        CircleImageView profileImage;
        public NotificationHolder(@NonNull View itemView) {
            super(itemView);

            time = itemView.findViewById(R.id.timeTv);
            notification = itemView.findViewById(R.id.notification);
            profileImage = itemView.findViewById(R.id.profileImage);
        }
    }
}
