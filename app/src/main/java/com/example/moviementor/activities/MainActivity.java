package com.example.moviementor.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.moviementor.R;
import com.example.moviementor.fragments.BaseFragment;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.fragments.SearchFragment;
import com.example.moviementor.fragments.SettingsFragment;
import com.example.moviementor.other.FragmentStackManager;

public class MainActivity extends AppCompatActivity {
    public final static String TAG_FEATURED_FRAGMENT = "FEATURED_FRAGMENT";
    public final static String TAG_SEARCH_FRAGMENT = "SEARCH_FRAGMENT";
    public final static String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";

    @NonNull FragmentStackManager fragmentStackManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        this.fragmentStackManager = FragmentStackManager.getInstance();
        Log.d("YOO: ", "stack sizes: " +  fragmentStackManager.featuredTabStack.size() +
        " " + fragmentStackManager.searchTabStack.size() + " " + fragmentStackManager.settingsTabStack.size());

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

//    @Override
//    public void onSaveInstanceState(final Bundle outState) {
//        super.onSaveInstanceState(outState);
//
//        // Save the fragment stack
//        outState.put
//        outState.putParcelableArrayList("fragmentStack", new ArrayList<>(mFragmentStack));
//    }

    private void setupNavigationFooter() {
        // Get the three navigation footer tab buttons
        final LinearLayout featuredTab = findViewById(R.id.featured_tab);
        final LinearLayout searchTab = findViewById(R.id.search_tab);
        final LinearLayout settingsTab = findViewById(R.id.settings_tab);

        // Setup click action for featured button
        featuredTab.setOnClickListener(view -> {
            fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.FEATURED);
//            // Try to get featured page fragment in view hierarchy
//            @Nullable Fragment featuredFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_FEATURED_FRAGMENT);
//
//            // If featured fragment not found, need to create this fragment and add it to
//            // the view hierarchy
//            if (featuredFragment == null) {
//                featuredFragment = new FeaturedFragment();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, featuredFragment, TAG_FEATURED_FRAGMENT)
//                        .commit();
//            }
//
//            // Featured fragment is in view hierarchy, but it may be hidden, so make it visible
//            getSupportFragmentManager().beginTransaction().show(featuredFragment).commit();
//
//            // Try to get the other two page fragments
//            final @Nullable Fragment searchFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SEARCH_FRAGMENT);
//            final @Nullable Fragment settingsFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SETTINGS_FRAGMENT);
//
//            // If either of the other two page fragments are in the view hierarchy, make sure to
//            // hide them, since the featured fragment should only be shown right now
//            if (searchFragment != null) {
//                getSupportFragmentManager().beginTransaction().hide(searchFragment).commit();
//            }
//            if (settingsFragment != null) {
//                getSupportFragmentManager().beginTransaction().hide(settingsFragment).commit();
//            }
        });

        // Setup click action for search button
        searchTab.setOnClickListener(view -> {
            fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.SEARCH);

//            // Try to get search page fragment in view hierarchy
//            @Nullable Fragment searchFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SEARCH_FRAGMENT);
//
//            // If search fragment not found, need to create this fragment and add it to
//            // the view hierarchy
//            if (searchFragment == null) {
//                searchFragment = new SearchFragment();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, searchFragment, TAG_SEARCH_FRAGMENT)
//                        .commit();
//            }
//
//            // Search fragment is in view hierarchy, but it may be hidden, so make it visible
//            getSupportFragmentManager().beginTransaction().show(searchFragment).commit();
//
//            // Try to get the other two page fragments
//            final @Nullable Fragment featuredFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_FEATURED_FRAGMENT);
//            final @Nullable Fragment settingsFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SETTINGS_FRAGMENT);
//
//            // If either of the other two page fragments are in the view hierarchy, make sure to
//            // hide them, since the search fragment should only be shown right now
//            if (featuredFragment != null) {
//                getSupportFragmentManager().beginTransaction().hide(featuredFragment).commit();
//            }
//            if (settingsFragment != null) {
//                getSupportFragmentManager().beginTransaction().hide(settingsFragment).commit();
//            }
        });

        // Setup click action for settings button
        settingsTab.setOnClickListener(view -> {
            fragmentStackManager.switchTab(getSupportFragmentManager(), BaseFragment.Tab.SETTINGS);

//            // Try to get settings page fragment in view hierarchy
//            @Nullable Fragment settingsFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SETTINGS_FRAGMENT);
//
//            // If settings fragment not found, need to create this fragment and add it to
//            // the view hierarchy
//            if (settingsFragment == null) {
//                settingsFragment = new SettingsFragment();
//                getSupportFragmentManager().beginTransaction()
//                        .add(R.id.fragment_container, settingsFragment, TAG_SETTINGS_FRAGMENT)
//                        .commit();
//            }
//
//            // Settings fragment is in view hierarchy, but it may be hidden, so make it visible
//            getSupportFragmentManager().beginTransaction().show(settingsFragment).commit();
//
//            // Try to get the other two page fragments
//            final @Nullable Fragment featuredFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_FEATURED_FRAGMENT);
//            final @Nullable Fragment searchFragment = getSupportFragmentManager()
//                    .findFragmentByTag(TAG_SEARCH_FRAGMENT);
//
//            // If either of the other two page fragments are in the view hierarchy, make sure to
//            // hide them, since the settings fragment should only be shown right now
//            if (featuredFragment != null) {
//                getSupportFragmentManager().beginTransaction().hide(featuredFragment).commit();
//            }
//            if (searchFragment != null) {
//                getSupportFragmentManager().beginTransaction().hide(searchFragment).commit();
//            }
        });
    }
}
