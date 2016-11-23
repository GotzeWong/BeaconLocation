package com.kyvlabs.brrr2.activities.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.activities.adapter.TabPagerFragmentAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ExampleFragment extends TitledFragment {
    private static final int LAYOUT = R.layout.fragment_example;
    public static final String ARG_POSITION = "POSITION";


    @Bind(R.id.viewPager)
    ViewPager viewPager;

    Bundle args;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(LAYOUT, container, false);
        ButterKnife.bind(this, v);


        args = this.getArguments();
        setViewPager();
//        Toast.makeText(Application.getAppContext(), args.getInt(ARG_POSITION, 0)+"", Toast.LENGTH_SHORT).show();

        return v;
    }

    private void setViewPager() {

        TabPagerFragmentAdapter adapter = new TabPagerFragmentAdapter(Application.getFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem( args.getInt(ARG_POSITION, 0));
//        viewPager.setOffscreenPageLimit(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //do nothing
            }

            @Override
            public void onPageSelected(int position) {
//                Toast.makeText(MainActivity.this, "click " + position, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                //do nothing
            }
        });

    }

}
