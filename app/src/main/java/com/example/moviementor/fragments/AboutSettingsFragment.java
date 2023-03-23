package com.example.moviementor.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;

public class AboutSettingsFragment extends BaseFragment {

    public AboutSettingsFragment() {
        super(R.layout.about_settings_fragment, Tab.SETTINGS);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupAboutSettingsPage(view);
    }

    private void setupAboutSettingsPage(final @NonNull View fragmentView) {
        final ImageButton headerBackButton = fragmentView
                .findViewById(R.id.about_settings_page_back_button);
        final ImageButton headerSearchButton = fragmentView
                .findViewById(R.id.about_settings_page_header_search_button);

        // Setup back button in header
        headerBackButton.setOnClickListener(view -> {
            requireActivity().onBackPressed();
        });

        // Setup click listener on header's search button to start user's request of jumping to the
        // search bar on the search page
        headerSearchButton.setOnClickListener(v -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.jumpToSearchBar();
        });

        final LinearLayout aboutSettingsRow = fragmentView.findViewById(R.id.about_settings_row);
        final LinearLayout attributionSettingsRow = fragmentView
                .findViewById(R.id.attribution_settings_row);
        final LinearLayout privacyPolicySettingsRow = fragmentView
                .findViewById(R.id.privacy_policy_settings_row);
        final LinearLayout termsOfServiceSettingsRow = fragmentView
                .findViewById(R.id.terms_of_service_settings_row);

        // Setup click listener to open about page when this row is clicked on
        aboutSettingsRow.setOnClickListener(view -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.openAboutPage();
        });

        // Setup click listener to open attributions page when this row is clicked on
        attributionSettingsRow.setOnClickListener(view -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            //mainActivity.openAttributionsPage();
        });

        // Setup click listener to open privacy policy page when this row is clicked on
        privacyPolicySettingsRow.setOnClickListener(view -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            //mainActivity.openPrivacyPolicyPage();
        });

        // Setup click listener to open terms of service page when this row is clicked on
        termsOfServiceSettingsRow.setOnClickListener(view -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            //mainActivity.openTermsOfServicePage();
        });
    }
}
