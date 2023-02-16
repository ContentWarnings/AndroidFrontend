package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;

public class SettingsFragment extends BaseFragment {

    public SettingsFragment() {
        super(R.layout.settings_fragment, Tab.SETTINGS);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO: Setup Settings Page
    }
}
