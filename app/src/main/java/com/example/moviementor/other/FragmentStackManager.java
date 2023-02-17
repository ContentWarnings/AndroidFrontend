package com.example.moviementor.other;

import androidx.annotation.NonNull;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.fragments.BaseFragment;
import com.example.moviementor.fragments.BaseFragment.Tab;

public class FragmentStackManager {
    private static FragmentStackManager instance;

    private final FragmentStack featuredTabStack;
    private final FragmentStack searchTabStack;
    private final FragmentStack settingsTabStack;

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

    public void openNewPage(final @NonNull MainActivity mainActivity,
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

        mainActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment, tag).commit();
    }

    public void goBackAPage(final @NonNull MainActivity mainActivity,
                            final @NonNull Tab currentTab) {
        FragmentStack currFragmentStack;
        String tag;

        if (currentTab == Tab.FEATURED) {
            currFragmentStack = this.featuredTabStack;
            tag = MainActivity.TAG_FEATURED_FRAGMENT;
        }
        else if (currentTab == Tab.SEARCH) {
            currFragmentStack = this.searchTabStack;
            tag = MainActivity.TAG_SEARCH_FRAGMENT;
        }
        else {
            currFragmentStack = this.settingsTabStack;
            tag = MainActivity.TAG_SETTINGS_FRAGMENT;
        }

        if (!currFragmentStack.canGoBack()) {
            return;
        }

        currFragmentStack.pop();
        final BaseFragment newTopFragment = currFragmentStack.getTopFragment();

        mainActivity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, newTopFragment, tag).commit();
    }
}
