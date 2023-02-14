package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.moviementor.R;

public class SettingsFragment extends Fragment {
    public SettingsFragment() {
        super(R.layout.settings_fragment);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @NonNull Bundle savedInstanceState) {
        // Bring progress bar to front so it's visible until loaded
        final ProgressBar loadingProgressWheel = requireActivity().findViewById(R.id.loading_circle);
        loadingProgressWheel.bringToFront();
    }
}
