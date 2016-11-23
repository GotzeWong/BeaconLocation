package com.kyvlabs.beaconadvertiser.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.activities.support.GroupsTokenComplete;
import com.kyvlabs.beaconadvertiser.network.NetworkHelper;
import com.kyvlabs.beaconadvertiser.network.model.Group;
import com.kyvlabs.beaconadvertiser.network.model.LoginModel;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action0;
import rx.functions.Action1;

public class ListOfGroupsDialog extends DialogFragment {
    private Map<String, Integer> groupsMap = new HashMap<>();
    private GroupsTokenComplete groupTextView;
    private ProgressBar progriss;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View inflate = inflater.inflate(R.layout.setting_list_of_group_dialog, null);

        builder.setMessage(R.string.dialog_list_of_groups)
                .setView(inflate)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        reLogin(getSelectedGroupsIds());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        progriss = (ProgressBar) inflate.findViewById(R.id.progress);

        groupTextView = (GroupsTokenComplete) inflate.findViewById(R.id.group_select);
        groupTextView.allowDuplicates(false);
        groupTextView.setDeletionStyle(TokenCompleteTextView.TokenDeleteStyle.Clear);

        NetworkHelper networkHelper = new NetworkHelper();
        showProgress(true);
        networkHelper.getGroups().subscribe(new Action1<List<Group>>() {
            @Override
            public void call(List<Group> groups) {
                Log.d("GETGROUPS", groups.toString());
                for (Group group : groups) {
                    groupsMap.put(group.getName(), group.getId());
                }
                List<String> list = new ArrayList<>(groupsMap.keySet());
                String[] groupsForAdapter = list.toArray(new String[list.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), R.layout.group_select_list_item, groupsForAdapter);
                groupTextView.setAdapter(adapter);
                showProgress(false);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(getActivity().getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });

        return builder.create();
    }

    public ArrayList<Integer> getSelectedGroupsIds() {
        ArrayList<Integer> groupsIds = new ArrayList<>();
        List<String> selectedGroupsNames = groupTextView.getObjects();
        for (String groupName : selectedGroupsNames) {
            if (groupsMap.containsKey(groupName)) {
                groupsIds.add(groupsMap.get(groupName));
            }
        }
        return groupsIds;
    }

    public void showProgress(final boolean show) {
        progriss.setVisibility(show ? View.VISIBLE : View.GONE);
        groupTextView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void reLogin(ArrayList<Integer> groups) {
        SharedPreferences userDetails = getActivity().getSharedPreferences(getString(R.string.saved_auth_key), Context.MODE_PRIVATE);
        String email = userDetails.getString(getString(R.string.saved_email), "");
        String password = userDetails.getString(getString(R.string.saved_password), "");

        NetworkHelper networkHelper = new NetworkHelper();
        networkHelper.login(email, password, groups).subscribe(new Action1<LoginModel>() {
            @Override
            public void call(LoginModel loginModel) {
                Log.d("LOGIN", loginModel.getAuth_key() + ":" + loginModel.getMessage());
                if (loginModel.getAuth_key() != null) {
                    SharedPreferences userDetails = getDialog().getContext().getSharedPreferences("saved_auth_key", Context.MODE_PRIVATE);
                    final SharedPreferences.Editor edit = userDetails.edit();
                    edit.putString("saved_auth_key", loginModel.getAuth_key());
                    edit.apply();
                } else {
                    showProgress(false);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
//                Log.d("LOGIN", throwable.getMessage());
//                Toast.makeText(getDialog().getContext(), "Unauthorized", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
    }
}

