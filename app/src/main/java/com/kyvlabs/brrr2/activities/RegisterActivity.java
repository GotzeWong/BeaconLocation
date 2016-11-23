package com.kyvlabs.brrr2.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kyvlabs.brrr2.Application;
import com.kyvlabs.brrr2.R;
import com.kyvlabs.brrr2.activities.support.GroupsTokenComplete;
import com.kyvlabs.brrr2.network.NetworkHelper;
import com.kyvlabs.brrr2.network.model.Group;
import com.kyvlabs.brrr2.network.model.RegistrationModel;
import com.tokenautocomplete.TokenCompleteTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action0;
import rx.functions.Action1;

public class RegisterActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private CheckBox mConfirmCheckbox;

    private GroupsTokenComplete groupTextView;

    private Map<String, Integer> groupsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up the register form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_ACTION_DONE) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mConfirmCheckbox = (CheckBox) findViewById(R.id.confirm_checkbox);

        Button mEmailSignInButton = (Button) findViewById(R.id.register_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mConfirmCheckbox.isChecked()) {
                    attemptRegister();
                } else {
                    Toast.makeText(RegisterActivity.this, R.string.you_need_confirm_license, Toast.LENGTH_LONG).show();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        groupTextView = (GroupsTokenComplete) findViewById(R.id.group_select);
        groupTextView.allowDuplicates(false);
        groupTextView.setDeletionStyle(TokenCompleteTextView.TokenDeleteStyle.Clear);

        NetworkHelper networkHelper = new NetworkHelper();
        networkHelper.getGroups().subscribe(new Action1<List<Group>>() {
            @Override
            public void call(List<Group> groups) {
                Log.d("GETGROUPS", groups.toString());
                for (Group group : groups) {
                    groupsMap.put(group.getName(), group.getId());
                }
                List<String> list = new ArrayList<>(groupsMap.keySet());
                String[] groupsForAdapter = list.toArray(new String[list.size()]);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.group_select_list_item, groupsForAdapter);
                groupTextView.setAdapter(adapter);
            }
        });

    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the register form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual register attempt is made.
     */
    public void attemptRegister() {
//        if (mAuthTask != null) {
//            return;
//        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the register attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt register and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user register attempt.
            showProgress(true);
            register(email, password, getSelectedGroupsIds());
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    /**
     * Shows the progress UI and hides the register form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            try {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }catch(NullPointerException np){
                mLoginFormView.setVisibility(View.VISIBLE);
            }
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void register(String email, String password, List<Integer> groups) {
        NetworkHelper networkHelper = new NetworkHelper();
        networkHelper.register(email, password, groups).subscribe(new Action1<RegistrationModel>() {
            @Override
            public void call(RegistrationModel registrationModel) {
                Log.d("Register", registrationModel.getAuth_key() + ":" + registrationModel.getMessage());
                if (registrationModel.getAuth_key() != null) {
                    SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(getString(R.string.saved_auth_key), MODE_PRIVATE);
                    SharedPreferences.Editor edit = userDetails.edit();
                    edit.clear();
                    edit.putString(getString(R.string.saved_auth_key), registrationModel.getAuth_key());
                    edit.apply();

                    sendDeviceInfo();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    RegisterActivity.this.startActivity(intent);
                    RegisterActivity.this.finish();
                } else {
                    showProgress(false);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.d("Register", throwable.getMessage());
                Toast.makeText(RegisterActivity.this, "Bad Request", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
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

    private void sendDeviceInfo() {
        final String authKey = getString(R.string.saved_auth_key);
        SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(authKey, MODE_PRIVATE);
        if (userDetails.contains(authKey)) {
            NetworkHelper networkHelper = new NetworkHelper();
            HashMap<String, String> info = prepareDeviceInfo();
            networkHelper.sendInfo(userDetails.getString(authKey, ""), info);
        }
    }

    @NonNull
    private HashMap<String, String> prepareDeviceInfo() {
        HashMap<String, String> info = new HashMap<>();
        info.put("Brand", Build.BRAND);
        info.put("Device", Build.DEVICE);
        info.put("Fingerprint", Build.FINGERPRINT);
        info.put("Manufacturer", Build.MANUFACTURER);
        info.put("Product", Build.PRODUCT);
        info.put("Display", Build.DISPLAY);

        info.put("OS version", System.getProperty("os.version"));
        info.put("SDK number", String.valueOf(Build.VERSION.SDK_INT));
        info.put("Version codename", Build.VERSION.CODENAME);
        info.put("Version release", Build.VERSION.RELEASE);
        return info;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }
}

