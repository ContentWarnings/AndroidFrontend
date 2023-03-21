package com.example.moviementor.other;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

// Singleton class that takes care of the storage and retrieval of a user's content warning
// preferences to and from the app's SharedPreferences file
public class ContentWarningPrefsStorage {
    // Three options user can set for each content warning
    public enum ContentWarningVisibility {
        SHOW(0), // Default option for every content warning
        WARN(1),
        HIDE(2);

        private final int value;
        ContentWarningVisibility(final int value) {
            this.value = value;
        }

        public int getIntegerValue() {
            return this.value;
        }
    }

    private static ContentWarningPrefsStorage instance;

    // This object will be used to store and retrieve all data
    // to and from the Activity's SharedPreferences file
    private final @NonNull SharedPreferences sharedPrefs;

    private ContentWarningPrefsStorage(final Activity activity) {
        this.sharedPrefs = activity.getPreferences(Context.MODE_PRIVATE);
    }

    // To implement singleton pattern, user must call this instance method instead of being
    // able to call the constructor directly
    @NonNull
    public static ContentWarningPrefsStorage getInstance(final Activity activity) {
        if (instance == null) {
            instance = new ContentWarningPrefsStorage(activity);
        }

        return instance;
    }

    // Helper function to convert integer to a ContentWarningVisibility enum value, returns
    // null if passed in integer is invalid and cannot be cast to the enum
    @Nullable
    public static ContentWarningVisibility getContentWarningVisibilityEnum(final Integer val) {
        if (val == null) {
            return null;
        }
        if (val == 0) {
            return ContentWarningVisibility.SHOW;
        }
        else if (val == 1) {
            return ContentWarningVisibility.WARN;
        }
        else if (val == 2) {
            return ContentWarningVisibility.HIDE;
        }
        else {
            return null;
        }
    }

    // Retrieves all content warning preferences set by the user from SharedPreferences
    @NonNull
    public Map<String, ContentWarningVisibility> getAllContentWarningPrefs() {
        final Map<String, ?> allPrefs = this.sharedPrefs.getAll();
        final Map<String, ContentWarningVisibility> contentWarningPrefs = new HashMap<>();

        // If nothing found in sharedPreferences, then just return an empty Map
        if (allPrefs == null || allPrefs.isEmpty()) {
            return contentWarningPrefs;
        }

        // Iterate though all the key/value pairs found in sharedPrefs
        for (final String key : allPrefs.keySet()) {
            // Ignore any non-integer values found in sharedPrefs
            if (allPrefs.get(key) instanceof Integer) {
                final Integer val = (Integer) allPrefs.get(key);
                final @Nullable ContentWarningVisibility contentWarningVisibility =
                        getContentWarningVisibilityEnum(val);

                // Only add visibility setting to the return map if the value found in sharedPrefs
                // corresponds to one of the allowed visibility settings
                if (contentWarningVisibility != null) {
                    contentWarningPrefs.put(key, contentWarningVisibility);
                }
            }
        }

        return contentWarningPrefs;
    }

    // Stores visibility setting for a content warning into sharedPrefs
    // (Overwrites old visibility setting for the content warning)
    public void storeContentWarningPref(final @NonNull String contentWarningName,
                                        final @NonNull ContentWarningVisibility visibilityOption) {
        final SharedPreferences.Editor sharedPrefsEditor = this.sharedPrefs.edit();

        // If content warning's visibility was changed to SHOW, don't store it in SharedPreferences
        // since this is the default visibility setting, so there is no reason to store it in memory
        if (visibilityOption == ContentWarningVisibility.SHOW) {
            // Remove this content warning's setting if it was previously stored in sharedPrefs
            if (this.sharedPrefs.contains(contentWarningName)) {
                sharedPrefsEditor.remove(contentWarningName).apply();
            }
        }
        // Otherwise, store this content warning's visibility setting into sharedPrefs
        else {
            sharedPrefsEditor.putInt(contentWarningName, visibilityOption.getIntegerValue())
                    .apply();
        }
    }
}
