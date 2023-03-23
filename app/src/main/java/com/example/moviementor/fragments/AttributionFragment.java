package com.example.moviementor.fragments;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;

public class AttributionFragment extends BaseFragment {
    public AttributionFragment() {
        super(R.layout.attribution_fragment, Tab.SETTINGS);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup back button in header
        final ImageButton headerBackButton = view.findViewById(R.id.attribution_page_back_button);
        headerBackButton.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        final TextView attributionParagraphText = view.findViewById(R.id.attribution_page_paragraph);
        attributionParagraphText.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
