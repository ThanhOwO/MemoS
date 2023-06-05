package com.Thanh.memos;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.Thanh.memos.fragments.Comment;
import com.Thanh.memos.fragments.CreateAccountFragment;
import com.Thanh.memos.fragments.LoginFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FragmentReplacerActivity extends AppCompatActivity {

    private FrameLayout frameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_replacer);

        frameLayout = findViewById(R.id.frameLayout);

        boolean isComment = getIntent().getBooleanExtra("isComment", false);
        if(isComment)
            setFragment(new Comment());
        else
            setFragment(new LoginFragment());
    }

    //animation for changing page
    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if(fragment instanceof CreateAccountFragment){
            fragmentTransaction.addToBackStack(null);
        }

        if (fragment instanceof Comment){
            String id = getIntent().getStringExtra("id");
            String uid = getIntent().getStringExtra("uid");

            Bundle bundle = new Bundle();
            bundle.putString("id", id);
            bundle.putString("uid", uid);
            fragment.setArguments(bundle);

        }

        fragmentTransaction.replace(frameLayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}