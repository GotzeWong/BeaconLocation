package com.kyvlabs.beaconadvertiser.activities.support;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.kyvlabs.beaconadvertiser.BeaconNotifier;
import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.data.BeaconIds;
import com.kyvlabs.beaconadvertiser.data.DBBeacon;
import com.kyvlabs.beaconadvertiser.data.DBHelper;
import com.kyvlabs.beaconadvertiser.data.DataKeys;
import com.kyvlabs.beaconadvertiser.views.BeaconListViewAdapter;

import java.util.ArrayList;
import java.util.List;

//database Fragment
public class DBActivityFragment extends Fragment {

    private ListView beaconsListView;
    //parent view. Used for dataflow
    private View rootView;
    private List<DBBeacon> beaconsFromDB;

    public DBActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_db, container, false);
        loadDbData();

        return rootView;
    }

    @Override
    public void onResume() {
        loadDbData();
        super.onResume();
    }

    //Loading data from db
    private void loadDbData() {
        //Read data
        beaconsListView = (ListView) rootView.findViewById(R.id.db_beacons_list);
        DBHelper dbHelper = new DBHelper();
        beaconsFromDB = dbHelper.getBeaconsFromDB();

        BeaconListViewAdapter beaconsAdapter = new BeaconListViewAdapter(getActivity().getApplicationContext(), beaconsFromDB);

        dbHelper.close();
        //show beacons data in list
        beaconsListView.setAdapter(beaconsAdapter);

        beaconsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity().getApplicationContext(), BeaconEditActivity.class);
                BeaconIds ids = new BeaconIds(((TextView) view.findViewById(R.id.list_beacon_uuid)).getText().toString(),
                        ((TextView) view.findViewById(R.id.list_beacon_major)).getText().toString(),
                        ((TextView) view.findViewById(R.id.list_beacon_minor)).getText().toString());
                intent.putExtra(DataKeys.BEACON_IDS, ids);
                startActivity(intent);
            }
        });
    }

    //Test beacon notification
    //TODO remove
    private void testNotification(int i) {
        BeaconNotifier beaconNotifier = new BeaconNotifier(getActivity().getApplicationContext());
        BeaconIds ids = new BeaconIds(beaconsFromDB.get(i).getIds().getUuid(), beaconsFromDB.get(i).getIds().getMajor(), beaconsFromDB.get(i).getIds().getMinor());
        ArrayList<BeaconIds> idsList;
        idsList = new ArrayList<BeaconIds>();
        idsList.add(ids);
        beaconNotifier.sendNotificationAboutBeacons(idsList);
    }
}
