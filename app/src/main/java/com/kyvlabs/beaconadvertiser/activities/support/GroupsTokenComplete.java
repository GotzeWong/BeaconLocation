package com.kyvlabs.beaconadvertiser.activities.support;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kyvlabs.beaconadvertiser.R;
import com.tokenautocomplete.TokenCompleteTextView;

public class GroupsTokenComplete extends TokenCompleteTextView<String> {
    public GroupsTokenComplete(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View getViewForObject(String group) {

        LayoutInflater l = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        LinearLayout view = (LinearLayout) l.inflate(R.layout.group_token, (ViewGroup) GroupsTokenComplete.this.getParent(), false);
        ((TextView) view.findViewById(R.id.text)).setText(group);

        return view;
    }

    @Override
    protected String defaultObject(String completionText) {
        return completionText.trim();
    }
}