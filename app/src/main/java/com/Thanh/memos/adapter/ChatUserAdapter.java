package com.Thanh.memos.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.Thanh.memos.model.ChatUserModel;

import java.util.List;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ChatUserHolder> {

    Activity context;
    List<ChatUserModel> list;

    public ChatUserAdapter(Activity context, List<ChatUserModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ChatUserHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ChatUserHolder extends RecyclerView.ViewHolder {
        public ChatUserHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
