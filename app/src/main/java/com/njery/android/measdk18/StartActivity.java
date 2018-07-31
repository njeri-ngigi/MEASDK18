package com.njery.android.measdk18;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.njery.android.measdk18.data.MeaContract.ContactsEntry;
import com.njery.android.measdk18.data.MeaContract.MeaEntry;

public class StartActivity extends AppCompatActivity{
    private static final String USER_SOS = "Please Call Me Back. Medical Emergency.";

    private View mWelcomeView, mFormUserDetails, mFormEmergencyContacts, mFormMedicalInfo;

    private ImageButton mSignUpButton;
    private Button mSaveButton;

    private EditText mEditTextFullName, mEditTextEmail,
            mEditTextNumber1, mEditTextNumber2, mEditTextNumber3,
            mEditTextName1, mEditTextName2, mEditTextName3,
            mEditTextMedicalConditions, mEditTextAllergies;

    private Spinner mBloodTypeSpinner;
    private int mBloodType = MeaEntry.BLOOD_TYPE_UNKNOWN;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_user_info:
                    mFormUserDetails.setVisibility(View.VISIBLE);
                    mWelcomeView.setVisibility(View.GONE);
                    mFormEmergencyContacts.setVisibility(View.GONE);
                    mFormMedicalInfo.setVisibility(View.GONE);

                    return true;
                case R.id.navigation_emergency_contacts:
                    mWelcomeView.setVisibility(View.GONE);
                    mFormUserDetails.setVisibility(View.GONE);
                    mFormEmergencyContacts.setVisibility(View.VISIBLE);
                    mFormMedicalInfo.setVisibility(View.GONE);

                    return true;
                case R.id.navigation_medical_info:
                    mWelcomeView.setVisibility(View.GONE);
                    mFormUserDetails.setVisibility(View.GONE);
                    mFormEmergencyContacts.setVisibility(View.GONE);
                    mFormMedicalInfo.setVisibility(View.VISIBLE);

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        setTitle(R.string.app_name);
        mWelcomeView = findViewById(R.id.welcome);
        mFormUserDetails = findViewById(R.id.form_user_details);
        mFormEmergencyContacts = findViewById(R.id.form_emergency_contacts);
        mFormMedicalInfo = findViewById(R.id.form_user_medical_info);

        mEditTextFullName = findViewById(R.id.et_full_name);
        mEditTextEmail = findViewById(R.id.et_email);
        mEditTextNumber1 = findViewById(R.id.et_phone_number_1);
        mEditTextNumber2 = findViewById(R.id.et_phone_number_2);
        mEditTextNumber3 = findViewById(R.id.et_phone_number_3);
        mEditTextName1 = findViewById(R.id.et_name_1);
        mEditTextName2 = findViewById(R.id.et_name_2);
        mEditTextName3 = findViewById(R.id.et_name_3);
        mEditTextMedicalConditions = findViewById(R.id.et_medical_conditions);
        mEditTextAllergies = findViewById(R.id.et_allergies);

        mEditTextFullName.setOnTouchListener(mTouchListener);
        mEditTextEmail.setOnTouchListener(mTouchListener);
        mEditTextNumber1.setOnTouchListener(mTouchListener);
        mEditTextNumber2.setOnTouchListener(mTouchListener);
        mEditTextNumber3.setOnTouchListener(mTouchListener);
        mEditTextName1.setOnTouchListener(mTouchListener);
        mEditTextName2.setOnTouchListener(mTouchListener);
        mEditTextName3.setOnTouchListener(mTouchListener);
        mEditTextMedicalConditions.setOnTouchListener(mTouchListener);
        mEditTextAllergies.setOnTouchListener(mTouchListener);

        mSignUpButton = findViewById(R.id.ib_sign_up);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWelcomeView.setVisibility(View.GONE);
                mFormUserDetails.setVisibility(View.VISIBLE);
                mFormEmergencyContacts.setVisibility(View.GONE);
                mFormMedicalInfo.setVisibility(View.GONE);
            }
        });

        mBloodTypeSpinner = findViewById(R.id.blood_type_spinner);
        mBloodTypeSpinner.setOnTouchListener(mTouchListener);
        setupSpinner();

        mSaveButton = findViewById(R.id.button_complete_sign_up);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInput(v);
            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void saveInput(View v){
        String fullName = mEditTextFullName.getText().toString().trim();
        String email = mEditTextEmail.getText().toString().trim();
        String name1 = mEditTextName1.getText().toString().trim();
        String name2 = mEditTextName2.getText().toString().trim();
        String name3 = mEditTextName3.getText().toString().trim();
        String phone1 = mEditTextNumber1.getText().toString().trim();
        String phone2 = mEditTextNumber2.getText().toString().trim();
        String phone3 = mEditTextNumber3.getText().toString().trim();
        String medicalConditions = mEditTextMedicalConditions.getText().toString().trim();
        String allergies = mEditTextAllergies.getText().toString().trim();

        if(TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(name1) || TextUtils.isEmpty(name2) || TextUtils.isEmpty(name3) ||
                TextUtils.isEmpty(phone1) || TextUtils.isEmpty(phone2) || TextUtils.isEmpty(phone3)){
            Snackbar.make(v, "You have a couple missing fields. \n Please Fill out all fields marked with * ", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return;
        }

        ContentValues detailValues = new ContentValues();
        detailValues.put(MeaEntry.COLUMN_FULL_NAME, fullName);
        detailValues.put(MeaEntry.COLUMN_EMAIL, email);
        detailValues.put(MeaEntry.COLUMN_MEDICAL_CONDITIONS, medicalConditions);
        detailValues.put(MeaEntry.COLUMN_ALLERGIES, allergies);
        detailValues.put(MeaEntry.COLUMN_BLOOD_TYPE, mBloodType);

        ContentValues person1 = new ContentValues();
        person1.put(ContactsEntry.COLUMN_NAME, name1);
        person1.put(ContactsEntry.COLUMN_PHONE_NUMBER, phone1);

        ContentValues person2 = new ContentValues();
        person2.put(ContactsEntry.COLUMN_NAME, name2);
        person2.put(ContactsEntry.COLUMN_PHONE_NUMBER, phone2);

        ContentValues person3 = new ContentValues();
        person3.put(ContactsEntry.COLUMN_NAME, name3);
        person3.put(ContactsEntry.COLUMN_PHONE_NUMBER, phone3);

        Uri detailsUri = getContentResolver().insert(MeaEntry.CONTENT_URI, detailValues);
        Uri personUri1 = getContentResolver().insert(ContactsEntry.CONTENT_URI, person1);
        Uri personUri2 = getContentResolver().insert(ContactsEntry.CONTENT_URI, person2);
        Uri personUri3 = getContentResolver().insert(ContactsEntry.CONTENT_URI, person3);

        if(detailsUri == null || personUri1 == null || personUri2 == null || personUri3 == null){
            Toast.makeText(this, R.string.save_error,
                    Toast.LENGTH_LONG).show();
            return;
        } else{
            Toast.makeText(this,R.string.save_success,
                    Toast.LENGTH_SHORT).show();
        }

        MeaSharedPreferences.setStoredQuery(StartActivity.this, "true");
        MeaSharedPreferences.setPrefUserName(StartActivity.this, fullName);
        MeaSharedPreferences.setPrefUserSos(StartActivity.this, USER_SOS);

        Intent intent = new Intent(StartActivity.this, HomeActivity.class);
        startActivity(intent);
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
                mBloodType = MeaEntry.BLOOD_TYPE_UNKNOWN;
            }
        });

    }

}
