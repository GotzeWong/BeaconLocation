package com.kyvlabs.brrr2.views;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.data.DBBeacon;

import java.util.List;

//Ads list view adapter
public class AdsListViewAdapter extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final List<DBBeacon> beacons;

    public AdsListViewAdapter(Context context, List<DBBeacon> beacons) {
        this.beacons = beacons;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Object getItem(int position) {
        return beacons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.ads_list_item, parent, false);
        }

        DBBeacon beacon = (DBBeacon) getItem(position);
        ((TextView) view.findViewById(R.id.list_beacon_title)).setText(beacon.getTitle());
        ((TextView) view.findViewById(R.id.list_beacon_description)).setText(Html.fromHtml(beacon.getDescription(), null, new HtmlTagHandler()));

        return view;
    }
}