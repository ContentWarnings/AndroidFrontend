package com.example.moviementor.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moviementor.R;

public class SettingsFragment extends Fragment {
    private final static String IS_HIDDEN_KEY = "IS_HIDDEN";
    private boolean isHidden = true;

    public SettingsFragment() {
        super(R.layout.settings_fragment);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If this featured fragment is being created for the first time or it was visible right
        // before a configuration change (such as device rotation), make this fragment visible
        if (savedInstanceState == null || !savedInstanceState.getBoolean(IS_HIDDEN_KEY)) {
            this.onHiddenChanged(false);
        }
        // Otherwise, make this fragment hidden
        else {
            this.onHiddenChanged(true);
        }

        // TODO: Setup Settings Page
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // If fragment should be hidden, update hidden boolean to true and make sure that tne
        // footer button for this fragment is deselected
        if (hidden) {
            this.isHidden = true;
            this.updateSettingsTabButton(false);
        }
        // Otherwise, update hidden boolean to false and make sure that the footer button for this
        // fragment is selected
        else {
            this.isHidden = false;
            this.updateSettingsTabButton(true);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        // Save current hidden/shown boolean status of fragment in outState, so that when the
        // device configuration changes (such as on rotation) the fragment's visibility status
        // can be recovered
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_HIDDEN_KEY, isHidden);
    }

    private void updateSettingsTabButton(final boolean settingsTabSelected) {
        // Get footer icon for this fragment and its background
        final ImageView settingsFooterIcon = requireActivity().findViewById(R.id.settings_icon);
        final GradientDrawable gradientDrawable = (GradientDrawable) settingsFooterIcon.getBackground();

        // If this footer icon is selected, change its background color to purple
        if (settingsTabSelected) {
            gradientDrawable.setColor(getResources().
                    getColor(R.color.nav_button_selected_background_color, null));
        }
        // Otherwise, make its background color transparent
        else {
            gradientDrawable.setColor(getResources().
                    getColor(R.color.nav_button_unselected_background_color, null));
        }

        settingsFooterIcon.setBackground(gradientDrawable);
    }
}
