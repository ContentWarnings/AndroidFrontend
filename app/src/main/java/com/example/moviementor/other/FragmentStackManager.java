package com.example.moviementor.other;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.fragments.BaseFragment;
import com.example.moviementor.fragments.BaseFragment.Tab;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.fragments.SearchFragment;
import com.example.moviementor.fragments.SettingsFragment;

public class FragmentStackManager {
    private static FragmentStackManager instance;

    public final FragmentStack featuredTabStack;
    public final FragmentStack searchTabStack;
    public final FragmentStack settingsTabStack;

    private FragmentStackManager () {
        // Init fragment stacks for each tab
        this.featuredTabStack = new FragmentStack();
        this.searchTabStack = new FragmentStack();
        this.settingsTabStack = new FragmentStack();
    }

    @NonNull
    public static FragmentStackManager getInstance() {
        if (instance == null) {
            instance = new FragmentStackManager();
        }
        return instance;
    }

    private BaseFragment getStartFragment(final @NonNull Tab tab) {
        if (tab == Tab.FEATURED) {
            return new FeaturedFragment();
        }
        else if (tab == Tab.SEARCH) {
            return new SearchFragment();
        }
        else {
            return new SettingsFragment();
        }
    }

    public void replaceTopLevelFragmentsOnStacks(final @NonNull FragmentManager fragmentManager) {
        final @Nullable BaseFragment currentFeaturedFragment = (BaseFragment) fragmentManager.
                findFragmentByTag(MainActivity.TAG_FEATURED_FRAGMENT);
        final @Nullable BaseFragment currentSearchFragment = (BaseFragment) fragmentManager.
                findFragmentByTag(MainActivity.TAG_SEARCH_FRAGMENT);
        final @Nullable BaseFragment currentSettingsFragment = (BaseFragment) fragmentManager.
                findFragmentByTag(MainActivity.TAG_SETTINGS_FRAGMENT);

        if (currentFeaturedFragment != null) {
            this.featuredTabStack.pop();
            this.featuredTabStack.push(currentFeaturedFragment);
        }
        if (currentSearchFragment != null) {
            this.searchTabStack.pop();
            this.searchTabStack.push(currentSearchFragment);
        }
        if (currentSettingsFragment != null) {
            this.settingsTabStack.pop();
            this.settingsTabStack.push(currentSettingsFragment);
        }
    }

    public void switchTab(final @NonNull FragmentManager fragmentManager,
                          final @NonNull Tab newTab) {
        FragmentStack destFragmentStack;
        String tag;

        if (newTab == Tab.FEATURED) {
            destFragmentStack = this.featuredTabStack;
            tag = MainActivity.TAG_FEATURED_FRAGMENT;
        }
        else if (newTab == Tab.SEARCH) {
            destFragmentStack = this.searchTabStack;
            tag = MainActivity.TAG_SEARCH_FRAGMENT;
        }
        else {
            destFragmentStack = this.settingsTabStack;
            tag = MainActivity.TAG_SETTINGS_FRAGMENT;
        }

        final BaseFragment topFragment;

        if (destFragmentStack.isEmpty()) {
            topFragment = getStartFragment(newTab);
            destFragmentStack.push(topFragment);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, topFragment, tag)
                    .commit();
        }
        else {
            topFragment = destFragmentStack.getTopFragment();
        }

        fragmentManager.beginTransaction().show(topFragment).commit();

        if (newTab != Tab.FEATURED && !featuredTabStack.isEmpty()) {
            final BaseFragment featuredFragment = featuredTabStack.getTopFragment();
            fragmentManager.beginTransaction().hide(featuredFragment).commit();
        }
        if (newTab != Tab.SEARCH && !searchTabStack.isEmpty()) {
            final BaseFragment searchFragment = searchTabStack.getTopFragment();
            fragmentManager.beginTransaction().hide(searchFragment).commit();
        }
        if (newTab != Tab.SETTINGS && !settingsTabStack.isEmpty()) {
            final BaseFragment settingsFragment = settingsTabStack.getTopFragment();
            fragmentManager.beginTransaction().hide(settingsFragment).commit();
        }
    }

    public void openNewPage(final @NonNull FragmentManager fragmentManager,
                            final @NonNull BaseFragment fragment,
                            final @NonNull Tab currentTab) {
        String tag;

        if (currentTab == Tab.FEATURED) {
            this.featuredTabStack.push(fragment);
            tag = MainActivity.TAG_FEATURED_FRAGMENT;
        }
        else if (currentTab == Tab.SEARCH) {
            this.searchTabStack.push(fragment);
            tag = MainActivity.TAG_SEARCH_FRAGMENT;
        }
        else {
            this.settingsTabStack.push(fragment);
            tag = MainActivity.TAG_SETTINGS_FRAGMENT;
        }

        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
    }

    public void goBackAPage(final @NonNull FragmentManager fragmentManager,
                            final @NonNull Tab currentTab) {
        FragmentStack currFragmentStack;
        String tag;

        if (currentTab == Tab.FEATURED) {
            currFragmentStack = this.featuredTabStack;
            tag = MainActivity.TAG_FEATURED_FRAGMENT;
        } else if (currentTab == Tab.SEARCH) {
            currFragmentStack = this.searchTabStack;
            tag = MainActivity.TAG_SEARCH_FRAGMENT;
        } else {
            currFragmentStack = this.settingsTabStack;
            tag = MainActivity.TAG_SETTINGS_FRAGMENT;
        }

        if (!currFragmentStack.canGoBack()) {
            return;
        }

        currFragmentStack.pop();
        final BaseFragment newTopFragment = currFragmentStack.getTopFragment();

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, newTopFragment, tag).commit();
    }
}
