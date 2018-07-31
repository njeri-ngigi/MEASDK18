package com.njery.android.measdk18;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.njery.android.measdk18.data.MeaContract.ContactsEntry;
import com.njery.android.measdk18.data.MeaContract.MeaEntry;

public class MainUserActivity extends AppCompatActivity {
    private TextView message1, message2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_user);

        message1 = findViewById(R.id.hello_message);
        message2 = findViewById(R.id.hello_message_2);

        displayPet();

    }

    private void displayPet(){
        String[] projection = {
                MeaEntry._ID,
                MeaEntry.COLUMN_FULL_NAME,
                MeaEntry.COLUMN_EMAIL,
                MeaEntry.COLUMN_BLOOD_TYPE,
                MeaEntry.COLUMN_ALLERGIES,
                MeaEntry.COLUMN_MEDICAL_CONDITIONS
        };
        String[] projection2 = {
                ContactsEntry._ID,
                ContactsEntry.COLUMN_NAME,
                ContactsEntry.COLUMN_PHONE_NUMBER
        };

        Uri newUri = MeaEntry.CONTENT_URI;
        String selection = MeaEntry._ID + "=?";
        String[] selectionArgs = new String[]{"1"};

        Uri newUri2 = ContactsEntry.CONTENT_URI;

        Cursor cursor = getContentResolver().query(newUri, projection, selection, selectionArgs, null);
        Cursor cursor2 = getContentResolver().query(newUri2, projection2, null, null, null);

        if(cursor.moveToFirst()){
            int nameColumnIndex = cursor.getColumnIndex(MeaEntry.COLUMN_FULL_NAME);

            String name = cursor.getString(nameColumnIndex);

            message1.setText(name);
        }
        while (cursor2.moveToNext()){
            String phone = cursor2.getString(cursor2.getColumnIndexOrThrow
                    (ContactsEntry.COLUMN_PHONE_NUMBER));
            message2.append(phone + "\n");
        }

    }
}
