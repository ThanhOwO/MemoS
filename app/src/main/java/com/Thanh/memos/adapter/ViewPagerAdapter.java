package com.Thanh.memos.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.Thanh.memos.fragments.Add;
import com.Thanh.memos.fragments.Home;
import com.Thanh.memos.fragments.Notification;
import com.Thanh.memos.fragments.Profile;
import com.Thanh.memos.fragments.Search;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    int noOfTabs;

    public ViewPagerAdapter(@NonNull FragmentManager fm, int tabCount) {
        super(fm);
        this.noOfTabs = noOfTabs;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Home();

            case 1:
                return new Search();

            case 2:
                return new Add();

            case 3:
                return new Notification();

            case 4:
                return new Profile();

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }
}
