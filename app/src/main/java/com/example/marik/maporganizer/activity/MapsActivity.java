package com.example.marik.maporganizer.activity;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import com.example.marik.maporganizer.R;
import com.example.marik.maporganizer.adapters.SectionPagerAdapter;
import com.example.marik.maporganizer.fragments.MapsFragment;

public class MapsActivity extends AppCompatActivity {

     ViewPager viewPager;


     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MapsFragment());

        viewPager.setAdapter(adapter);
    }

}