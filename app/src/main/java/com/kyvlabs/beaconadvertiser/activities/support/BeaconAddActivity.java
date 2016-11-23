package com.kyvlabs.beaconadvertiser.activities.support;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
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
import com.kyvlabs.beaconadvertiser.data.DBHelper;
import com.kyvlabs.beaconadvertiser.views.InputFilterMinMax;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//Activity for adding beacon to db (layout - activity_add_actvity)
public class BeaconAddActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_actvity);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.beacon_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    //Add beacon  fragment
    public static class PlaceholderFragment extends Fragment {

        String imageUriString;
        ImageView pictureView;

        public PlaceholderFragment() {
        }

        //called when user return to this fragment
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
            View rootView = inflater.inflate(R.layout.fragment_add_actvity, container, false);
            //Finding UI elements
            TextView minorView = (TextView) rootView.findViewById(R.id.edit_fragment_minor);
            minorView.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
            TextView majorView = (TextView) rootView.findViewById(R.id.edit_fragment_major);
            majorView.setFilters(new InputFilter[]{new InputFilterMinMax("0", "255")});
            pictureView = (ImageView) rootView.findViewById(R.id.edit_fragment_picture);

            Button loadButton = (Button) rootView.findViewById(R.id.edit_fragment_load_button);
            //On click for load button
            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("image/*");
                    startActivityForResult(intent, 1);
                }
            });

            return rootView;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            //confirmation editing beacon in db
            if (id == R.id.edit_fragment_ok_button) {
                //Finding UI elements
                String uidString = String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_uuid)).getText());
                String majorString = String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_major)).getText());
                String minorString = String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_minor)).getText());
                String titleString = String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_title)).getText());
                String descriptionString = String.valueOf(((EditText) getActivity().findViewById(R.id.edit_fragment_description)).getText());

                if (imageUriString != null && !majorString.equals("") && !minorString.equals("")) {
                    //Saving to db
                    DBHelper dbHelper = new DBHelper();

                    SQLiteDatabase database = dbHelper.getReadableDatabase();

                    ContentValues cv = new ContentValues();

                    cv.put("uuid", uidString);
                    cv.put("major", majorString);
                    cv.put("minor", minorString);
                    cv.put("title", titleString);
                    cv.put("picture", imageUriString);
                    cv.put("description", descriptionString);
                    try {
                        database.insertOrThrow("ads", null, cv);
                    } catch (SQLiteConstraintException e) {
                        Toast.makeText(getActivity().getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    Toast.makeText(getActivity().getApplicationContext(), "Beacon added", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Fill major, minor and select picture", Toast.LENGTH_LONG).show();
                    return false;
                }
            }
            return super.onOptionsItemSelected(item);
        }


    }
}
