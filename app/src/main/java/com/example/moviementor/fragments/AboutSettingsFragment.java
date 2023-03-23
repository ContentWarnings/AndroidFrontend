package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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

        setupAboutSettingsPage();
    }

    private void setupAboutSettingsPage() {
        final ImageButton headerBackButton = requireView()
                .findViewById(R.id.about_settings_page_back_button);
        final ImageButton headerSearchButton = requireView()
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
    }
}
