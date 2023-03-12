package com.Thanh.memos.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.Thanh.memos.R;
import com.Thanh.memos.adapter.HomeAdapter;
import com.Thanh.memos.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Home extends Fragment {

    private RecyclerView recyclerView;
    HomeAdapter adapter;
    private List<HomeModel> list;
    private FirebaseUser user;
    DocumentReference reference;

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
        //reference = FirebaseFirestore.getInstance().collection("Posts").document(user.getUid());
        list = new ArrayList<>();
        adapter = new HomeAdapter(list, getContext());
        recyclerView.setAdapter(adapter);

        loadDataFromFirestorage();
    }

    private void init(View view){

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if(getActivity() != null)
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
    }

    //get data from Firebase storage
    private void loadDataFromFirestorage(){

        CollectionReference reference = FirebaseFirestore.getInstance().collection("Users")
                        .document(user.getUid())
                        .collection("Post Images");
        reference.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null) {
                    Log.e("Error: ", error.getMessage());
                    return;
                }
                assert value != null;
                for(QueryDocumentSnapshot snapshot : value){

                    list.add(new HomeModel(snapshot.get("userName").toString(), snapshot.get("").toString(),
                            snapshot.get("timestamp").toString(),
                            snapshot.get("profileImage").toString(),
                            snapshot.get("postImage").toString(),
                            snapshot.get("uid").toString(),
                            snapshot.get("comments").toString(),
                            snapshot.get("description").toString(),
                            snapshot.get("id").toString(),
                            Integer.parseInt(snapshot.get("likeCount").toString())
                    ));
                    adapter.notifyDataSetChanged();
                }
            }
        });

        adapter.notifyDataSetChanged();

    }
}