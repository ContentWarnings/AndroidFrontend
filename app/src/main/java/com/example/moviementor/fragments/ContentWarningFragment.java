package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.other.ContentWarning;

public class ContentWarningFragment extends BaseFragment {
    private @Nullable ContentWarning contentWarning;

    public ContentWarningFragment() {
        super(R.layout.content_warning_fragment, null);
        this.contentWarning = null;
    }

    // Called right after constructor when ContentWarningFragment is created to give page content
    // warning data to populate page with
    public void assignContentWarning(final @NonNull ContentWarning contentWarning) {
        this.contentWarning = contentWarning;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // When content warning page is recreated due to a configuration change, then just go
        // back to previous page since the content warning data is lost on this page
        if (this.contentWarning == null) {
            requireActivity().onBackPressed();
            return;
        }

        // Setup back button in header
        final ImageButton backButton = requireView()
                .findViewById(R.id.content_warning_page_header_back_button);
        backButton.setOnClickListener(viewBackButton -> {
            requireActivity().onBackPressed();
        });

        // Set header's title with name of this content warning prefixed by "CW:"
        final TextView contentWarningPageTitle = requireView()
                .findViewById(R.id.content_warning_page_title);
        final String cwPageTitleText = getString(R.string.content_warning_page_title_prefix) + " "
                + this.contentWarning.getContentWarningName();
        contentWarningPageTitle.setText(cwPageTitleText);
    }
}
