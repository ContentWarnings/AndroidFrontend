package com.example.moviementor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.example.moviementor.R;
import com.example.moviementor.fragments.BaseFragment;
import com.example.moviementor.other.AdvancedSearchOptionsModal;
import com.example.moviementor.other.FragmentStackManager;
import com.example.moviementor.other.SearchOptions;

public class MainActivity extends AppCompatActivity {
    private static final String SEARCH_OPTIONS_MODAL_TAG = "searchOptionsModal";

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

    // Open the advanced search options modal on top of the current (search) page
    public void openAdvancedSearchOptionsModal(final @NonNull SearchOptions searchOptions) {
        // Try to get search options modal if currently on screen
        final Fragment findModal = getSupportFragmentManager()
                .findFragmentByTag(SEARCH_OPTIONS_MODAL_TAG);

        // If the search options modal was found then don't add open another modal
        // on top of the current one
        if (findModal != null) {
            return;
        }

        final AdvancedSearchOptionsModal modal = new AdvancedSearchOptionsModal();
        modal.setSearchOptions(searchOptions);
        modal.show(getSupportFragmentManager(), SEARCH_OPTIONS_MODAL_TAG);
    }

    // Tell the fragment stack manager to notify the search page that new search search options were
    // selected, so that search results are re-fetched and re-populated on screen
    public void applyNewSearchOptions() {
        this.fragmentStackManager.applyNewSearchOptions();
    }
}
