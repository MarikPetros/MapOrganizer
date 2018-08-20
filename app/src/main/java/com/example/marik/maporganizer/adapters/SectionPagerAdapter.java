package com.example.marik.maporganizer.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.marik.maporganizer.fragments.FragmentTaskCreation;
import com.example.marik.maporganizer.fragments.MapsFragment;

import java.util.ArrayList;
import java.util.List;
// stores fragments for tabs


public class SectionPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> mFragmentList= new ArrayList<>();


    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public  void addFragment(Fragment fragment){
        mFragmentList.add(fragment);
    }

}