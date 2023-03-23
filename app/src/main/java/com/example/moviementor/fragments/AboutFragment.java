package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;

public class AboutFragment extends BaseFragment {
    public AboutFragment() {
        super(R.layout.about_fragment, Tab.SETTINGS);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button in header
        final ImageButton headerBackButton = view.findViewById(R.id.about_page_back_button);
        headerBackButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }
}
