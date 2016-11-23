package com.kyvlabs.beaconadvertiser.activities.support;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.kyvlabs.beaconadvertiser.Application;
import com.kyvlabs.beaconadvertiser.R;
import com.kyvlabs.beaconadvertiser.activities.BaseActivity;
import com.kyvlabs.beaconadvertiser.data.BeaconIds;
import com.kyvlabs.beaconadvertiser.data.DBHelper;
import com.kyvlabs.beaconadvertiser.data.DataKeys;
import com.kyvlabs.beaconadvertiser.views.InputFilterMinMax;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//Activity to edit beacon (layout - activity_beacon_edit)
public class BeaconEditActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_edit);

        FragmentManager fragmentManager = getFragmentManager();

        PlaceholderFragment beaconEditFragment = new PlaceholderFragment();
        beaconEditFragment.setArguments(getIntent().getExtras());
        if (savedInstanceState == null) {
            fragmentManager.beginTransaction()
                    .add(R.id.container, beaconEditFragment)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.beacon_edit, menu);
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        String imageUriString;
        ImageView pictureView;
        private String uuid;
        private String major;
        private String minor;

        public PlaceholderFragment() {
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case 1:
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        imageUriString = saveFile(imageUri);
                        toImageViewFromUri(pictureView, imageUriString);
                    }
                    break;
            }
        }

        //save file from input stream to uri file. Uses for save images to file system
        private String saveFile(Uri sourceUri) {
            String destinationFilename = Application.getCachePath() + File.separatorChar + String.valueOf(System.currentTimeMillis());

            BufferedInputStream bis = null;
            BufferedOutputStream bos = null;

            try {
                bis = new BufferedInputStream(getActivity().getApplicationContext().getContentResolver().openInputStream(sourceUri));
                bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
                byte[] buf = new byte[1024];
                bis.read(buf);
                do {
                    bos.write(buf);
                } while (bis.read(buf) != -1);
            } catch (IOException ignored) {

            } finally {
                try {
                    if (bis != null) bis.close();
                    if (bos != null) bos.close();
                } catch (IOException ignored) {

                }
            }
            return "file://" + destinationFilename;
        }

        //draw image to image view from uri file
        private void toImageViewFromUri(ImageView imageview, String imageUriStr) {
            Uri imageUri = Uri.parse(imageUriStr);
            InputStream imageStream = null;
            try {
                imageStream = getActivity().getApplicationContext().getContentResolver().openInputStream(imageUri);
                imageview.setImageBitmap(BitmapFactory.decodeStream(imageStream));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                // Handle the error
            } finally {
                if (imageStream != null) {
                    try {
                        imageStream.close();
                    } catch (IOException e) {
                        // Ignore the exception
                    }
                }
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            setHasOptionsMenu(true);
            super.onCreate(savedInstanceState);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            //Finding UI elements
            View rootView = inflater.inflate(R.layout.fragment_beacon_edit, container, false);

            TextView minorView = (TextView) rootView.findViewById(R.id.edit_fragment_minor);
            minorView.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
            TextView majorView = (TextView) rootView.findViewById(R.id.edit_fragment_major);
            majorView.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});

            pictureView = (ImageView) rootView.findViewById(R.id.edit_fragment_picture);

            Button loadButton = (Button) rootView.findViewById(R.id.edit_fragment_load_button);
            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //On click for load button
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                }
            });

            Bundle bundle = getArguments();
            if (bundle != null) {

                BeaconIds ids = bundle.getParcelable(DataKeys.BEACON_IDS);
                uuid = ids.getUuid();
                major = ids.getMajor();
                minor = ids.getMinor();
                DBHelper dbHelper = new DBHelper();

                SQLiteDatabase database = dbHelper.getReadableDatabase();
                Cursor cursor = database.rawQuery("SELECT * FROM ads WHERE uuid = '"
                                + uuid
                                + "' AND major = '"
                                + major
                                + "' AND minor = '"
                                + minor + "'",
                        null);
                if (cursor.moveToFirst()) {
                    ((TextView) rootView.findViewById(R.id.edit_fragment_description)).setText(cursor.getString(cursor.getColumnIndex("description")));
                    ((TextView) rootView.findViewById(R.id.edit_fragment_uuid)).setText(cursor.getString(cursor.getColumnIndex("uuid")));
                    ((TextView) rootView.findViewById(R.id.edit_fragment_title)).setText(cursor.getString(cursor.getColumnIndex("title")));
                    ((TextView) rootView.findViewById(R.id.edit_fragment_minor)).setText(cursor.getString(cursor.getColumnIndex("minor")));
                    ((TextView) rootView.findViewById(R.id.edit_fragment_major)).setText(cursor.getString(cursor.getColumnIndex("major")));
                    imageUriString = cursor.getString(cursor.getColumnIndex("picture"));
                    toImageViewFromUri(pictureView, imageUriString);
                }

            }
            return rootView;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            //confirmation editing beacon in db
            if (id == R.id.edit_fragment_ok_button) {
                //Saving to db
                DBHelper dbHelper = new DBHelper();

                SQLiteDatabase database = dbHelper.getReadableDatabase();

                ContentValues cv = new ContentValues();

                cv.put("uuid", String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_uuid)).getText()));
                cv.put("major", String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_major)).getText()));
                cv.put("minor", String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_minor)).getText()));
                cv.put("title", String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_title)).getText()));
                cv.put("picture", imageUriString);
                cv.put("description", String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_description)).getText()));
                try {
                    database.update("ads", cv, "uuid = ? and major = ? and minor = ?",
                            new String[]{uuid, major, minor});
                } catch (SQLiteConstraintException e) {
                    Toast.makeText(getActivity().getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    return false;
                }
                Toast.makeText(getActivity().getApplicationContext(), "Beacon updated", Toast.LENGTH_LONG).show();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }
}
