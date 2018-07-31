package com.njery.android.measdk18;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.njery.android.measdk18.data.MeaContract.MeaEntry;
import com.njery.android.measdk18.data.MeaContract.ContactsEntry;


public class MeaCursorAdapter extends CursorAdapter {
    public MeaCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).
                inflate(R.layout.contacts_list_view, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView tvContactName = view.findViewById(R.id.tv_contact_name);
        TextView tvContactNumber = view.findViewById(R.id.tv_contact_number);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.COLUMN_NAME));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsEntry.COLUMN_PHONE_NUMBER));

        tvContactName.setText(name);
        tvContactNumber.setText(phone);

    }
}
