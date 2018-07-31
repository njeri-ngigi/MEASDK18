package com.njery.android.measdk18;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.njery.android.measdk18.data.MeaContract.ContactsEntry;
import com.njery.android.measdk18.data.MeaContract.MeaEntry;
import com.njery.android.measdk18.data.MeaDbHelper;
import com.njery.android.measdk18.data.MeaProvider;

public class ProfileActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DETAILS_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setTitle(R.string.title_profile);

        Button mGoToContacts = findViewById(R.id.go_to_contacts);
        mGoToContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ContactsActivity.class);
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(DETAILS_LOADER, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_edit_details){
            Intent intent = new Intent(ProfileActivity.this, ProfileEditActivity.class);
            startActivity(intent);
        }

        if(id == R.id.action_delete_account){
            showDeleteConfirmationDialog();
        }
        return super.onOptionsItemSelected(item);
    }



    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.confirm_delete_account);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ProfileActivity.this,
                        "Account deleted successfully!", Toast.LENGTH_SHORT).show();
                finish();
//                MeaSharedPreferences.setPrefUserSos(ProfileActivity.this,null);
//                MeaSharedPreferences.setPrefUserName(ProfileActivity.this, null);
//                MeaSharedPreferences.setStoredQuery(ProfileActivity.this, null);
//
//                finish();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(dialog!=null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
            TextView mNameTV = findViewById(R.id.tv_user_name);
            TextView mBloodTypeTV = findViewById(R.id.tv_user_blood_type);
            TextView mMedicalConditionsTV = findViewById(R.id.tv_user_medical_conditions);
            TextView mAllergiesTV = findViewById(R.id.tv_user_allergies);
            TextView mSosTV = findViewById(R.id.tv_user_sos);

            String name = cursor.getString(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_FULL_NAME));
            Integer bloodTypeInt = cursor.getInt(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_BLOOD_TYPE));
            String medicalConditions = cursor.getString(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_MEDICAL_CONDITIONS));
            String allergies = cursor.getString(cursor.getColumnIndexOrThrow(MeaEntry.COLUMN_ALLERGIES));

            String sos = MeaSharedPreferences.getPrefUserSos(this);

            if(TextUtils.isEmpty(medicalConditions)){
                medicalConditions = "None";
            }
            if (TextUtils.isEmpty(allergies)) {
                allergies = "None";
            }

            String bloodType = "Unknown";

            switch (bloodTypeInt){
                case MeaEntry.BLOOD_TYPE_A_POS:
                    bloodType = "A+";
                    break;
                case MeaEntry.BLOOD_TYPE_A_NEG:
                    bloodType = "A-";
                    break;
                case MeaEntry.BLOOD_TYPE_AB:
                    bloodType = "AB";
                    break;
                case MeaEntry.BLOOD_TYPE_O_POS:
                    bloodType = "O+";
                    break;
                case MeaEntry.BLOOD_TYPE_O_NEG:
                    bloodType = "O-";
                    break;
                default:
                    bloodType = "Unknown";
            }

            mNameTV.setText(name);
            mBloodTypeTV.setText(bloodType);
            mMedicalConditionsTV.setText(medicalConditions);
            mAllergiesTV.setText(allergies);
            mSosTV.setText(sos);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
