package com.Thanh.memos.fragments;


import android.annotation.SuppressLint;
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
import com.Thanh.memos.model.CommentModel;
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
import java.util.Collections;
import java.util.Comparator;
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
    LinearLayout introduce;


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

                assert activity != null;
                commentCount.observe((LifecycleOwner) activity, new Observer<Integer>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onChanged(Integer integer) {
                        if(commentCount.getValue() == 0){
                            textView.setVisibility(View.VISIBLE);
                        }else {
                            textView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = user.getUid();
                DocumentReference userRef = FirebaseFirestore.getInstance().collection("Users").document(uid);
                userRef.update("online", false);
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

        introduce = view.findViewById(R.id.introduce);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    //get data from Firebase storage
    private void loadDataFromFirestorage() {

        final DocumentReference reference = FirebaseFirestore.getInstance().collection("Users")
                .document(user.getUid());

        final CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("Users");
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
                            public void onEvent(@Nullable QuerySnapshot value1, @Nullable FirebaseFirestoreException error1) {

                                if (error1 != null){
                                    Log.d("Error", error1.getMessage());
                                }

                                if (value1 == null)
                                    return;


                                for (QueryDocumentSnapshot snapshot : value1){

                                    snapshot.getReference().collection("Post Images")
                                            .orderBy("timestamp", Query.Direction.DESCENDING)
                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                @Override
                                                public void onEvent(@Nullable QuerySnapshot value11, @Nullable FirebaseFirestoreException error11) {
                                                    if (error11 != null){
                                                        Log.d("Error", error11.getMessage());
                                                    }

                                                    if (value11 == null)
                                                        return;

                                                    for (QueryDocumentSnapshot snapshot1 : value11){
                                                        //post data
                                                            if (!snapshot1.exists())
                                                                return;

                                                            HomeModel model = snapshot1.toObject(HomeModel.class);
                                                            String postId = model.getId();

                                                            boolean postExists = false;
                                                            for (HomeModel existingPost : list) {
                                                                if (existingPost.getId().equals(postId)) {
                                                                    postExists = true;
                                                                    break;
                                                                }
                                                            }

                                                            if (!postExists) {
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
                                                            }

                                                            //get comment from database firebase
                                                        snapshot1.getReference().collection("Comments")
                                                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                                                .get()
                                                                .addOnCompleteListener(task -> {
                                                                    if (task.isSuccessful()) {
                                                                        List<CommentModel> comments = new ArrayList<>();
                                                                        for (QueryDocumentSnapshot commentSnapshot : task.getResult()) {
                                                                            CommentModel commentModel = commentSnapshot.toObject(CommentModel.class);
                                                                            comments.add(commentModel);
                                                                        }

                                                                        // Sort the comments by timestamp in descending order
                                                                        Collections.sort(comments, new Comparator<CommentModel>() {
                                                                            @Override
                                                                            public int compare(CommentModel o1, CommentModel o2) {
                                                                                return o2.getTimestamp().compareTo(o1.getTimestamp()); // Compare in reverse order
                                                                            }
                                                                        });

                                                                        commentCount.setValue(comments.size());
                                                                    }
                                                                });
                                                    }
                                                    Collections.sort(list, new Comparator<HomeModel>() {
                                                        @Override
                                                        public int compare(HomeModel o1, HomeModel o2) {
                                                            return o2.getTimestamp().compareTo(o1.getTimestamp()); // Compare in reverse order
                                                        }
                                                    });
                                                    if (list.isEmpty()) {
                                                        introduce.setVisibility(View.VISIBLE);
                                                    } else {
                                                        introduce.setVisibility(View.GONE);
                                                    }
                                                    adapter.notifyDataSetChanged();
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
    void loadStories(List<String> followingList) {
        storiesModelList.clear();
        Query query = FirebaseFirestore.getInstance().collection("Stories");
        query.whereIn("uid", followingList).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            StoriesModel model = snapshot.toObject(StoriesModel.class);
                            storiesModelList.add(model);
                        }
                    }
                    storiesAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.d("Error: ", e.getMessage());
                });
    }

}