package com.njery.android.measdk18.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.njery.android.measdk18.data.MeaContract.MeaEntry;
import com.njery.android.measdk18.data.MeaContract.ContactsEntry;

public class MeaDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mea_details.db";
    private static final int DATABASE_VERSION = 1;

    public static final String CREATE_TABLE_DETAILS = "CREATE TABLE " + MeaEntry.TABLE_NAME
            + "("
            + MeaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MeaEntry.COLUMN_FULL_NAME + " TEXT NOT NULL, "
            + MeaEntry.COLUMN_EMAIL + " TEXT NOT NULL, "
            + MeaEntry.COLUMN_BLOOD_TYPE + " INTEGER NOT NULL DEFAULT 0, "
            + MeaEntry.COLUMN_ALLERGIES + " TEXT, "
            + MeaEntry.COLUMN_MEDICAL_CONDITIONS + " TEXT);";

    public static final String CREATE_TABLE_CONTACTS = "CREATE TABLE " + ContactsEntry.TABLE_NAME_CONTACTS
            + "("
            + ContactsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + ContactsEntry.COLUMN_NAME + " TEXT NOT NULL, "
            + ContactsEntry.COLUMN_PHONE_NUMBER + " TEXT NOT NULL);";

    public static final String DELETE_ENTRIES = "DROP TABLE " + MeaEntry.TABLE_NAME + " IF EXISTS;";
    public static final String DELETE_CONTACT_ENTRIES = "DROP TABLE " + ContactsEntry.TABLE_NAME_CONTACTS + " IF EXISTS;";

    public MeaDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_DETAILS);
        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_ENTRIES);
        db.execSQL(DELETE_CONTACT_ENTRIES);
        onCreate(db);
    }

}
