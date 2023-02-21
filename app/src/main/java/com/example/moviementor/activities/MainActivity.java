package com.example.moviementor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.moviementor.R;
import com.example.moviementor.fragments.BaseFragment;
import com.example.moviementor.other.FragmentStackManager;

public class MainActivity extends AppCompatActivity {
    private final @NonNull FragmentStackManager fragmentStackManager;

    public MainActivity() {
        super();
        this.fragmentStackManager = FragmentStackManager.getInstance();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // If MainActivity was recreated, current fragments in the view hierarchy were
        // recreated, so need to overwrite the stale fragments in the fragment stacks with the
        // new ones
        if (savedInstanceState != null) {
            fragmentStackManager.replaceTopLevelFragmentsOnStacks(getSupportFragmentManager());
        }

        setupNavigationFooter();

        // Fragment is only loaded into container when savedInstanceState is null. This ensures that
        // the home page fragment is added only once: when the main app activity is first created
        if (savedInstanceState == null) {
            // Simulate click on featured tab button in order to make app open to featured page
            // when app is first launched
            final LinearLayout featuredTabButton = findViewById(R.id.featured_tab);
            featuredTabButton.performClick();
        }
    }

    @Override
    public void onBackPressed() {
        this.fragmentStackManager.goBackAPage(getSupportFragmentManager());
    }

    private void setupNavigationFooter() {
        // Get the three navigation footer tab buttons
        final LinearLayout featuredTab = findViewById(R.id.featured_tab);
        final LinearLayout searchTab = findViewById(R.id.search_tab);
        final LinearLayout settingsTab = findViewById(R.id.settings_tab);

        // Setup click action for featured button
        featuredTab.setOnClickListener(view -> {
            fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.FEATURED);
        });

        // Setup click action for search button
        searchTab.setOnClickListener(view -> {
            fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.SEARCH);
        });

        // Setup click action for settings button
        settingsTab.setOnClickListener(view -> {
            fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.SETTINGS);
        });
    }

    // User requested to switch directly to search tab and open up search bar, so first make sure
    // that search tab is either empty or displaying the starting SearchFragment, and then
    // direct this request to fragment stack manager right after
    public void jumpToSearchBar() {
        fragmentStackManager.goToRootSearchTab(getSupportFragmentManager());
        fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.SEARCH, true);
    }
}
