package com.example.moviementor.fragments;

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
}
