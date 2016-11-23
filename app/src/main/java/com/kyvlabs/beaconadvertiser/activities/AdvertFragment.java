package com.kyvlabs.beaconadvertiser.activities;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.activities.support.LinkTransformationMethod;
import com.kyvlabs.beaconadvertiser.activities.support.chrometabs.ChromeTabFallback;
import com.kyvlabs.beaconadvertiser.activities.support.chrometabs.CustomTabActivityHelper;
import com.kyvlabs.beaconadvertiser.data.BeaconIds;
import com.kyvlabs.beaconadvertiser.data.DBHelper;
import com.kyvlabs.beaconadvertiser.data.DataKeys;
import com.kyvlabs.beaconadvertiser.views.HtmlTagHandler;
import com.squareup.picasso.Picasso;

//Fragment to Advert activity
public class AdvertFragment extends Fragment {
    //TODO set from properties and 10 min
    //interval for pauses between adverts showing
    private static final long NOT_SHOW_INTERVAL = 1000 * 60 * 10;

    public AdvertFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Finding UI elements
        View rootView = inflater.inflate(R.layout.fragment_advert, container, false);

        TextView titleText = (TextView) rootView.findViewById(R.id.ad_title_text);
        TextView descriptionText = (TextView) rootView.findViewById(R.id.ad_description_text);
        ImageButton xButton = (ImageButton) rootView.findViewById(R.id.x_button);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        ImageButton QButton = (ImageButton) rootView.findViewById(R.id.q_button);

        //Reading data from bundle and show it on ui.
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String title = bundle.getString(DataKeys.AD_TITLE_KEY, getResources().getString(R.string.no_title));
            titleText.setText(title);
            String groupName = bundle.getString(DataKeys.AD_GROUP_NAME, getResources().getString(R.string.no_title));
            getActivity().setTitle(groupName);
            String descriptionString = bundle.getString(DataKeys.AD_DESCRIPTION_KEY, getResources().getString(R.string.no_description));
            descriptionText.setText(Html.fromHtml(descriptionString, null, new HtmlTagHandler()));
            descriptionText.setTransformationMethod(new LinkTransformationMethod(getActivity()));
            descriptionText.setMovementMethod(LinkMovementMethod.getInstance());
            String picturePath = bundle.getString(DataKeys.AD_PICTURE_KEY, getResources().getString(R.string.no_picture));
            final String link = bundle.getString(DataKeys.AD_LINK_KEY, "https://ru.wikipedia.org/wiki/IBeacon");
            QButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!"".equals(link)) {
                        CustomTabActivityHelper.openCustomTab(getActivity(), Uri.parse(link), new ChromeTabFallback());
                    }
                }
            });
            ImageView imageView = (ImageView) rootView.findViewById(R.id.background);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(getActivity()).load(picturePath).into(imageView);
            final BeaconIds ids = bundle.getParcelable(DataKeys.BEACON_IDS);

            String advertReason = bundle.getString(DataKeys.ADVERT_REASON_KEY, "");

            if (advertReason.equals(DataKeys.ADVERT_WALK_REASON)) {
                xButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DBHelper dbHelper = new DBHelper();
                        dbHelper.setNextShowTime(ids, System.currentTimeMillis() + NOT_SHOW_INTERVAL);
                        getActivity().finish();
                    }
                });
            }
        }
        return rootView;
    }
}

