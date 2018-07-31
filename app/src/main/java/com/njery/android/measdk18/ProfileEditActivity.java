package com.njery.android.measdk18;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.njery.android.measdk18.data.MeaContract.MeaEntry;

public class ProfileEditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int DETAILS_LOADER = 0;

    private int mBloodType;
    private Spinner mBloodTypeSpinner;

    EditText nameET, medicalConditionsET, allergiesET, sosET;
    private boolean hasDetailsChanged = false;
    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hasDetailsChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        setTitle("Edit Profile");
        mBloodTypeSpinner = findViewById(R.id.spinner_user_blood_type);

        nameET = findViewById(R.id.et_user_name);
        medicalConditionsET = findViewById(R.id.et_user_medical_conditions);
        allergiesET = findViewById(R.id.et_user_allergies);
        sosET = findViewById(R.id.et_user_sos);

        nameET.setOnTouchListener(listener);
        medicalConditionsET.setOnTouchListener(listener);
        allergiesET.setOnTouchListener(listener);
        sosET.setOnTouchListener(listener);
        mBloodTypeSpinner.setOnTouchListener(listener);

        Button saveDetailsButton = findViewById(R.id.button_save_update);
        saveDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUpdate();
            }
        });

        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
    }

    private void saveUpdate(){
        String name = nameET.getText().toString().trim();
        String medicalConditions = medicalConditionsET.getText().toString().trim();
        String allergies = allergiesET.getText().toString().trim();
        String sos = sosET.getText().toString().trim();

        if (TextUtils.isEmpty(name)  || TextUtils.isEmpty(sos)){
            Toast.makeText(ProfileEditActivity.this,
                    "Missing Fields!", Toast.LENGTH_LONG).show();
            return;
        }

        MeaSharedPreferences.setPrefUserSos(this, sos);

        ContentValues values = new ContentValues();
        values.put(MeaEntry.COLUMN_FULL_NAME, name);
        values.put(MeaEntry.COLUMN_MEDICAL_CONDITIONS, medicalConditions);
        values.put(MeaEntry.COLUMN_ALLERGIES, allergies);
        values.put(MeaEntry.COLUMN_BLOOD_TYPE, mBloodType);

        Uri updateUri = ContentUris.withAppendedId(MeaEntry.CONTENT_URI, 1);

        int rowsAffected = getContentResolver().update(updateUri, values, null, null);

        if (rowsAffected == 0)
            Toast.makeText(this,
                    R.string.save_error, Toast.LENGTH_LONG).show();
        else{
            Toast.makeText(this,
                    R.string.save_contact_success, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupSpinner(){
        ArrayAdapter bloodTypeSpinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.blood_type_array, android.R.layout.simple_spinner_item);

        bloodTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mBloodTypeSpinner.setAdapter(bloodTypeSpinnerAdapter);
        mBloodTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int index = parent.getSelectedItemPosition();
                if (index == 0)
                    mBloodType = MeaEntry.BLOOD_TYPE_UNKNOWN;
                if (index == 1)
                    mBloodType = MeaEntry.BLOOD_TYPE_A_POS;
                if (index == 2)
                    mBloodType = MeaEntry.BLOOD_TYPE_A_NEG;
                if (index == 3)
                    mBloodType = MeaEntry.BLOOD_TYPE_AB;
                if (index == 4)
                    mBloodType = MeaEntry.BLOOD_TYPE_O_POS;
                if (index == 5)
                    mBloodType = MeaEntry.BLOOD_TYPE_O_NEG;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MeaEntry._ID,
                MeaEntry.COLUMN_FULL_NAME,
                MeaEntry.COLUMN_BLOOD_TYPE,
                MeaEntry.COLUMN_ALLERGIES,
                MeaEntry.COLUMN_MEDICAL_CONDITIONS
        };

        String selection = MeaEntry._ID + "=?";
        String[] selectionArgs = {"1"};

        return new CursorLoader(this,
                MeaEntry.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_FULL_NAME));
            int bloodTypeInt = cursor.getInt(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_BLOOD_TYPE));
            String medicalConditions = cursor.getString(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_MEDICAL_CONDITIONS));
            String allergies = cursor.getString(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_ALLERGIES));

            String sos = MeaSharedPreferences.getPrefUserSos(this);

            if(TextUtils.isEmpty(medicalConditions)){
                medicalConditions = "None";
            }
            if (TextUtils.isEmpty(allergies)) {
                allergies = "None";
            }

            setupSpinner();

            mBloodTypeSpinner.setSelection(bloodTypeInt);
            nameET.setText(name);
            medicalConditionsET.setText(medicalConditions);
            allergiesET.setText(allergies);
            sosET.setText(sos);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
