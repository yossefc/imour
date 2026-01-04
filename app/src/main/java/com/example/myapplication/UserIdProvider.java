package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UserIdProvider {
    private static final String PREFS = "app_prefs";
    private static final String KEY_UID = "local_user_id";

    public static String getOrCreate(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String uid = sp.getString(KEY_UID, null);
        if (uid == null) {
            uid = UUID.randomUUID().toString();
            sp.edit().putString(KEY_UID, uid).apply();
        }
        return uid;
    }
}
