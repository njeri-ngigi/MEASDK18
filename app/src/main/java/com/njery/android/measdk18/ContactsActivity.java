package com.njery.android.measdk18;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.njery.android.measdk18.data.MeaContract.ContactsEntry;

public class ContactsActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int CONTACTS_LOADER = 0;
    private static final int REQUEST_CALL_PERMISSION = 2;
    MeaCursorAdapter mCursorAdapter;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        setTitle(R.string.title_contacts);

        ListView contactsListView = findViewById(R.id.lv_contacts);

        View emptyView = findViewById(R.id.empty_view);
        contactsListView.setEmptyView(emptyView);

        mCursorAdapter = new MeaCursorAdapter(this, null);
        contactsListView.setAdapter(mCursorAdapter);

         AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {

             @Override
             public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                 final Uri currentUri = ContentUris.withAppendedId(ContactsEntry.CONTENT_URI, id);

                 PopupMenu popup = new PopupMenu(getBaseContext(), view);
                 popup.getMenuInflater().inflate(R.menu.contacts_options, popup.getMenu());
                 //popup.setGravity(1);
                 popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                     @Override
                     public boolean onMenuItemClick(MenuItem item) {
                         int itemId = item.getItemId();

                         switch(itemId){
                             case R.id.action_call:
                                 TextView nameTextView = view.findViewById(R.id.tv_contact_name);
                                 String name = nameTextView.getText().toString();

                                 TextView numberTextView = view.findViewById(R.id.tv_contact_number);
                                 String number = numberTextView.getText().toString();

                                 Toast.makeText(ContactsActivity.this,
                                         "Calling " + name, Toast.LENGTH_SHORT).show();
                                 call(number);

                                 return true;
                             case R.id.action_edit:
                                 Intent intent = new Intent(ContactsActivity.this, ContactsEditActivity.class);

                                 intent.setData(currentUri);
                                 startActivity(intent);

                                 return true;

                             case R.id.action_delete:
                                 showDeleteConfirmationDialog(currentUri);

                                 return true;

                             default:
                                     Toast.makeText(ContactsActivity.this,
                                             "Nothing here", Toast.LENGTH_SHORT).show();
                         }
                         return true;
                     }
                 });
                 popup.show();
             }
         };

        contactsListView.setOnItemClickListener(listener);
        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(ContactsActivity.this, "You have long pressed", Toast.LENGTH_SHORT).show();
            return true;
            }
        });
        getLoaderManager().initLoader(CONTACTS_LOADER, null, this);


    }

    private void call(String number){
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel: " + number));

        if (ActivityCompat.checkSelfPermission(ContactsActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE},
                    REQUEST_CALL_PERMISSION);
        }
        startActivity(callIntent);
    }

    private void showDeleteConfirmationDialog(final Uri currentUri){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(R.string.confirm_delete_contact);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int rowsDeleted = getContentResolver().delete(currentUri, null, null);
                if(rowsDeleted == 0)
                    Toast.makeText(ContactsActivity.this,
                            "Delete failed", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(ContactsActivity.this,
                            "Contact deleted", Toast.LENGTH_SHORT).show();
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
                ContactsEntry._ID,
                ContactsEntry.COLUMN_NAME,
                ContactsEntry.COLUMN_PHONE_NUMBER
        };

        return new CursorLoader(this,
                ContactsEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.add_menu, menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_add){
            Intent intent = new Intent(ContactsActivity.this, ContactsEditActivity.class);
            startActivity(intent);
        }
         return super.onOptionsItemSelected(item);
    }
}