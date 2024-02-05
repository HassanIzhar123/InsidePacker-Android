package com.wireguard.insidepacker_android.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager<T> {

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context, String prefName) {
        sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public void saveValue(String key, T value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }
        editor.apply();
    }

    // Method to retrieve a value from shared preferences
    @SuppressWarnings("unchecked")
    public T getValue(String key, T defaultValue) {
        if (defaultValue instanceof String) {
            return (T) sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return (T) (Integer) sharedPreferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Float) {
            return (T) (Float) sharedPreferences.getFloat(key, (Float) defaultValue);
        } else if (defaultValue instanceof Long) {
            return (T) (Long) sharedPreferences.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return (T) (Boolean) sharedPreferences.getBoolean(key, (Boolean) defaultValue);
        }
        return null;
    }

    // Method to clear all saved values in shared preferences
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
