package com.Thanh.memos.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.Thanh.memos.R;
import com.Thanh.memos.adapter.ChatUserAdapter;
import com.Thanh.memos.model.ChatUserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatUsersActivity extends AppCompatActivity {

    ChatUserAdapter adapter;
    List<ChatUserModel> list;
    FirebaseUser user;
    LinearLayout waitingmessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);

        init();

        fetchUserData();

        clickListener();
    }

    void init(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        adapter = new ChatUserAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        waitingmessage = findViewById(R.id.gif);
    }

    void fetchUserData(){
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", user.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null)
                            return;

                        if (value.isEmpty()){
                            waitingmessage.setVisibility(View.VISIBLE);
                        }else{
                            waitingmessage.setVisibility(View.GONE);
                        }

                        list.clear();
                        for(QueryDocumentSnapshot snapshot : value){
                            if (snapshot.exists()){
                                ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                                list.add(model);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }

    void clickListener(){
        adapter.OnStartChat(new ChatUserAdapter.OnStartChat() {
            @Override
            public void clicked(int position, List<String> uids, String chatID) {
                String oppositeUID;
                if (!uids.get(0).equalsIgnoreCase(user.getUid())){
                    oppositeUID = uids.get(0);
                }else {
                    oppositeUID = uids.get(1);
                }
                Intent intent = new Intent(ChatUsersActivity.this, ChatActivity.class);
                intent.putExtra("uid", oppositeUID);
                intent.putExtra("id", chatID);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus(true);
    }

    @Override
    protected void onPause() {
        updateStatus(false);
        super.onPause();
    }

    void updateStatus(boolean status){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            Map<String, Object> map = new HashMap<>();
            map.put("online", status);

            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(userId)
                    .update(map);
        } else {
            // Handle the situation when the FirebaseUser object is null
        }
    }
}