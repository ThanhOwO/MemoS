package com.Thanh.memos.fragments;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Thanh.memos.FragmentReplacerActivity;
import com.Thanh.memos.R;
import com.Thanh.memos.adapter.HomeAdapter;
import com.Thanh.memos.adapter.StoriesAdapter;
import com.Thanh.memos.chat.ChatUsersActivity;
import com.Thanh.memos.model.HomeModel;
import com.Thanh.memos.model.StoriesModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Home extends Fragment {
    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;
    private final MutableLiveData<Integer> commentCount = new MutableLiveData<>();
    RecyclerView storiesRecyclerView;
    StoriesAdapter storiesAdapter;
    List<StoriesModel> storiesModelList;
    ImageButton sendBtn;


    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);


        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getActivity());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestorage();

        //Create event for like, unlike post
        adapter.OnPressed(new HomeAdapter.OnPressed() {
            @Override
            public void onLiked(int position, String id, String uid, List<String> likeList, boolean isChecked) {
                DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(uid)
                        .collection("Post Images")
                        .document(id);
                if (likeList.contains(user.getUid())) {
                    likeList.remove(user.getUid()); //unlike

                } else {
                    likeList.add(user.getUid()); //like
                }

                Map<String, Object> map = new HashMap<>();
                map.put("likes", likeList);
                reference.update(map);

            }


            //Comment count
            @Override
            public void setCommentCount(TextView textView) {

                Activity activity = getActivity();

                commentCount.observe((LifecycleOwner) activity, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if(commentCount.getValue() == 0){
                            textView.setVisibility(View.GONE);
                        }else {
                            textView.setVisibility(View.VISIBLE);
                        }
                        textView.setText("See all " + commentCount.getValue() + " comments");
                    }
                });



            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatUsersActivity.class);
                startActivity(intent);
            }
        });
    }

    private void init(View view) {

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        sendBtn = view.findViewById(R.id.sendBtn);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        storiesRecyclerView = view.findViewById(R.id.storiesRecyclerView);
        storiesRecyclerView.setHasFixedSize(true);
        storiesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        storiesModelList = new ArrayList<>();
        storiesModelList.add(new StoriesModel("", "", "", "",""));
        storiesAdapter = new StoriesAdapter(storiesModelList, getActivity());
        storiesRecyclerView.setAdapter(storiesAdapter);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    //get data from Firebase storage
    private void loadDataFromFirestorage() {

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");
        reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.d("Error", error.getMessage());
                    return;
                }

                if (value == null)
                    return;

                //get post only user that followed

                List<String> uidList = (List<String>) value.get("following");

                if (uidList == null || uidList.isEmpty())
                    return;

                collectionReference.whereIn("uid", uidList)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                                if (error != null){
                                    Log.d("Error", error.getMessage());
                                }

                                if (value == null)
                                    return;

                                for (QueryDocumentSnapshot snapshot : value){

                                    snapshot.getReference().collection("Post Images")
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                    if (error != null){
                                                        Log.d("Error", error.getMessage());
                                                    }

                                                    if (value == null)
                                                        return;

                                                    list.clear();

                                                    for (QueryDocumentSnapshot snapshot : value){
                                                        //post data
                                                            if (!snapshot.exists())
                                                                return;

                                                            HomeModel model = snapshot.toObject(HomeModel.class);
                                                            list.add(new HomeModel(
                                                                    model.getName(),
                                                                    model.getProfileImage(),
                                                                    model.getImageUrl(),
                                                                    model.getUid(),
                                                                    model.getDescription(),
                                                                    model.getId(),
                                                                    model.getTimestamp(),
                                                                    model.getLikes()
                                                            ));

                                                            //get comment from database firebase
                                                            snapshot.getReference().collection("Comments").get()
                                                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                                    if (task.isSuccessful()){

                                                                                        int count = 0;
                                                                                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()){
                                                                                            count++;
                                                                                        }
                                                                                        commentCount.setValue(count);
                                                                                        //comment count
                                                                                    }
                                                                                }
                                                                            });

                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }
                                            });
                                }
                            }
                        });

                //fetch stories
                loadStories(uidList);
            }
        });
    }

    //get stories from firebase database
    void loadStories(List<String> followingList){
        Query query = FirebaseFirestore.getInstance().collection("Stories");
        query.whereIn("uid", followingList).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Log.d("Error", error.getMessage());
                }

                if (value == null)
                    return;

                list.clear();
                for (QueryDocumentSnapshot snapshot : value){

                    if(!value.isEmpty()){
                        StoriesModel model = snapshot.toObject(StoriesModel.class);
                        storiesModelList.add(model);
                    }

                }
                storiesAdapter.notifyDataSetChanged();
            }
        });
    }
}