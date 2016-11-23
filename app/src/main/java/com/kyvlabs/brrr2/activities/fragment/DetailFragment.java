package com.kyvlabs.brrr2.activities.fragment;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.data.DBBeacon;
import com.kyvlabs.brrr2.utils.DBHelper;
import com.kyvlabs.brrr2.utils.DateUtils;
import com.kyvlabs.brrr2.utils.LinkHelper;
import com.kyvlabs.brrr2.utils.StringHelper;
import com.kyvlabs.brrr2.views.HtmlTagHandler;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DetailFragment extends TitledFragment {
    private static final int LAYOUT = R.layout.fragment_detail;
    public static final String ARG_POSITION = "POSITION";

    @Bind(R.id.viewPager)
    ViewPager viewPager;

    @Bind(R.id.card_title_image)
    ImageView titleImage;
    @Bind(R.id.card_title_text)
    TextView titleText;
    @Bind(R.id.card_time_text)
    TextView timeText;
    @Bind(R.id.card_main_text)
    TextView mainText;
    @Bind(R.id.card_action_button1)
    Button actionButton1;
    @Bind(R.id.card_action_button2)
    Button actionButton2;
    @Bind(R.id.card_like_button)
    ImageButton cardHeartButton;

    DBBeacon beacon;

    int position;

    private boolean mainTextExpand = false;

    private DBHelper mydb ;

    private View v;


    public static DetailFragment getInstance(int pos) {
        Bundle args = new Bundle();

        DetailFragment fragment = new DetailFragment();
        fragment.setTitle("DetailFragment");

        args.putInt(DetailFragment.ARG_POSITION, pos);
        fragment.setArguments(args);


        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (null == v) {
            v = inflater.inflate(LAYOUT,
                    container, false);
        }

        position = this.getArguments().getInt(ARG_POSITION, 0);

        mydb = new DBHelper(Application.getAppContext());
//        ButterKnife.bind(this, v);

        TextView titleText = ButterKnife.findById(v, R.id.card_title_text);
        TextView timeText = ButterKnife.findById(v, R.id.card_time_text);
        TextView mainText = ButterKnife.findById(v, R.id.card_main_text);

        ImageView titleImage = ButterKnife.findById(v, R.id.card_title_image);

        Button actionButton1 = ButterKnife.findById(v, R.id.card_action_button1);

        Button actionButton2 = ButterKnife.findById(v, R.id.card_action_button2);
        ImageButton cardHeartButton = ButterKnife.findById(v, R.id.card_like_button);

        Bundle args = this.getArguments();
        int pos = args.getInt(ARG_POSITION, 0);

        try {
            this.beacon = Application.getBeaconList().get(position);
            Picasso picasso = Picasso.with(Application.getAppContext());
            //            if (BuildConfig.DEBUG) {
            //                picasso.setIndicatorsEnabled(true);
            //            }
            picasso.load(beacon.getPicture())
                    .error(R.drawable.no_image)
                    .into(titleImage);

            titleText.setText(beacon.getTitle());
//            timeText.setText(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.SHORT, Locale.US).format(beacon.getNextUpdateTime()) + "");

//            timeText.setText(DateUtils.timestampToDate(beacon.getNextShowTime()+""));
            timeText.setText(DateUtils.currentTimeToDate());
            mainText.setText(Html.fromHtml(beacon.getDescription(), null, new HtmlTagHandler()));

            actionButton1.setText(StringHelper.getStrRes(Application.getAppContext(),R.string.detail_btn_locate));
            actionButton2.setText(StringHelper.getStrRes(Application.getAppContext(),R.string.detail_btn_more));

            beacon.setLiked(mydb.get(beacon));
            cardHeartButton.setSelected(beacon.isLiked());

            actionButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkHelper.goToUrl(Application.getAppContext(), beacon.getLink());
//                    Toast.makeText(Application.getAppContext(), " click more", Toast.LENGTH_SHORT).show();
                }
            });

            mainText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mainTextExpand = !mainTextExpand;
                    if (mainTextExpand) {
                        ((TextView) v).setSingleLine(false);
                    } else {
                        ((TextView) v).setLines(2);
                    }
                }
            });

            cardHeartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    v.setSelected(!v.isSelected());
                    if (v.isSelected()) {
                        like();

                    } else {
                        unlike();

                    }
                }
            });

            return v;
        }catch (IndexOutOfBoundsException exception){
            return v;
        }
    }

    private void like() {

        beacon.setLiked(true);

        //add to database
        mydb.insert(beacon);
    }

    private void unlike() {

        beacon.setLiked(false);

        // delete record
        mydb.delete(beacon);
    }

}
