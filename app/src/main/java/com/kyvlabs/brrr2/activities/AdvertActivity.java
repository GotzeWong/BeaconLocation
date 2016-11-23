package com.kyvlabs.brrr2.activities;

import android.app.FragmentManager;
import android.os.Bundle;

import com.kyvlabs.brrr2.R;

//Activity witch showing acdvert. Fragment container.
public class AdvertActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert);

        FragmentManager fragmentManager = getFragmentManager();

        AdvertFragment advertFragment = new AdvertFragment();
        advertFragment.setArguments(getIntent().getExtras());

        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, advertFragment)
                    .commit();
        }
    }

}
