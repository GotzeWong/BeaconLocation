package com.kyvlabs.brrr2.activities;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.activities.support.LinkTransformationMethod;
import com.kyvlabs.brrr2.activities.support.chrometabs.ChromeTabFallback;
import com.kyvlabs.brrr2.activities.support.chrometabs.CustomTabActivityHelper;
import com.kyvlabs.brrr2.data.BeaconIds;
import com.kyvlabs.brrr2.data.DBHelper;
import com.kyvlabs.brrr2.data.DataKeys;
import com.kyvlabs.brrr2.utils.DateUtils;
import com.kyvlabs.brrr2.utils.StringHelper;
import com.kyvlabs.brrr2.views.HtmlTagHandler;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.ButterKnife;

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
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        TextView titleText = (TextView) rootView.findViewById(R.id.card_title_text);
        TextView descriptionText = (TextView) rootView.findViewById(R.id.card_main_text);
        TextView timeText = (TextView) rootView.findViewById(R.id.card_time_text);

        Button xButton = (Button) rootView.findViewById(R.id.card_action_button2);
        xButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        xButton.setText(StringHelper.getStrRes(Application.getAppContext(),R.string.detail_btn_quit));

        Button QButton = (Button) rootView.findViewById(R.id.card_action_button1);
        QButton.setText(StringHelper.getStrRes(Application.getAppContext(),R.string.detail_btn_more));

        //Reading data from bundle and show it on ui.
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            String title = bundle.getString(DataKeys.AD_TITLE_KEY, getResources().getString(R.string.no_title));
            getActivity().setTitle(title);
            titleText.setText(title);
            String descriptionString = bundle.getString(DataKeys.AD_DESCRIPTION_KEY, getResources().getString(R.string.no_description));
            descriptionText.setText(Html.fromHtml(descriptionString, null, new HtmlTagHandler()));
            descriptionText.setTransformationMethod(new LinkTransformationMethod(getActivity()));
            descriptionText.setMovementMethod(LinkMovementMethod.getInstance());
            String timestamp = bundle.getString(DataKeys.AD_TIME_KEY, System.currentTimeMillis()+"");
//            timeText.setText(DateUtils.timestampToDate(timestamp));
            timeText.setText(DateUtils.currentTimeToDate());
            String picturePath = bundle.getString(DataKeys.AD_PICTURE_KEY, getResources().getString(R.string.no_picture));
            final String link = bundle.getString(DataKeys.AD_LINK_KEY, "https://ru.wikipedia.org/wiki/IBeacon");
            QButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomTabActivityHelper.openCustomTab(getActivity(), Uri.parse(link), new ChromeTabFallback());
                }
            });
            ImageView imageView = (ImageView) rootView.findViewById(R.id.card_title_image);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
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

