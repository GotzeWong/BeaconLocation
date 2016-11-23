package com.kyvlabs.beaconadvertiser.activities;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.kyvlabs.beaconadvertiser.Application;
import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.activities.support.GroupsTokenComplete;
import com.kyvlabs.beaconadvertiser.network.NetworkHelper;
import com.kyvlabs.beaconadvertiser.network.model.ForgotModel;
import com.kyvlabs.beaconadvertiser.network.model.Group;
import com.kyvlabs.beaconadvertiser.network.model.LoginModel;
import com.tokenautocomplete.TokenCompleteTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.functions.Action0;
import rx.functions.Action1;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_WRITE_CONTACTS = 101;
    CallbackManager callbackManager;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private GroupsTokenComplete groupTextView;
    private Map<String, Integer> groupsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_WRITE_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {

        }

        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        setTitle(R.string.title_activity_login);
        final String authKey = getString(R.string.saved_auth_key);
        SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(authKey, MODE_PRIVATE);
        if (userDetails.contains(authKey)) {
            goToMainActivity();
            NetworkHelper networkHelper = new NetworkHelper();
            HashMap<String, String> info = prepareDeviceInfo();
            networkHelper.sendInfo(userDetails.getString(authKey, ""), info);
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_ACTION_DONE) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button mRegistrationButton = (Button) findViewById(R.id.register_button);
        mRegistrationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                goToRegistration();
            }
        });

        Button mForgotPasswordButton = (Button) findViewById(R.id.forgot_password_button);
        mForgotPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptForgotPassword();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions(Collections.singletonList("email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                final AccessToken accessToken = loginResult.getAccessToken();
                final String stringAccessToken = accessToken.getToken();
                Log.d("FACEBOOK", stringAccessToken);

                // App code
                GraphRequest request = GraphRequest.newMeRequest(
                        accessToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                // Application code
                                try {
                                    String email = response.getJSONObject().getString("email");
                                    Log.v("LoginActivity", email);
                                    loginFb(email, stringAccessToken);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK", "canceled");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FACEBOOK", "error", error);
            }
        });

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
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
            }
        }
        if (requestCode == PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void attemptForgotPassword() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();

        boolean cancel = false;
        View focusView = null;

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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            forgotPassword(email);
        }
    }

    private void forgotPassword(String email) {
        NetworkHelper networkHelper = new NetworkHelper();
        networkHelper.forgotPassword(email).subscribe(new Action1<ForgotModel>() {
            @Override
            public void call(ForgotModel o) {
                Toast.makeText(getApplicationContext(), R.string.forgot_password_success, Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(getApplicationContext(), R.string.something_wrong, Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
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

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(intent);
        LoginActivity.this.finish();
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
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
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            login(email, password, getSelectedGroupsIds());
        }
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

    private void goToRegistration() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private void login(final String email, final String password, List<Integer> groups) {
        NetworkHelper networkHelper = new NetworkHelper();
        networkHelper.login(email, password, groups).subscribe(new Action1<LoginModel>() {
            @Override
            public void call(LoginModel loginModel) {
                Log.d("LOGIN", loginModel.getAuth_key() + ":" + loginModel.getMessage());
                if (loginModel.getAuth_key() != null) {

                    SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(getString(R.string.saved_auth_key), MODE_PRIVATE);
                    SharedPreferences.Editor edit = userDetails.edit();
                    edit.clear();
                    edit.putString(getString(R.string.saved_auth_key), loginModel.getAuth_key());
                    edit.putString(getString(R.string.saved_email), email);
                    edit.putString(getString(R.string.saved_password), password);
                    edit.commit();

                    sendDeviceInfo();
                    goToMainActivity();
                } else {
                    showProgress(false);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.d("LOGIN", throwable.getMessage());
                Toast.makeText(LoginActivity.this, "Unauthorized", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
    }

    private void loginFb(String email, String auth) {
        NetworkHelper networkHelper = new NetworkHelper();
        networkHelper.loginFb(email, auth).subscribe(new Action1<LoginModel>() {
            @Override
            public void call(LoginModel loginModel) {
                Log.d("LOGIN", loginModel.getAuth_key() + ":" + loginModel.getMessage());
                if (loginModel.getAuth_key() != null) {

                    SharedPreferences userDetails = Application.getAppContext().getSharedPreferences(getString(R.string.saved_auth_key), MODE_PRIVATE);
                    SharedPreferences.Editor edit = userDetails.edit();
                    edit.clear();
                    edit.putString(getString(R.string.saved_auth_key), loginModel.getAuth_key());
                    edit.apply();

                    sendDeviceInfo();
                    goToMainActivity();

                } else {
                    showProgress(false);
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Log.d("LOGIN", throwable.getMessage());
                Toast.makeText(LoginActivity.this, "Unauthorized", Toast.LENGTH_LONG).show();
                showProgress(false);
            }
        }, new Action0() {
            @Override
            public void call() {

            }
        });
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
                new ArrayAdapter<>(LoginActivity.this,
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

