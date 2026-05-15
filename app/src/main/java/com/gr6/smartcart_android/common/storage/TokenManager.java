package com.gr6.smartcart_android.common.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.gr6.smartcart_android.common.utils.Constants;

public class TokenManager {

    private static TokenManager instance;
    private final SharedPreferences sharedPreferences;

    private TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_AUTH, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }

        return instance;
    }

    public void saveToken(String token) {
        sharedPreferences.edit()
                .putString(Constants.KEY_TOKEN, token)
                .apply();
    }

    public String getToken() {
        return sharedPreferences.getString(Constants.KEY_TOKEN, null);
    }

    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clearToken() {
        sharedPreferences.edit()
                .remove(Constants.KEY_TOKEN)
                .apply();
    }

    public void clearAll() {
        sharedPreferences.edit().clear().apply();
    }
}