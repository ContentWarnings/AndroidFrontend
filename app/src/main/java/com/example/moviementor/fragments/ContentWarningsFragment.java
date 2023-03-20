package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.adapters.ContentWarningsSettingsAdapter;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.ContentWarningPrefsStorage;
import com.example.moviementor.other.ContentWarningPrefsStorage.ContentWarningVisibility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContentWarningsFragment extends BaseFragment implements ContentWarningsSettingsAdapter.OnItemClickListener {
    private @Nullable ContentWarningsSettingsAdapter contentWarningsSettingsAdapter;

    public ContentWarningsFragment() {
        super(R.layout.content_warnings_fragment, Tab.SETTINGS);
        this.contentWarningsSettingsAdapter = null;
    }


    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bring progress bar to front so it's visible until RecyclerView is populated
        final ProgressBar loadingProgressWheel = view.findViewById(R.id.loading_circle);
        loadingProgressWheel.bringToFront();

        // If content warnings settings adapter has already been created, then setup the content
        // warning settings page with this adapter and its list of content warning names
        if (this.contentWarningsSettingsAdapter != null) {
            setupContentWarningsPage(new ArrayList<>());
        }
        // Otherwise, need to get list of content warning names from API before setting up the page
        else {
            Backend.fetchContentWarningNames(this);
        }
    }

    public void setupContentWarningsPage(final @NonNull List<String> contentWarningNames) {
        // Hide loading progress wheel since content warnings are ready to populate screen
        final ProgressBar loadingProgressWheel = requireView().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);

        final RecyclerView contentWarningsRecyclerView = requireView()
                .findViewById(R.id.content_warnings_settings_recycler_view);

        // Get all current content warning preferences stored for the user
        final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                .getInstance(requireActivity());
        final Map<String, ContentWarningVisibility> cwPrefsMap = cwPrefsStorage
                .getAllContentWarningPrefs();

        // If content warnings settings adapter has not been setup for this fragment yet, then
        // create it
        if (this.contentWarningsSettingsAdapter == null) {
            this.contentWarningsSettingsAdapter = new
                    ContentWarningsSettingsAdapter(contentWarningNames, cwPrefsMap);

            // Attach fragment as listener to the content warnings settings RecyclerView
            this.contentWarningsSettingsAdapter.setOnItemClickListener(this);
        }
        else {
            // This page may have been reopened with new content warning preferences, so try to
            // update the adapter in case any changes were made
            this.contentWarningsSettingsAdapter.updateContentWarningPrefs(cwPrefsMap);
        }

        // Bind the adapter and a Linear Layout Manager to the RecyclerView
        contentWarningsRecyclerView.setAdapter(this.contentWarningsSettingsAdapter);
        contentWarningsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    // Function called by the listener attached to the child RecyclerView's adapter. Only called by
    // listener when the header's search button is clicked on
    @Override
    public void onHeaderSearchButtonClick() {
        // Route user's request to open search bar to the main activity
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.jumpToSearchBar();
    }

    // Function called by the listener attached to the child RecyclerView's adapter. Only called by
    // listener when the header's back button is clicked on
    @Override
    public void onHeaderBackButtonClick() {
        // Tell the main activity to go back a page
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.onBackPressed();
    }

    // Function called by the listener attached to the child RecyclerView's adapter. Only called by
    // listener when a content warning row is clicked on
    @Override
    public void onContentWarningRowClick(final @NonNull String contentWarningName,
                                         final ContentWarningVisibility contentWarningVisibility) {
        // Route user's request to open content warning page to the main activity
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.openContentWarningSettingsPage(contentWarningName, contentWarningVisibility);
    }
}
