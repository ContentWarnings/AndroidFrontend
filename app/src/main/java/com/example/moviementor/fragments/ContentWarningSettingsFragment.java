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
import com.example.moviementor.other.ContentWarningPrefsStorage;
import com.example.moviementor.other.ContentWarningPrefsStorage.ContentWarningVisibility;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class ContentWarningSettingsFragment extends BaseFragment {
    private static final int NO_VISIBILITY_FOUND = -1;

    private static final String STORED_CONTENT_WARNING_NAME_KEY = "CONTENT_WARNING_NAME";
    private static final String STORED_CONTENT_WARNING_VISIBILITY_KEY = "CONTENT_WARNING_VISIBILITY";

    private static float ACTIVATED_ALPHA = 1.0f;
    private static float DEACTIVATED_ALPHA = 0.7f;

    private @Nullable String contentWarningName;
    private @Nullable ContentWarningVisibility contentWarningVisibility;

    public ContentWarningSettingsFragment() {
        super(R.layout.content_warning_settings_fragment, null);
        this.contentWarningName = null;
        this.contentWarningVisibility = null;
    }

    // Called right after constructor when this fragment is created to give page initial data
    // to use
    public void assignContentWarning(final @NonNull String contentWarningName,
                                     final ContentWarningVisibility contentWarningVisibility) {
        this.contentWarningName = contentWarningName;
        this.contentWarningVisibility = contentWarningVisibility;

        // Should never happen but safety check to make sure that visibility of content warning is
        // always valid (default to SHOW if invalid)
        if (contentWarningVisibility == null) {
            this.contentWarningVisibility = ContentWarningVisibility.SHOW;
        }
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If content warning settings page was recreated, then recover content warning's name and
        // visibility from saved instance state
        if (savedInstanceState != null) {
            this.contentWarningName = savedInstanceState.getString(STORED_CONTENT_WARNING_NAME_KEY);
            this.contentWarningVisibility = ContentWarningPrefsStorage
                    .getContentWarningVisibilityEnum(savedInstanceState.getInt(STORED_CONTENT_WARNING_VISIBILITY_KEY));

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

        // Fetch content warning's description from API
        Backend.fetchContentWarningDescription(this, this.contentWarningName);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Get content warning's current visibility as int value but default to
        // NO_VISIBILITY_FOUND if visibility object is set to null currently
        final int storeContentWarningVisibility = (this.contentWarningVisibility == null) ?
                NO_VISIBILITY_FOUND : this.contentWarningVisibility.getIntegerValue();

        outState.putString(STORED_CONTENT_WARNING_NAME_KEY, this.contentWarningName);
        outState.putInt(STORED_CONTENT_WARNING_VISIBILITY_KEY, storeContentWarningVisibility);
    }

    // When content warning settings fragment is removed from view hierarchy, nullify its content
    // warning name and visibility so that content warning info fetched from the API does not try
    // to populate this page anymore
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.contentWarningName = null;
        this.contentWarningVisibility = null;
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
        else if (this.contentWarningName == null || this.contentWarningVisibility == null) {
            return;
        }

        // Hide loading progress wheel since page is ready to be populated
        final ProgressBar loadingProgressWheel = requireView().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);

        // Populate the content warning's description text
        final TextView contentWarningDescriptionText = requireView()
                .findViewById(R.id.content_warning_description);
        contentWarningDescriptionText.setText(contentWarningDescription);

        final SwitchMaterial showHideToggleButton = requireView()
                .findViewById(R.id.show_content_warning_toggle_button);
        final SwitchMaterial warnToggleButton = requireView()
                .findViewById(R.id.display_warning_toggle_button);

        // Configure switch buttons to be set to the content warning's current visibility
        // status
        if (this.contentWarningVisibility == ContentWarningVisibility.SHOW) {
            // Just turn the show switch on
            showHideToggleButton.setChecked(true);
        }
        else if (this.contentWarningVisibility == ContentWarningVisibility.WARN) {
            // Need to turn both switches to selected since a warning can't be shown for a movie
            // if it is not able to be shown in the first place
            showHideToggleButton.setChecked(true);
            warnToggleButton.setChecked(true);
        }
        else {
            final LinearLayout displayWarningToggleRow = requireView()
                    .findViewById(R.id.display_warning_toggle_row);
            final TextView displayWarningDescription = requireView()
                    .findViewById(R.id.display_warning_description);

            // Grey out display warning toggle button and text and deactivate this toggle button
            // since current visibility of content warning is set to "HIDE" which means no warning
            // can be shown until movies with this content warning are not hidden anymore
            displayWarningToggleRow.setAlpha(DEACTIVATED_ALPHA);
            displayWarningDescription.setAlpha(DEACTIVATED_ALPHA);
            warnToggleButton.setAlpha(DEACTIVATED_ALPHA);
            warnToggleButton.setEnabled(false);

        }

        showHideToggleButton.setOnCheckedChangeListener((compoundButton, checked) -> {
            // Update the visibility status for the content warning
            this.contentWarningVisibility =
                    (checked) ? ContentWarningVisibility.SHOW : ContentWarningVisibility.HIDE;

            // Store the new visibility status for this content warning in
            // local storage (sharedPrefs)
            final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                    .getInstance(requireActivity());
            cwPrefsStorage.storeContentWarningPref(this.contentWarningName, this.contentWarningVisibility);

            final LinearLayout displayWarningToggleRow = requireView()
                    .findViewById(R.id.display_warning_toggle_row);
            final TextView displayWarningDescription = requireView()
                    .findViewById(R.id.display_warning_description);
            final SwitchMaterial displayWarningToggleButton = requireView()
                    .findViewById(R.id.display_warning_toggle_button);

            if (this.contentWarningVisibility == ContentWarningVisibility.HIDE) {
                // If content warning visibility is set to "HIDE", then grey out and deactivate the
                // display warning toggle button and text since no warning can be shown until
                // movies with this content warning are not hidden anymore
                displayWarningToggleRow.setAlpha(DEACTIVATED_ALPHA);
                displayWarningDescription.setAlpha(DEACTIVATED_ALPHA);
                displayWarningToggleButton.setAlpha(DEACTIVATED_ALPHA);

                // Also, deactivate the display warning toggle button and make sure it is turned
                // off
                displayWarningToggleButton.setEnabled(false);
                displayWarningToggleButton.setChecked(false);
            }
            else {
                // Otherwise, content warning visibility has been set to "SHOW", so make the
                // display warning toggle button and text not greyed out anymore since warnings
                // can be show again for movies with this content warning
                displayWarningToggleRow.setAlpha(ACTIVATED_ALPHA);
                displayWarningDescription.setAlpha(ACTIVATED_ALPHA);
                displayWarningToggleButton.setAlpha(ACTIVATED_ALPHA);

                // Also, reactivate the display warning toggle button
                displayWarningToggleButton.setEnabled(true);
            }
        });

        // Make all items on the page visible
        final LinearLayout contentWarningSettingsPageOptions = requireView()
                .findViewById(R.id.content_warning_settings_page_options);
        contentWarningSettingsPageOptions.setVisibility(View.VISIBLE);
    }
}
