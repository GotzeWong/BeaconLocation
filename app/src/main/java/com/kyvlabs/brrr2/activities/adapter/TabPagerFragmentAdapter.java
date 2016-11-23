package com.kyvlabs.brrr2.activities.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.activities.fragment.DetailFragment;
import com.kyvlabs.brrr2.data.DBBeacon;

import java.util.ArrayList;
import java.util.List;

public class TabPagerFragmentAdapter extends FragmentStatePagerAdapter {

    private static List<DBBeacon> beacons;

    public TabPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
        //clone from List
        beacons = new ArrayList<DBBeacon>(Application.getBeaconList().size());
        for (DBBeacon dBBeacon : Application.getBeaconList()) {
            beacons.add(dBBeacon);
        }

    }


    @Override
    public Fragment getItem(int position) {
        return DetailFragment.getInstance(position);
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return  beacons.get(position).getTitle();
    }

}
