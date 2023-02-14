package com.example.moviementor.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.moviementor.R;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.fragments.SearchFragment;
import com.example.moviementor.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity {
    private final static String TAG_FEATURED_FRAGMENT = "FEATURED_FRAGMENT";
    private final static String TAG_SEARCH_FRAGMENT = "SEARCH_FRAGMENT";
    private final static String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        setupNavigationFooter();

        // Fragment is only loaded into container when savedInstanceState is null. This ensures that
        // the home page fragment is added only once: when the main app activity is first created
        if (savedInstanceState == null) {
            final Fragment fragment = new FeaturedFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, TAG_FEATURED_FRAGMENT).commit();
        }
    }

    private void setupNavigationFooter() {
        // Get the three navigation footer tabs
        final LinearLayout featuredTab = findViewById(R.id.featured_tab);
        final LinearLayout searchTab = findViewById(R.id.search_tab);
        final LinearLayout settingsTab = findViewById(R.id.settings_tab);

        featuredTab.setOnClickListener(view -> {
            @Nullable Fragment featuredFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_FEATURED_FRAGMENT);

            if (featuredFragment == null) {
                featuredFragment = new FeaturedFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, featuredFragment, TAG_FEATURED_FRAGMENT)
                        .commit();
            }
            else {
                getSupportFragmentManager().beginTransaction().show(featuredFragment).commit();
            }

            final @Nullable Fragment searchFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_SEARCH_FRAGMENT);
            final @Nullable Fragment settingsFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_SETTINGS_FRAGMENT);

            if (searchFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(searchFragment).commit();
            }
            if (settingsFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(settingsFragment).commit();
            }
        });

        searchTab.setOnClickListener(view -> {
            @Nullable Fragment searchFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_SEARCH_FRAGMENT);

            if (searchFragment == null) {
                searchFragment = new SearchFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, searchFragment, TAG_SEARCH_FRAGMENT)
                        .commit();
            }
            else {
                getSupportFragmentManager().beginTransaction().show(searchFragment).commit();
            }

            final @Nullable Fragment featuredFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_FEATURED_FRAGMENT);
            final @Nullable Fragment settingsFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_SETTINGS_FRAGMENT);

            if (featuredFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(featuredFragment).commit();
            }
            if (settingsFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(settingsFragment).commit();
            }
        });

        settingsTab.setOnClickListener(view -> {
            @Nullable Fragment settingsFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_SETTINGS_FRAGMENT);

            if (settingsFragment == null) {
                settingsFragment = new SettingsFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, settingsFragment, TAG_SETTINGS_FRAGMENT)
                        .commit();
            }
            else {
                getSupportFragmentManager().beginTransaction().show(settingsFragment).commit();
            }

            final @Nullable Fragment featuredFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_FEATURED_FRAGMENT);
            final @Nullable Fragment searchFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_SEARCH_FRAGMENT);

            if (featuredFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(featuredFragment).commit();
            }
            if (searchFragment != null) {
                getSupportFragmentManager().beginTransaction().hide(searchFragment).commit();
            }
        });
    }
}
