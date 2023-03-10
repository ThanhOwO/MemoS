package com.Thanh.memos.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.Thanh.memos.R;
import com.Thanh.memos.adapter.HomeAdapter;
import com.Thanh.memos.model.HomeModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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
        list.add(new HomeModel("Thanh","03/09/2023","","","19522230",45));
        list.add(new HomeModel("Tung","03/09/2023","","","45234140",12));
        list.add(new HomeModel("Thanh2","03/09/2023","","","15622141",8));
        list.add(new HomeModel("Thanh3","03/09/2023","","","85334247",78));
        list.add(new HomeModel("Thanh4","03/09/2023","","","19522230",45));
        list.add(new HomeModel("Tung2","03/09/2023","","","45234140",127));
        list.add(new HomeModel("Thanh5","03/09/2023","","","15622141",84));
        list.add(new HomeModel("Thanh6","03/09/2023","","","85334247",782));
        list.add(new HomeModel("Thanh7","03/09/2023","","","85334247",18));

        adapter.notifyDataSetChanged();

    }
}