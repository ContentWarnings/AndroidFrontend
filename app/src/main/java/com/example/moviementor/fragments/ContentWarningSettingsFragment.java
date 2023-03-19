package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;

public class ContentWarningSettingsFragment extends BaseFragment {
    private static final String STORED_CONTENT_WARNING_NAME_KEY = "CONTENT_WARNING_NAME";

    private @Nullable String contentWarningName;

    public ContentWarningSettingsFragment() {
        super(R.layout.content_warning_settings_fragment, null);
        this.contentWarningName = null;
    }

    // Called right after constructor when this fragment is created to give page initial data
    // to use
    public void assignContentWarning(final @NonNull String contentWarningName) {
        this.contentWarningName = contentWarningName;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If movie page was recreated, then recover movie's id and name in saved instance state
        if (savedInstanceState != null) {
            this.contentWarningName = savedInstanceState.getString(STORED_CONTENT_WARNING_NAME_KEY);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STORED_CONTENT_WARNING_NAME_KEY, this.contentWarningName);
    }

    // When content warning settings fragment is removed from view hierarchy, nullify its content
    // warning name so that content warning info fetched from the API does not try to populate this
    // page anymore
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.contentWarningName = null;
    }
}
