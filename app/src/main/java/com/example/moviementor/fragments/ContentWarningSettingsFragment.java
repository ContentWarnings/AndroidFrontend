package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.other.Backend;

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

        // Setup back button in header
        final ImageButton backButton = requireView()
                .findViewById(R.id.content_warning_settings_page_back_button);
        backButton.setOnClickListener(viewBackButton -> {
            requireActivity().onBackPressed();
        });

        // Set header's title initially while content warning's data is being fetched
        // and the rest of the page is waiting to be populated
        final TextView cwSettingsPageTitle = requireView()
                .findViewById(R.id.content_warning_settings_page_header_title);
        cwSettingsPageTitle.setText(this.contentWarningName);

        // Fetch movie's data from API to populate the page
        Backend.fetchContentWarningDescription(this, this.contentWarningName);
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

    public void setupContentWarningSettingsPage(final @Nullable String contentWarningDescription) {
        // If description was not found or JSON returned for content warning was invalid, then don't
        // populate content warning's settings page
        if (contentWarningDescription == null) {
            return;
        }
        // If this fragment has been removed from view hierarchy before content warning's
        // description was retrieved to populate the page, then ignore this outdated content
        // warning data
        else if (this.contentWarningName == null) {
            return;
        }

        // Hide loading progress wheel since page is ready to be populated
        final ProgressBar loadingProgressWheel = requireView().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);

        // Populate the content warning's description text
        final TextView contentWarningDescriptionText = requireView()
                .findViewById(R.id.content_warning_description);
        contentWarningDescriptionText.setText(contentWarningDescription);

        // Make the rest of the items on the page visible
        final LinearLayout contentWarningSettingsPageOptions = requireView()
                .findViewById(R.id.content_warning_settings_page_options);
        contentWarningSettingsPageOptions.setVisibility(View.VISIBLE);
    }
}
