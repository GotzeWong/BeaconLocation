package com.kyvlabs.beaconadvertiser.activities;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.services.WalkService;

//Fragment for walk and talk buttons
public class WalkOrTalkFragment extends Fragment {

    public WalkOrTalkFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final Activity activity = getActivity();
        //loading markup
        View rootView = inflater.inflate(R.layout.fragment_walk_or_talk, container, false);
        // Creating walk button
        ImageButton walkButton = (ImageButton) rootView.findViewById(R.id.walk_button);
        walkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startService(new Intent(activity, WalkService.class));
                Toast.makeText(activity, R.string.scan_started, Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        });
        //Creating talk button
        ImageButton talkButton = (ImageButton) rootView.findViewById(R.id.talk_button);
        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.stopService(new Intent(activity, WalkService.class));
                Toast.makeText(getActivity(), R.string.scan_stopped, Toast.LENGTH_SHORT).show();
                activity.startActivity(new Intent(activity, TalkActivity.class));
            }
        });


        return rootView;
    }
}

