package com.Thanh.memos.fragments;

import static android.app.Activity.RESULT_OK;

import static com.Thanh.memos.MainActivity.IS_SEARCHED_USER;
import static com.Thanh.memos.MainActivity.USER_ID;
import static com.Thanh.memos.utils.Constrants.PREF_DIRECTORY;
import static com.Thanh.memos.utils.Constrants.PREF_NAME;
import static com.Thanh.memos.utils.Constrants.PREF_STORED;
import static com.Thanh.memos.utils.Constrants.PREF_URL;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.Thanh.memos.FragmentReplacerActivity;
import com.Thanh.memos.MainActivity;
import com.Thanh.memos.R;
import com.Thanh.memos.chat.ChatActivity;
import com.Thanh.memos.model.PostImageModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marsad.stylishdialogs.StylishAlertDialog;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv, emptyGallery;
    private CircleImageView profileImage;
    private Button followBtn, startChatBtn;
    private RecyclerView recyclerView;

    private LinearLayout countLayout;
    private FirebaseUser user;
    private ImageButton editprofileBtn, optionBtn;
    int count;

    boolean isFollowed;
    DocumentReference userRef, myRef;
    List<Object> followersList, followingList, followingList_2;
    boolean isMyProfile = true;
    String userUID;
    FirestoreRecyclerAdapter<PostImageModel, PostImageHolder> adapter;

    LinearLayout newuserNotification;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);

        myRef = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        //When check user done change the profile to the user that searched
        if(IS_SEARCHED_USER){
            userUID = USER_ID;
            isMyProfile = false;
            loadData();
        }else{
            isMyProfile = true;
            userUID = user.getUid();
        }

        //checks whether the current user is viewing their own profile or someone else's profile.
        if (isMyProfile){
            editprofileBtn.setVisibility(View.VISIBLE);
            followBtn.setVisibility(View.GONE);
            startChatBtn.setVisibility(View.GONE);
            countLayout.setVisibility(View.VISIBLE);
        }else {
            editprofileBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.VISIBLE);
        }

        //get user information from firebase storage
        userRef = FirebaseFirestore.getInstance().collection("Users")
                .document(userUID);

        loadBasicData();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        loadPostImage();
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(null);

        clickListener();

    }

    private void loadData(){
        myRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.e("Tag_b", error.getMessage());
                    return;
                }
                if(value == null || !value.exists()){
                    return;
                }
                followingList_2 = (List<Object>) value.get("following");
            }
        });
    }

    private void clickListener(){
        //follow or unfollow user
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFollowed){
                    followersList.remove(user.getUid()); //opposite user
                    followingList_2.remove(userUID); //us

                    final Map<String, Object> map_2 = new HashMap<>();
                    map_2.put("following", followingList_2);

                    Map<String, Object> map = new HashMap<>();
                    map.put("followers", followersList);

                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                followBtn.setText("Follow");
                                myRef.update(map_2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Log.e("Tag_3", task.getException().getMessage());
                                        }
                                    }
                                });
                            }else {
                                Log.e("Tag", ""+task.getException().getMessage());
                            }
                        }
                    });



                }else {
                    createNotification();
                    followersList.add(user.getUid()); //opposite user
                    followingList_2.add(userUID); //us

                    final Map<String, Object> map_2 = new HashMap<>();
                    map_2.put("following", followingList_2);

                    Map<String, Object> map = new HashMap<>();
                    map.put("followers", followersList);
                    userRef.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                followBtn.setText("Unfollow");
                                myRef.update(map_2).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            Toast.makeText(getContext(), "Followed", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Log.e("Tag_3_1", task.getException().getMessage());
                                        }
                                    }
                                });
                            }else {
                                Log.e("Tag", ""+task.getException().getMessage());
                            }
                        }
                    });


                }
            }
        });

        //Edit profile change picture profile
        editprofileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(getContext(), Profile.this);
            }
        });

        startChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryChat();
            }
        });

        optionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getActivity(), v);
                popupMenu.inflate(R.menu.menu_main);

                // Set enter animation
                try {
                    Class<?> classPopupMenu = Class.forName(popupMenu.getClass().getName());
                    Method setEnterAnimation = classPopupMenu.getDeclaredMethod("setEnterAnimation", int.class);
                    setEnterAnimation.setAccessible(true);
                    setEnterAnimation.invoke(popupMenu, R.style.PopupAnimation);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_setting:
                                // Handle "Setting" button click
                                return true;
                            case R.id.action_logout:
                                // Handle "Logout" button click
                                FirebaseAuth.getInstance().signOut();

                                Intent intent = new Intent(getActivity(), FragmentReplacerActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                // Set exit animation
                popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
                    @Override
                    public void onDismiss(PopupMenu menu) {
                        try {
                            Class<?> classPopupMenu = Class.forName(menu.getClass().getName());
                            Method setExitAnimation = classPopupMenu.getDeclaredMethod("setExitAnimation", int.class);
                            setExitAnimation.setAccessible(true);
                            setExitAnimation.invoke(menu, R.style.PopupAnimation);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                popupMenu.show();
            }
        });
    }


    void queryChat(){

        StylishAlertDialog alertDialog = new StylishAlertDialog(getContext(), StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Starting Chat...");
        alertDialog.setCancelable(false);
        alertDialog.show();

        List<String> list = new ArrayList<>();


        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", userUID)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            QuerySnapshot snapshot = task.getResult();

                            if (snapshot.isEmpty()){
                                startChat(alertDialog);
                            }else {
                                //get chat id and pass
                                alertDialog.dismissWithAnimation();
                                for (DocumentSnapshot snapshotChat : snapshot){
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("uid", userUID);
                                    intent.putExtra("id", snapshotChat.getId()); //return doc id
                                    startActivity(intent);
                                }
                            }
                        }else {
                            alertDialog.dismissWithAnimation();
                        }
                    }
                });
    }

    void startChat(StylishAlertDialog alertDialog){

        //starting a conversation
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");

        List<String> list = new ArrayList<>();
        list.add(0, user.getUid());
        list.add(1, userUID);

        String pushID = reference.document().getId();

        Map<String, Object> map = new HashMap<>();
        map.put("id", pushID);
        map.put("lastMessage", "Hi!");
        map.put("time", FieldValue.serverTimestamp());
        map.put("uid", list);

        reference.document(pushID).update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                }else {
                    reference.document(pushID).set(map);
                }
            }
        });

        //Message

        CollectionReference messageRef = FirebaseFirestore.getInstance()
                .collection("Messages")
                .document(pushID)
                .collection("Messages");

        String messageID = messageRef.document().getId();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("id", messageID);
        messageMap.put("message", "Hi!");
        messageMap.put("senderID", user.getUid());
        messageMap.put("time", FieldValue.serverTimestamp());

        messageRef.document(messageID).set(messageMap);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.dismissWithAnimation();
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("uid", userUID);
                intent.putExtra("id", pushID);
                startActivity(intent);
            }
        }, 3000);
    }

    private void init(View view){

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        assert getActivity() != null;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        nameTv = view.findViewById(R.id.nameTv);
        statusTv = view.findViewById(R.id.aboutTv);
        toolbarNameTv = view.findViewById(R.id.toolbarNameTv);
        followersCountTv = view.findViewById(R.id.followerCountTv);
        followingCountTv = view.findViewById(R.id.followingCountTv);
        postCountTv = view.findViewById(R.id.postCountTv);
        profileImage = view.findViewById(R.id.profileImage);
        followBtn = view.findViewById(R.id.followBtn);
        recyclerView = view.findViewById(R.id.recyclerView);
        countLayout = view.findViewById(R.id.countLayout);
        editprofileBtn = view.findViewById(R.id.edit_profileImage);
        startChatBtn = view.findViewById(R.id.startChatBtn);
        optionBtn = view.findViewById(R.id.optionBtn);
        newuserNotification = view.findViewById(R.id.notification);
        emptyGallery = view.findViewById(R.id.emptyGallery);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    //Get data from firebase storage
    private void loadBasicData(){

        userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.e("Tag_0", error.getMessage());
                    return;
                }

                assert value != null;
                if(value.exists()){
                    String name = value.getString("name");
                    String status = value.getString("status");


                    String profileURL = value.getString("profileImage");

                    nameTv.setText(name);
                    toolbarNameTv.setText(name);
                    statusTv.setText(status);

                    followersList = (List<Object>) value.get("followers");
                    followingList = (List<Object>) value.get("following");

                    followersCountTv.setText(""+followersList.size());
                    followingCountTv.setText(""+followingList.size());

                    try {
                        Glide.with(getContext().getApplicationContext())
                                .load(profileURL)
                                .placeholder(R.drawable.ic_person)
                                .circleCrop()
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                                        storeProfileImage(bitmap, profileURL);
                                        return false;
                                    }
                                })
                                .timeout(6500)
                                .into(profileImage);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    if(followersList.contains(user.getUid())){
                        followBtn.setText("UnFollow");
                        isFollowed = true;
                    }else{
                        isFollowed = false;
                        followBtn.setText("Follow");
                    }

                }
            }
        });
    }

    private void storeProfileImage(Bitmap bitmap, String url){

        SharedPreferences preferences = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        boolean isStored = preferences.getBoolean(PREF_STORED, false);
        String urlString = preferences.getString(PREF_URL, "");
        SharedPreferences.Editor editor = preferences.edit();

        if(isStored && urlString.equals(url))
            return;

        if (IS_SEARCHED_USER)
            return;

        ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());
        File directory = contextWrapper.getDir("image_data", Context.MODE_PRIVATE);
        if(!directory.exists())
            directory.mkdir();
        File path = new File(directory, "profile.png");

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }finally {
            try {
                outputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        editor.putBoolean(PREF_STORED, true);
        editor.putString(PREF_URL, url);
        editor.putString(PREF_DIRECTORY, directory.getAbsolutePath());
        editor.apply();

    }

    //Get post images of user from Firebase storage
    private void loadPostImage(){


        DocumentReference reference = FirebaseFirestore.getInstance().collection("Users").document(userUID);
        Query query = reference.collection("Post Images");
        FirestoreRecyclerOptions<PostImageModel> options = new FirestoreRecyclerOptions.Builder<PostImageModel>()
                .setQuery(query, PostImageModel.class)
                .build();

         adapter = new FirestoreRecyclerAdapter<PostImageModel, PostImageHolder>(options) {
            @NonNull
            @Override
            public PostImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_image_items, parent, false);
                return new PostImageHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull PostImageHolder holder, int position, @NonNull PostImageModel model) {
                Glide.with(holder.itemView.getContext().getApplicationContext())
                        .load(model.getImageUrl())
                        .timeout(6500)
                        .into(holder.imageView);
                count = getItemCount();
                postCountTv.setText("" + count);
            }

             @Override
             public int getItemCount() {
                 int count = super.getItemCount();
                 if (count == 0 && isMyProfile) {
                     newuserNotification.setVisibility(View.VISIBLE);
                 } else if (count == 0 && !isMyProfile) {
                     newuserNotification.setVisibility(View.GONE);
                     emptyGallery.setVisibility(View.VISIBLE);
                 } else {
                     newuserNotification.setVisibility(View.GONE);
                     emptyGallery.setVisibility(View.GONE);
                 }
                 return count;
             }
         };

    }


    private static class PostImageHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        public PostImageHolder(@NonNull View itemView){
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            Uri uri = result.getUri();

            uploadImage(uri);
        }
    }

    //Upload profile image
    private void uploadImage(Uri uri){

        String fileName = UUID.randomUUID().toString() + ".jpg";
        final StorageReference reference = FirebaseStorage.getInstance().getReference().child("Profile Images/" + fileName);

        reference.putFile(uri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            reference.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            String imageURL = uri.toString();
                                            UserProfileChangeRequest.Builder request = new UserProfileChangeRequest.Builder();
                                            request.setPhotoUri(uri);

                                            user.updateProfile(request.build());
                                            Map<String, Object> map = new HashMap<>();
                                            map.put("profileImage", imageURL);

                                            FirebaseFirestore.getInstance().collection("Users")
                                                    .document(user.getUid())
                                                    .update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Toast.makeText(getContext(), "Updated Successfully", Toast.LENGTH_SHORT).show();
                                                            }else {
                                                                Toast.makeText(getContext(), "Error: " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        }
                                    });
                        }else {
                            assert task.getException() != null;
                            Toast.makeText(getContext(), "Error: " +task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    void createNotification(){

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Notifications");

        String id = reference.document().getId();
        Map<String, Object> map = new HashMap<>();
        map.put("time", FieldValue.serverTimestamp());
        map.put("notification", user.getDisplayName() + " has followed you.");
        map.put("id", id);
        map.put("uid", userUID);

        reference.document().set(map);
    }
}