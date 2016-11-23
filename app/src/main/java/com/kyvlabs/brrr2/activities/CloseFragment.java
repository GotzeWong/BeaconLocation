package com.kyvlabs.brrr2.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.services.WalkService;

//Fragment for close button
public class CloseFragment extends Fragment {

    public CloseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_close, container, false);

        ImageButton xButton = (ImageButton) rootView.findViewById(R.id.x_button);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().stopService(new Intent(getActivity(), WalkService.class));
                Toast.makeText(getActivity(), R.string.scan_stopped, Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });

        return rootView;
    }
}
