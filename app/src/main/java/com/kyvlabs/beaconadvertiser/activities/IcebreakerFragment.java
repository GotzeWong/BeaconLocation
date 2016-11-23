package com.kyvlabs.beaconadvertiser.activities;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kyvlabs.beaconadvertiser.R;

//Fragment for Icebreaker bamnner
public class IcebreakerFragment extends Fragment {

    public IcebreakerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_icebreaker, container, false);
    }

}
