package com.gr6.smartcart_android.common.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.gr6.smartcart_android.common.utils.Constants;

public class UserSession {

    private static UserSession instance;
    private final SharedPreferences sharedPreferences;

    private UserSession(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(Constants.PREF_USER, Context.MODE_PRIVATE);
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context);
        }

        return instance;
    }

    public void saveUserId(Long userId) {
        if (userId == null) return;

        sharedPreferences.edit()
                .putLong(Constants.KEY_USER_ID, userId)
                .apply();
    }

    public Long getUserId() {
        long value = sharedPreferences.getLong(Constants.KEY_USER_ID, -1L);
        return value == -1L ? null : value;
    }

    public void saveFullName(String fullName) {
        sharedPreferences.edit()
                .putString(Constants.KEY_FULL_NAME, fullName)
                .apply();
    }

    public String getFullName() {
        return sharedPreferences.getString(Constants.KEY_FULL_NAME, "");
    }

    public void saveEmail(String email) {
        sharedPreferences.edit()
                .putString(Constants.KEY_EMAIL, email)
                .apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(Constants.KEY_EMAIL, "");
    }

    public void saveAvatarUrl(String avatarUrl) {
        sharedPreferences.edit()
                .putString(Constants.KEY_AVATAR_URL, avatarUrl)
                .apply();
    }

    public String getAvatarUrl() {
        return sharedPreferences.getString(Constants.KEY_AVATAR_URL, "");
    }

    public void saveRole(String role) {
        sharedPreferences.edit()
                .putString(Constants.KEY_ROLE, role)
                .apply();
    }

    public void savePhoneNumber(String phoneNumber) {
        sharedPreferences.edit()
                .putString(Constants.KEY_PHONE_NUMBER, phoneNumber)
                .apply();
    }

    public String getPhoneNumber() {
        return sharedPreferences.getString(Constants.KEY_PHONE_NUMBER, "");
    }

    public String getRole() {
        return sharedPreferences.getString(Constants.KEY_ROLE, "");
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}