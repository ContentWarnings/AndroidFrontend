package com.example.moviementor.other;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.example.moviementor.R;
import com.example.moviementor.fragments.BaseFragment;
import com.example.moviementor.fragments.BaseFragment.Tab;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.fragments.SearchFragment;
import com.example.moviementor.fragments.SettingsFragment;

// Singleton class that manages the operations of each tab's fragment stack, and manages the
// addition/removal of fragments from the app's view hierarchy (fragment container)
public class FragmentStackManager {
    public final static String TAG_FEATURED_FRAGMENT = "FEATURED_FRAGMENT";
    public final static String TAG_SEARCH_FRAGMENT = "SEARCH_FRAGMENT";
    public final static String TAG_SETTINGS_FRAGMENT = "SETTINGS_FRAGMENT";

    private static FragmentStackManager instance;

    // Fragment stacks for each tab
    public final FragmentStack featuredTabStack;
    public final FragmentStack searchTabStack;
    public final FragmentStack settingsTabStack;

    // Keeps track of whichever tab is currently opened
    private static Tab currentTabOpened = Tab.FEATURED;

    private FragmentStackManager () {
        // Init fragment stacks for each tab
        this.featuredTabStack = new FragmentStack();
        this.searchTabStack = new FragmentStack();
        this.settingsTabStack = new FragmentStack();
    }

    // To implement singleton pattern, user must call this instance method instead of being
    // able to call the constructor directly
    @NonNull
    public static FragmentStackManager getInstance() {
        if (instance == null) {
            instance = new FragmentStackManager();
        }
        return instance;
    }

    // Returns an instance of the first fragment that should be added for each tab
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

    // Called when MainActivity is recreated. When this happens, all the fragments currently in the
    // view hierarchy are recreated, so the top of each non-empty fragment stack needs to be
    // replaced with each recreated fragment in the view hierarchy. Otherwise, when trying to switch
    // between tabs after activity recreation, the program will reference a stale fragment in one
    // of the fragment stacks causing the program to crash since this stale fragment will not be
    // attached to the view hierarchy
    public void replaceTopLevelFragmentsOnStacks(final @NonNull FragmentManager fragmentManager) {
        // Try to get any recreated fragments in the view hierarchy
        final @Nullable BaseFragment currentFeaturedFragment = (BaseFragment) fragmentManager.
                findFragmentByTag(TAG_FEATURED_FRAGMENT);
        final @Nullable BaseFragment currentSearchFragment = (BaseFragment) fragmentManager.
                findFragmentByTag(TAG_SEARCH_FRAGMENT);
        final @Nullable BaseFragment currentSettingsFragment = (BaseFragment) fragmentManager.
                findFragmentByTag(TAG_SETTINGS_FRAGMENT);

        // Replace the top of each non-empty fragment stack with these recreated fragments
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

    // Called when the user clicks on a button in the navigation footer to switch to a new tab.
    public void switchTab(final @NonNull FragmentManager fragmentManager,
                          final @NonNull Tab newTab) {
        FragmentStack destFragmentStack;
        String tag;

        // Get the fragment stack and tag of the new tab that is being switched to
        if (newTab == Tab.FEATURED) {
            destFragmentStack = this.featuredTabStack;
            tag = TAG_FEATURED_FRAGMENT;
        }
        else if (newTab == Tab.SEARCH) {
            destFragmentStack = this.searchTabStack;
            tag = TAG_SEARCH_FRAGMENT;
        }
        else {
            destFragmentStack = this.settingsTabStack;
            tag = TAG_SETTINGS_FRAGMENT;
        }

        final BaseFragment topFragment;

        // User has not opened this tab yet since launching the app, so its fragment stack is empty.
        // Need to get the starting fragment for this tab, add it to the tab's fragment stack, and
        // then add it to the app's view hierarchy
        if (destFragmentStack.isEmpty()) {
            topFragment = getStartFragment(newTab);
            destFragmentStack.push(topFragment);
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, topFragment, tag)
                    .commit();
        }
        // This tab has already been opened, so get its current top level fragment that is present
        // in the view hierarchy
        else {
            topFragment = destFragmentStack.getTopFragment();
        }

        // Tab's fragment is in view hierarchy, but it may be hidden, so make it visible
        fragmentManager.beginTransaction().show(topFragment).commit();

        // If either of the other two tab fragments are in the view hierarchy, make sure to
        // hide them, since the new tab's fragment should only be shown right now
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

        // Update the new tab that is opened
        currentTabOpened = newTab;
    }

    // Used to open a new page/fragment from the current tab
    public void openNewPage(final @NonNull FragmentManager fragmentManager,
                            final @NonNull BaseFragment fragment,
                            final @NonNull Tab currentTab) {
        String tag;

        // Get the tag of the tab that the new fragment is being loaded into and push this new
        // fragment to the top of this tab's fragment stack
        if (currentTab == Tab.FEATURED) {
            this.featuredTabStack.push(fragment);
            tag = TAG_FEATURED_FRAGMENT;
        }
        else if (currentTab == Tab.SEARCH) {
            this.searchTabStack.push(fragment);
            tag = TAG_SEARCH_FRAGMENT;
        }
        else {
            this.settingsTabStack.push(fragment);
            tag = TAG_SETTINGS_FRAGMENT;
        }

        // Overwrite this tab's existing fragment in the view hierarchy with this new one
        fragmentManager.beginTransaction().replace(R.id.fragment_container, fragment, tag).commit();
    }

    // Used to go back to the last fragment/page in the current tab
    public void goBackAPage(final @NonNull FragmentManager fragmentManager) {
        FragmentStack currFragmentStack;
        String tag;

        // Get the tag and fragment stack of the current tab that the user is trying to go back in
        if (currentTabOpened == Tab.FEATURED) {
            currFragmentStack = this.featuredTabStack;
            tag = TAG_FEATURED_FRAGMENT;
        } else if (currentTabOpened == Tab.SEARCH) {
            currFragmentStack = this.searchTabStack;
            tag = TAG_SEARCH_FRAGMENT;
        } else {
            currFragmentStack = this.settingsTabStack;
            tag = TAG_SETTINGS_FRAGMENT;
        }

        // If on the starting page for a tab, simply don't let the user go back since there is
        // no history before this page/fragment
        if (!currFragmentStack.canGoBack()) {
            return;
        }

        // Remove the currently displayed fragment from the top of the current tab's fragment stack
        currFragmentStack.pop();
        final BaseFragment newTopFragment = currFragmentStack.getTopFragment();

        if (newTopFragment == null) {
            return;
        }

        // Load the previous fragment into the view hierarchy for this tab
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, newTopFragment, tag).commit();
    }
}
