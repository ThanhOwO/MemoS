package com.Thanh.memos.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

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
import java.util.List;

public class ChatUsersActivity extends AppCompatActivity {

    ChatUserAdapter adapter;
    List<ChatUserModel> list;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_users);

        init();

        fetchUserData();
    }

    void init(){
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        list = new ArrayList<>();
        adapter = new ChatUserAdapter(this, list);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(adapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    void fetchUserData(){
        CollectionReference reference = FirebaseFirestore.getInstance().collection("Messages");
        reference.whereArrayContains("uid", user.getUid())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(error != null)
                            return;

                        if (value.isEmpty())
                            return;

                        list.clear();
                        for(QueryDocumentSnapshot snapshot : value){
                            ChatUserModel model = snapshot.toObject(ChatUserModel.class);
                            list.add(model);
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
}