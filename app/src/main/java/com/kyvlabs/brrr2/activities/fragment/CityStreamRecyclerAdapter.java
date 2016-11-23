package com.kyvlabs.brrr2.activities.fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
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

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CityStreamRecyclerAdapter extends RecyclerView.Adapter<CityStreamRecyclerAdapter.ViewHolder> {

    private final List<DBBeacon> beacons;
    private DBHelper mydb ;
    private Context context;

    public CityStreamRecyclerAdapter(Context context, List<DBBeacon> beacons) {
        this.beacons = beacons;
        mydb = new DBHelper(Application.getAppContext());
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.stream_card, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        viewHolder.setData(i);
    }

    @Override
    public int getItemCount() {
        return beacons.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

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

            private boolean mainTextExpand = false;
            private DBBeacon beacon;
            private int pos;


        ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

//                    Toast.makeText(Application.getAppContext(), " click View", Toast.LENGTH_SHORT).show();

                    ExampleFragment fragment = new ExampleFragment();
                    Bundle args = new Bundle();
                    args.putInt(ExampleFragment.ARG_POSITION, pos);
                    fragment.setArguments(args);

                    // Insert the fragment by replacing any existing fragment
                    FragmentTransaction transaction = Application.getFragmentManager().beginTransaction();
                    // Replace whatever is in thefragment_container view with this fragment,
                    // and add the transaction to the backstack
                    transaction.replace(R.id.container, fragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });
        }

        public void setData(int i) {
            pos = i;
            this.beacon = beacons.get(pos);
            Picasso picasso = Picasso.with(Application.getAppContext());
//            if (BuildConfig.DEBUG) {
//                picasso.setIndicatorsEnabled(true);
//            }
            picasso.load(beacon.getPicture())
                    .error(R.drawable.no_image)
                    .into(titleImage);

            titleText.setText(beacon.getTitle());
            titleText.setText(beacon.getIds().toString());

            timeText.setText(DateUtils.currentTimeToDate());
            mainText.setText(beacon.getIds().getRssi().toString());
//            timeText.setText(beacon.getNextUpdateTime()+"");
            if(null != beacon.getDescription() && beacon.getDescription().length() > 0)
                mainText.setText(Html.fromHtml(beacon.getDescription(), null, new HtmlTagHandler()));

            actionButton1.setText(StringHelper.getStrRes(Application.getAppContext(),R.string.detail_btn_locate));
            actionButton2.setText(StringHelper.getStrRes(Application.getAppContext(),R.string.detail_btn_more));

//            cardHeartButton.setSelected(beacon.isLiked());
            beacon.setLiked(mydb.get(beacon));
            cardHeartButton.setSelected(beacon.isLiked());

            actionButton2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(Application.getAppContext(), beacon.getLink(), Toast.LENGTH_SHORT).show();
//                    new UrlTypeTask().execute(beacon.getLink());
                    try {
                        String type = LinkHelper.getMimeType(beacon.getLink());
                        LinkHelper.goToUrl(context, beacon.getLink());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @OnClick(R.id.card_like_button)
        public void likeToggle(View button) {
            button.setSelected(!button.isSelected());
            if (button.isSelected()) {
                like();
            } else {
                unlike();
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

        @OnClick(R.id.card_main_text)
        public void expandMainTextClick(View v) {
            mainTextExpand = !mainTextExpand;
            if (mainTextExpand) {
                ((TextView) v).setSingleLine(false);
            } else {
                ((TextView) v).setLines(2);
            }
        }

        class UrlTypeTask extends AsyncTask<String, Void, String> {

            private Exception exception;

            protected String doInBackground(String... urls) {
                try {
                    String type = LinkHelper.getMimeType(beacon.getLink());
                    return type;
                } catch (Exception e) {
                    this.exception = e;
                    return null;
                }
            }

            protected void onPostExecute(String feed) {
                // TODO: check this.exception
                // TODO: do something with the feed
                Toast.makeText(context,feed, Toast.LENGTH_SHORT).show();

            }
        }
    }

}