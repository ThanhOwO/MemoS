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
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends Fragment {

    private TextView nameTv, toolbarNameTv, statusTv, followingCountTv, followersCountTv, postCountTv, emptyGallery;
    private CircleImageView profileImage;
    private Button followBtn, startChatBtn;
    private RecyclerView recyclerView;

    private LinearLayout countLayout;
    private FirebaseUser user;
    private ImageButton editprofileBtn, optionBtn, editName, editStatus;
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
            editName.setVisibility(View.VISIBLE);
            editStatus.setVisibility(View.VISIBLE);
        }else {
            editprofileBtn.setVisibility(View.GONE);
            followBtn.setVisibility(View.VISIBLE);
            editName.setVisibility(View.GONE);
            editStatus.setVisibility(View.GONE);
            optionBtn.setVisibility(View.GONE);

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
                    @SuppressLint("NonConstantResourceId")
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_setting:
                                // Handle "Setting" button click
                                return true;
                            case R.id.action_logout:
                                userRef.update("online", false);

                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getActivity(), FragmentReplacerActivity.class);
                                intent.putExtra("isComment", false);
                                startActivity(intent);

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

        editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditNameDialog(Gravity.CENTER);
            }
        });

        editStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openEditStatusDialog(Gravity.CENTER);
            }
        });
    }

    private void openEditNameDialog(int gravity){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_editname);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttribute = window.getAttributes();

        windowAttribute.gravity = gravity;
        window.setAttributes(windowAttribute);

        if(Gravity.CENTER == gravity){
            dialog.setCancelable(true);
        }
        else {
            dialog.setCancelable(false);
        }

        EditText editName = dialog.findViewById(R.id.editnameET);
        Button saveBtn = dialog.findViewById(R.id.saveBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
               String currentname = documentSnapshot.getString("name");
                if (currentname != null) {
                    String currentNameWithEllipsis = currentname + "...";
                    editName.setHint(currentNameWithEllipsis);
                }
            }
        }).addOnFailureListener(e -> {
            editName.setHint("Cannot get current name!");
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newUsername = editName.getText().toString().trim();
                String lowercaseUsername = newUsername.toLowerCase();

                Map<String, Object> updates = new HashMap<>();
                updates.put("name", newUsername);
                updates.put("search", lowercaseUsername);

                if (!newUsername.isEmpty()){
                    userRef.update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Username updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update username", Toast.LENGTH_SHORT).show();
                            });
                }else{
                    Toast.makeText(getContext(), "Please enter a username", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.show();
    }

    private void openEditStatusDialog(int gravity){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_editstatus);

        Window window = dialog.getWindow();
        if(window == null){
            return;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams windowAttribute = window.getAttributes();

        windowAttribute.gravity = gravity;
        window.setAttributes(windowAttribute);

        if(Gravity.CENTER == gravity){
            dialog.setCancelable(true);
        }
        else {
            dialog.setCancelable(false);
        }

        EditText editStatus = dialog.findViewById(R.id.editstatusET);
        Button saveBtn = dialog.findViewById(R.id.saveBtn);
        Button cancelBtn = dialog.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()){
                String currentstatus = documentSnapshot.getString("status");
                if (currentstatus != null) {
                    String currentNameWithEllipsis = currentstatus + "...";
                    editStatus.setHint(currentNameWithEllipsis);
                }
            }
        }).addOnFailureListener(e -> {
            editStatus.setHint("Cannot get current status!");
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newStatus = editStatus.getText().toString().trim();

                if (!newStatus.isEmpty()){
                    userRef.update("status", newStatus)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }).addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Failed to update status", Toast.LENGTH_SHORT).show();
                            });
                }else{
                    Toast.makeText(getContext(), "Please enter a status", Toast.LENGTH_SHORT).show();
                }
            }
        });



        dialog.show();
    }

    void queryChat() {
        assert getContext() != null;
        StylishAlertDialog alertDialog = new StylishAlertDialog(getContext(), StylishAlertDialog.PROGRESS);
        alertDialog.setTitleText("Starting Chat...");
        alertDialog.setCancelable(false);
        alertDialog.show();

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", userUID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();

                    if (snapshot.isEmpty()) {
                        startChat(alertDialog);
                    } else {
                        boolean chatFound = false;
                        for (DocumentSnapshot snapshotChat : snapshot) {
                            List<String> uidList = (List<String>) snapshotChat.get("uid");
                            if (uidList != null && uidList.contains(user.getUid())) {
                                chatFound = true;
                                String chatId = snapshotChat.getId();
                                navigateToChat(alertDialog, chatId);
                                break;
                            }
                        }

                        if (!chatFound) {
                            startChat(alertDialog);
                        }
                    }
                } else {
                    alertDialog.dismissWithAnimation();
                }
            }
        });
    }

    void startChat(StylishAlertDialog alertDialog) {
        // Starting a conversation
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

        reference.document(pushID).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    navigateToChat(alertDialog, pushID);
                } else {
                    alertDialog.dismissWithAnimation();
                }
            }
        });
    }

    void navigateToChat(StylishAlertDialog alertDialog, String chatId) {
        alertDialog.dismissWithAnimation();
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("uid", userUID);
        intent.putExtra("id", chatId);
        startActivity(intent);
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
        editName = view.findViewById(R.id.edit_name);
        editStatus = view.findViewById(R.id.edit_status);


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
                                .placeholder(R.drawable.profile)
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

                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openEditPost(Gravity.CENTER, model.getImageUrl(), model.getId(), model.getDescription());
                    }
                });

            }

             private void openEditPost(int gravity, String imageUrl, String postId, String descriptionText){
                 final Dialog dialog = new Dialog(getContext());
                 dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                 dialog.setContentView(R.layout.dialog_editpost);

                 Window window = dialog.getWindow();
                 if(window == null){
                     return;
                 }
                 window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                 window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                 WindowManager.LayoutParams windowAttribute = window.getAttributes();

                 windowAttribute.gravity = gravity;
                 window.setAttributes(windowAttribute);

                 if(Gravity.CENTER == gravity){
                     dialog.setCancelable(true);
                 }
                 else {
                     dialog.setCancelable(false);
                 }

                 ImageView img = dialog.findViewById(R.id.postImg);
                 ImageButton imgBtn = dialog.findViewById(R.id.deletePost);
                 TextView description = dialog.findViewById(R.id.postDes);
                 ImageButton editBtn = dialog.findViewById(R.id.edit_description);
                 EditText editDes = dialog.findViewById(R.id.postdesET);
                 Button saveBtn = dialog.findViewById(R.id.saveBtn);
                 Button cancelBtn = dialog.findViewById(R.id.cancelBtn);

                 int radius = (int) getResources().getDisplayMetrics().density * 16; // Convert 16dp to pixels
                 Glide.with(dialog.getContext().getApplicationContext())
                         .load(imageUrl)
                         .transform(new CenterCrop(), new RoundedCorners(radius))
                         .into(img);

                 imgBtn.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         showConfirmationDialog(dialog, postId);
                     }
                 });

                 description.setText(descriptionText);
                 editBtn.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         editDes.setVisibility(View.VISIBLE);
                         editBtn.setVisibility(View.GONE);
                         editDes.setHint(description.getText().toString());
                     }
                 });

                 cancelBtn.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         dialog.dismiss();
                     }
                 });

                 saveBtn.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         String updatedDescription = editDes.getText().toString().trim();
                         // Perform update operation using Firestore
                         updatePostDescription(postId, updatedDescription);
                         description.setText(updatedDescription);
                         editDes.setVisibility(View.GONE);
                         editBtn.setVisibility(View.VISIBLE);
                     }
                 });

                 if (!isMyProfile){
                     imgBtn.setVisibility(View.GONE);
                     editBtn.setVisibility(View.GONE);
                     saveBtn.setVisibility(View.GONE);
                     cancelBtn.setVisibility(View.GONE);
                 }

                 dialog.show();
             }

             private void showConfirmationDialog(Dialog previousDialog, String postId) {
                 // Create and set up your custom dialog using your confirm dialog layout
                 final Dialog confirmationDialog = new Dialog(getContext());
                 confirmationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                 confirmationDialog.setContentView(R.layout.confirm_dialog);

                 Window window = confirmationDialog.getWindow();
                 if(window == null){
                     return;
                 }
                 window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                 window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                 WindowManager.LayoutParams windowAttribute = window.getAttributes();

                 // Set up your dialog's views and buttons as needed
                 Button yesButton = confirmationDialog.findViewById(R.id.yesBtn);
                 Button noButton = confirmationDialog.findViewById(R.id.noBtn);

                 // Set up the button click listeners
                 yesButton.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         // Perform delete operation using Firestore
                         deletePost(postId);
                         previousDialog.dismiss();
                         confirmationDialog.dismiss();
                     }
                 });

                 noButton.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         confirmationDialog.dismiss();
                     }
                 });

                 // Show the custom confirmation dialog
                 confirmationDialog.show();
             }

             private void deletePost(String postId) {
                 // Get the reference to the "Post Images" collection of the current user
                 DocumentReference postRef = FirebaseFirestore.getInstance()
                         .collection("Users")
                         .document(userUID)
                         .collection("Post Images")
                         .document(postId);

                 // Delete the document
                 postRef.delete()
                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                 // Deletion successful
                                 Toast.makeText(getContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                             }
                         })
                         .addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 // Error occurred while deleting
                                 Toast.makeText(getContext(), "Failed to delete post", Toast.LENGTH_SHORT).show();
                             }
                         });

                 // Get the reference to the "Comments" subcollection within the document
                 CollectionReference commentsRef = postRef.collection("Comments");

                 // Delete all the documents within the "Comments" subcollection
                 commentsRef.get()
                         .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                             @Override
                             public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                 // Delete each document within the "Comments" subcollection
                                 for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                     DocumentReference commentRef = commentsRef.document(documentSnapshot.getId());
                                     commentRef.delete();
                                 }
                             }
                         })
                         .addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 // Error occurred while deleting the "Comments" subcollection
                                 Toast.makeText(getContext(), "Failed to delete comments", Toast.LENGTH_SHORT).show();
                             }
                         });
             }


             private void updatePostDescription(String postId, String updatedDescription) {
                 // Get the reference to the "Post Images" collection of the current user
                 DocumentReference reference = FirebaseFirestore.getInstance()
                         .collection("Users")
                         .document(userUID)
                         .collection("Post Images")
                         .document(postId);

                 reference.update("description", updatedDescription)
                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid) {
                                 // Update successful
                                 Toast.makeText(getContext(), "Description updated successfully", Toast.LENGTH_SHORT).show();
                             }
                         })
                         .addOnFailureListener(new OnFailureListener() {
                             @Override
                             public void onFailure(@NonNull Exception e) {
                                 // Error occurred while updating
                                 Toast.makeText(getContext(), "Failed to update description", Toast.LENGTH_SHORT).show();
                             }
                         });
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
        map.put("oppositeID", user.getUid());

        reference.document().set(map);
    }
}