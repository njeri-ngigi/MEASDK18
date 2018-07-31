package com.njery.android.measdk18;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.njery.android.measdk18.data.MeaContract.ContactsEntry;

public class ContactsEditActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int EXISTING_CONTACTS_LOADER = 0;
    private Uri mcurrentUri;

    private EditText mEditContactName, mEditContactNumber;
    private Button mSaveButton;

    private boolean mContactHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mContactHasChanged = true;
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_edit);

        Intent intent = getIntent();
        mcurrentUri = intent.getData();

        if(mcurrentUri == null){
            setTitle(getString(R.string.add_contact));
        }else{
            setTitle(getString(R.string.edit_contact));
            getLoaderManager().initLoader(EXISTING_CONTACTS_LOADER, null, this);
        }

        mEditContactName = findViewById(R.id.et_edit_name);
        mEditContactNumber = findViewById(R.id.et_edit_number);

        mEditContactName.setOnTouchListener(mTouchListener);
        mEditContactNumber.setOnTouchListener(mTouchListener);

        mSaveButton = findViewById(R.id.button_save);
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveContact();
            }
        });
    }

    private void saveContact(){
        String name = mEditContactName.getText().toString().trim();
        String number = mEditContactNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)){
            Toast.makeText(ContactsEditActivity.this,
                    "Missing Fields!", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ContactsEntry.COLUMN_NAME, name);
        values.put(ContactsEntry.COLUMN_PHONE_NUMBER, number);

        if(mcurrentUri == null){
            Uri newUri = getContentResolver().insert(ContactsEntry.CONTENT_URI, values);

            if(newUri == null)
                Toast.makeText(this,
                        R.string.save_error, Toast.LENGTH_LONG).show();
            else{
                Toast.makeText(this,
                        R.string.save_contact_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        }else{
            int rowsAffected = getContentResolver().update(mcurrentUri, values, null, null);

            if (rowsAffected == 0)
                Toast.makeText(this,
                        R.string.save_error, Toast.LENGTH_LONG).show();
            else{
                Toast.makeText(this,
                        R.string.save_contact_success, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ContactsEntry._ID,
                ContactsEntry.COLUMN_NAME,
                ContactsEntry.COLUMN_PHONE_NUMBER
        };

        return new CursorLoader(this,
                mcurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1)
            return;

        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.COLUMN_NAME));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.COLUMN_PHONE_NUMBER));

            mEditContactName.setText(name);
            mEditContactNumber.setText(number);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditContactName.setText("");
        mEditContactNumber.setText("");

    }


    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.unsaved_changes_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //User clicks keep editing, so discard the dialog
                if(dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(!mContactHasChanged){
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
