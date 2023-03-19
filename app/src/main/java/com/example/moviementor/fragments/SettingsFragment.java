package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;

public class SettingsFragment extends BaseFragment {

    public SettingsFragment() {
        super(R.layout.settings_fragment, Tab.SETTINGS);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSettingsPage();
    }

    private void setupSettingsPage() {
        final TextView headerTitle = requireView().findViewById(R.id.header_title);
        final ImageButton headerSearchButton = requireView().findViewById(R.id.header_search_button);

        // Set the setting page's header with the corresponding title text
        headerTitle.setText(R.string.settings_page_header_title);

        // Setup click listener on header's search button to start user's request of jumping to the
        // search bar on the search page
        headerSearchButton.setOnClickListener(v -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.jumpToSearchBar();
        });

        final LinearLayout contentWarningsSettingsRow = requireView()
                .findViewById(R.id.content_warnings_settings_row);
        final LinearLayout aboutSettingsRow = requireView().findViewById(R.id.about_settings_row);

        // Setup click listener to open content warnings page when content warnings row is
        // clicked on
        contentWarningsSettingsRow.setOnClickListener(view -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.openContentWarningsPage();
        });

        // Setup click listener to open about page when about row is clicked on
        aboutSettingsRow.setOnClickListener(view -> {
            final MainActivity mainActivity = (MainActivity) requireActivity();
            mainActivity.openAboutPage();
        });
    }
}
