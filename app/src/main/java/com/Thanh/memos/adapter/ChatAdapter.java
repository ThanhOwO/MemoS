package com.Thanh.memos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Thanh.memos.R;
import com.Thanh.memos.model.ChatModel;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatHolder> {
    Context context;
    List<ChatModel> list;

    public ChatAdapter(Context context, List<ChatModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_items, parent, false);
        return new ChatHolder(view);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHolder holder, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (list.get(position).getSenderID().equalsIgnoreCase(user.getUid())){
            holder.leftchat.setVisibility(View.GONE);
            holder.rightchat.setVisibility(View.VISIBLE);
            holder.rightchat.setText(list.get(position).getMessage());
            holder.profileImg.setVisibility(View.GONE);
        }else{
            holder.rightchat.setVisibility(View.GONE);
            holder.leftchat.setVisibility(View.VISIBLE);
            holder.leftchat.setText(list.get(position).getMessage());

            FirebaseFirestore.getInstance().collection("Users")
                    .document(list.get(position).getSenderID())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String profileImage = document.getString("profileImage");
                                Glide.with(context).load(profileImage).into(holder.profileImg);
                            }
                        }
                    });
        }
    }

    static class ChatHolder extends RecyclerView.ViewHolder{

        TextView leftchat, rightchat;
        CircleImageView profileImg;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);

            leftchat = itemView.findViewById(R.id.leftChat);
            rightchat = itemView.findViewById(R.id.rightChat);
            profileImg = itemView.findViewById(R.id.profileImage);
        }
    }
}
