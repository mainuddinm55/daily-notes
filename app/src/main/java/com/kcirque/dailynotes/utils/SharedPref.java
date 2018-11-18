package com.kcirque.dailynotes.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    private Context context;
    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "note_pref";

    private static final String MASTER_PIN = "pin";
    private static final String SORT_BY_KEY = "sort_by";
    private static final String VIEW_KEY = "view";
    private static final String THEME_KEY = "theme";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Activity.MODE_PRIVATE);
    }

    public void putPin(String pin) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MASTER_PIN, pin);
        editor.apply();
    }

    public String getPin() {
        return sharedPreferences.getString(MASTER_PIN, null);
    }

    public void putSortBy(String sortBy) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SORT_BY_KEY, sortBy);
        editor.apply();
    }

    public String getSortBy() {
        return sharedPreferences.getString(SORT_BY_KEY, "Create Date");
    }

    public void putView(String view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(VIEW_KEY, view);
        editor.apply();
    }

    public String getView() {
        return sharedPreferences.getString(VIEW_KEY, "List View");
    }

    public void putTheme(String theme) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(THEME_KEY, theme);
        editor.apply();
    }

    public String getTheme() {
        return sharedPreferences.getString(THEME_KEY, "Light Theme");
    }

}
