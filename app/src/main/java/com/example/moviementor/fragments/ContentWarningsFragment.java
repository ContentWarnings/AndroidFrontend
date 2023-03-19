package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.adapters.ContentWarningsSettingsAdapter;

public class ContentWarningsFragment extends BaseFragment implements ContentWarningsSettingsAdapter.OnItemClickListener {
    public ContentWarningsFragment() {
        super(R.layout.content_warnings_fragment, Tab.SETTINGS);
    }


    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupContentWarningsPage();
    }

    private void setupContentWarningsPage() {
        // Load array of content warnings and their corresponding descriptions from resources
        final String[] contentWarningNames = getResources()
                .getStringArray(R.array.content_warning_names);
        final String[] contentWarningDescriptions = getResources()
                .getStringArray(R.array.content_warning_descriptions);

        // Initialize RecyclerView and its adapter
        final RecyclerView contentWarningsRecyclerView = requireView()
                .findViewById(R.id.content_warnings_settings_recycler_view);
        final ContentWarningsSettingsAdapter cwSettingsAdapter =
                new ContentWarningsSettingsAdapter(contentWarningNames, contentWarningDescriptions);

        // Attach fragment as listener to the content warnings settings RecyclerView
        cwSettingsAdapter.setOnItemClickListener(this);

        // Bind the adapter and a Linear Layout Manager to the RecyclerView
        contentWarningsRecyclerView.setAdapter(cwSettingsAdapter);
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
}
