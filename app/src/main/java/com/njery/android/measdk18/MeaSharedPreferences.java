package com.njery.android.measdk18;

import android.content.Context;
import android.preference.PreferenceManager;

public class MeaSharedPreferences {
    private static final String PREF_SEARCH_USER_ACC = "searchUser";
    private static final String PREF_USER_NAME = "username";
    private static final String PREF_USER_SOS = "sos";

    public static String getStoredQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_USER_ACC, null);
    }
    public static void setStoredQuery(Context context, String accountStatus){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_USER_ACC, accountStatus)
                .apply();
    }

    public static String getPrefUserName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_USER_NAME, null);
    }
    public static void setPrefUserName(Context context, String username){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_USER_NAME, username)
                .apply();
    }

    public static String getPrefUserSos(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_USER_SOS, null);
    }
    public  static void setPrefUserSos(Context context, String sos){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_USER_SOS, sos)
                .apply();
    }

}
