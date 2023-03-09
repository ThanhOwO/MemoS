package com.Thanh.memos.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.Thanh.memos.R;
import com.Thanh.memos.fragments.Home;
import com.Thanh.memos.model.HomeModel;
import com.bumptech.glide.Glide;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder> {

    private List<HomeModel> list;
    Context context;

    public HomeAdapter(List<HomeModel> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @androidx.annotation.NonNull
    @Override
    public HomeHolder onCreateViewHolder(@androidx.annotation.NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_items, parent, false);
        return new HomeHolder(view);
    }

    @Override
    public void onBindViewHolder(@androidx.annotation.NonNull HomeHolder holder, int position) {

        holder.userNametv.setText(list.get(position).getUserName());
        holder.timeTv.setText(list.get(position).getTimestamp());

        int count = list.get(position).getLikeCount();

        //like count
        if(count == 0){
            holder.likecountTv.setVisibility(View.INVISIBLE);
        }else if (count == 1) {
            holder.likecountTv.setText(count + "likes");
        }else{
            holder.likecountTv.setText(count + "likes");
        }

        holder.timeTv.setText(list.get(position).getLikeCount() + "like");

        Random random = new Random();
        int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context.getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(R.drawable.ic_person)
                .timeout(6500)
                .into(holder.profileImage);
        Glide.with(context.getApplicationContext())
                .load(list.get(position).getProfileImage())
                .placeholder(new ColorDrawable(color))
                .timeout(7000)
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class HomeHolder extends RecyclerView.ViewHolder{

        private CircleImageView profileImage;
        private TextView userNametv, timeTv, likecountTv;
        private ImageView imageView;
        private ImageButton likeBtn, commentBtn, shareBtn;

        public HomeHolder(@NonNull View itemView){
            super(itemView);

            profileImage = itemView.findViewById(R.id.profileImage);
            imageView = itemView.findViewById(R.id.imageView);
            userNametv = itemView.findViewById(R.id.nameTv);
            timeTv = itemView.findViewById(R.id.timeTv);
            likecountTv = itemView.findViewById(R.id.likecountTv);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            commentBtn = itemView.findViewById(R.id.commentBtn);
            shareBtn = itemView.findViewById(R.id.shareBtn);
        }
    }
}
