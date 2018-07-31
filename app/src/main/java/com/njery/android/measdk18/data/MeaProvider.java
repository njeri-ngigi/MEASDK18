package com.njery.android.measdk18.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.njery.android.measdk18.data.MeaContract.MeaEntry;
import com.njery.android.measdk18.data.MeaContract.ContactsEntry;

public class MeaProvider extends ContentProvider{
    private static final String LOG_TAG = MeaProvider.class.getSimpleName();

    private static final int MEA = 100;
    private static final int MEA_ID = 101;
    private static final int CONTACTS = 102;
    private static final int CONTACTS_ID = 103;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(MeaContract.CONTENT_AUTHORITY, MeaContract.PATH_DETAILS, MEA);
        sUriMatcher.addURI(MeaContract.CONTENT_AUTHORITY, MeaContract.PATH_DETAILS + "/#", MEA_ID);
        sUriMatcher.addURI(MeaContract.CONTENT_AUTHORITY, MeaContract.PATH_CONTACTS, CONTACTS);
        sUriMatcher.addURI(MeaContract.CONTENT_AUTHORITY, MeaContract.PATH_CONTACTS + "/#", CONTACTS_ID);
    }

    private MeaDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new MeaDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match){
            case MEA:
                cursor = db.query(MeaEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, null);
                break;
            case MEA_ID:
                selection = MeaEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(MeaEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            case CONTACTS:
                cursor = db.query(ContactsEntry.TABLE_NAME_CONTACTS, projection,
                        selection, selectionArgs, null, null, null);
                break;
            case CONTACTS_ID:
                selection = ContactsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(ContactsEntry.TABLE_NAME_CONTACTS, projection, selection, selectionArgs, null, null, sortOrder);

                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MEA:
                return MeaEntry.CONTENT_LIST_TYPE;
            case MEA_ID:
                return MeaEntry.CONTENT_ITEM_TYPE;
            case CONTACTS:
                return ContactsEntry.CONTENT_LIST_TYPE;
            case CONTACTS_ID:
                return ContactsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case MEA:
                return insertMea(uri, values, MeaEntry.TABLE_NAME);
            case CONTACTS:
                return insertMea(uri, values, ContactsEntry.TABLE_NAME_CONTACTS);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertMea(Uri uri, ContentValues values, String table_name){

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(table_name, null, values);

        if(id == -1){
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match){
            case MEA:
                rowsDeleted = db.delete(MeaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MEA_ID:
                selection = MeaEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(MeaEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CONTACTS:
                rowsDeleted = db.delete(ContactsEntry.TABLE_NAME_CONTACTS, selection, selectionArgs);
                break;
            case CONTACTS_ID:
                selection = ContactsEntry._ID + "=?";
                selectionArgs = new String[]{ String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ContactsEntry.TABLE_NAME_CONTACTS, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion not supported for " + uri);
        }
        if(rowsDeleted !=0 )
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case MEA:
                return updateMea(uri, values, selection, selectionArgs, MeaEntry.TABLE_NAME);
            case MEA_ID:
                selection = MeaEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMea(uri, values, selection, selectionArgs,  MeaEntry.TABLE_NAME);
            case CONTACTS:
                return updateMea(uri, values, selection, selectionArgs, ContactsEntry.TABLE_NAME_CONTACTS);
            case CONTACTS_ID:
                selection = ContactsEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateMea(uri, values, selection, selectionArgs, ContactsEntry.TABLE_NAME_CONTACTS);
            default:
                throw new IllegalArgumentException("Update not supported for " + uri);
        }
    }

    private int updateMea(Uri uri, ContentValues values, String selection, String[] selectionArgs, String table_name){
        if(values.size() == 0)
            return 0;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsUpdated = db.update(table_name, values, selection, selectionArgs);

        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
    public void deleteTables(){
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.execSQL(MeaDbHelper.DELETE_ENTRIES);
        db.execSQL(MeaDbHelper.DELETE_CONTACT_ENTRIES);
    }
    }
