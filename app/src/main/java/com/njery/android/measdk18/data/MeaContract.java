package com.njery.android.measdk18.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class MeaContract {
    public static final String CONTENT_AUTHORITY = "com.njery.android.measdk18";
    public static final String PATH_DETAILS = "details";
    public static final String PATH_CONTACTS = "contacts";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private MeaContract(){}

    public static abstract class MeaEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_DETAILS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DETAILS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DETAILS;

        public static final String TABLE_NAME = "details";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_FULL_NAME = "full_name";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_BLOOD_TYPE = "blood_type";
        public static final String COLUMN_ALLERGIES = "allergies";
        public static final String COLUMN_MEDICAL_CONDITIONS = "medical_conditions";

        public static final int BLOOD_TYPE_UNKNOWN = 0;
        public static final int BLOOD_TYPE_A_POS = 1;
        public static final int BLOOD_TYPE_A_NEG = 2;
        public static final int BLOOD_TYPE_AB = 3;
        public static final int BLOOD_TYPE_O_POS = 4;
        public static final int BLOOD_TYPE_O_NEG = 5;



    }
    public static abstract class ContactsEntry implements BaseColumns{
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CONTACTS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CONTACTS;

        public static final String TABLE_NAME_CONTACTS = "contacts";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_PHONE_NUMBER = "phone_number";

    }
}
