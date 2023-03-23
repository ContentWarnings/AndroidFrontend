package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;

public class TermsOfServiceFragment extends BaseFragment {
    public TermsOfServiceFragment() {
        super(R.layout.terms_of_service_fragment, Tab.SETTINGS);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button in header
        final ImageButton headerBackButton = view.findViewById(R.id.terms_of_service_page_back_button);
        headerBackButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }
}
