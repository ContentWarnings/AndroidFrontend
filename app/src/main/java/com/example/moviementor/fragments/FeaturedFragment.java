package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.adapters.TrendingMoviesAdapter;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.ContentWarningPrefsStorage;
import com.example.moviementor.other.SpanSizeLookupWithHeader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FeaturedFragment extends BaseFragment implements TrendingMoviesAdapter.OnItemClickListener {
    // Holds onto the adapter for the trending page's RecyclerView
    private @Nullable TrendingMoviesAdapter trendingMoviesAdapter;

    public FeaturedFragment() {
        super(R.layout.featured_fragment, Tab.FEATURED);
        this.trendingMoviesAdapter = null;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bring progress bar to front so it's visible until RecyclerView is populated
        final ProgressBar loadingProgressWheel = view.findViewById(R.id.loading_circle);
        loadingProgressWheel.bringToFront();

        // If trending movies adapter has already been created, then setup the trending page
        // with this adapter and its list of trending movies
        if (this.trendingMoviesAdapter != null) {
            createAndPopulateTrendingMoviesList(new ArrayList<>());
        }
        // Otherwise, need to get trending movies from API before setting up the trending movies
        // list
        else {
            // Fetch currently trending movies from the database
            Backend.fetchTrendingMovies(this);
        }
    }

    public void createAndPopulateTrendingMoviesList(final @NonNull List<TrendingMovieViewModel> trendingMoviesData) {
        // Get number of columns that grid will render
        final int numColumns = getResources().getInteger(R.integer.num_trending_list_columns);

        final RecyclerView trendingMoviesRecyclerView = requireView().findViewById(R.id.trending_movies_recycler_view);

        // If trending movies adapter has not been setup for this fragment yet, then create it
        if (this.trendingMoviesAdapter == null) {
            // Get all current content warning preferences stored for the user
            final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                    .getInstance(requireActivity());
            final Map<String, ContentWarningPrefsStorage.ContentWarningVisibility> cwPrefsMap = cwPrefsStorage
                    .getAllContentWarningPrefs();

            this.trendingMoviesAdapter = new TrendingMoviesAdapter(trendingMoviesData, cwPrefsMap);

            // Attach fragment as listener to the trending movies RecyclerView
            this.trendingMoviesAdapter.setOnItemClickListener(this);
        }

        // Bind the adapter and a Grid Layout Manager to the RecyclerView
        trendingMoviesRecyclerView.setAdapter(this.trendingMoviesAdapter);
        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), numColumns);
        layoutManager.setSpanSizeLookup(new SpanSizeLookupWithHeader(numColumns));
        trendingMoviesRecyclerView.setLayoutManager(layoutManager);

        // Now that RecyclerView is populated, remove the loading progress wheel
        final ProgressBar loadingProgressWheel = requireView().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);

        // If the header is the only item in the adapter, then all movies were filtered out, so
        // display text to user, otherwise make sure this text is hidden
        final TextView allFeaturedMoviesFilteredOut = requireView().findViewById(R.id.all_featured_movies_filtered_out);
        if (this.trendingMoviesAdapter.getItemCount() <= 1) {
            allFeaturedMoviesFilteredOut.setVisibility(View.VISIBLE);
        }
        else {
            allFeaturedMoviesFilteredOut.setVisibility(View.GONE);
        }
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);

        // If page is being shown again, need to check if any content warning preferences have
        // changed since, so that the list of trending movies can be re-filtered if needed
        if (!hidden && this.trendingMoviesAdapter != null) {
            // Get all current content warning preferences stored for the user
            final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                    .getInstance(requireActivity());
            final Map<String, ContentWarningPrefsStorage.ContentWarningVisibility> cwPrefsMap = cwPrefsStorage
                    .getAllContentWarningPrefs();

            this.trendingMoviesAdapter.checkContentWarningPrefsChanged(cwPrefsMap);

            // If the header is the only item in the adapter, then all movies were filtered out, so
            // display text to user, otherwise make sure this text is hidden
            final TextView allFeaturedMoviesFilteredOut = requireView().findViewById(R.id.all_featured_movies_filtered_out);
            if (this.trendingMoviesAdapter.getItemCount() <= 1) {
                allFeaturedMoviesFilteredOut.setVisibility(View.VISIBLE);
            }
            else {
                allFeaturedMoviesFilteredOut.setVisibility(View.GONE);
            }
        }
    }

    // Function called by the listener attached to the child RecyclerView's adapter. Only called by
    // listener when the header's search button is clicked on
    @Override
    public void onHeaderSearchButtonClick() {
        // Route user's request to open search bar to the main activity
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.jumpToSearchBar();
    }

    // Function called by the listener attached to the child RecyclerView's adapter when a trending
    // movie item is clicked on
    @Override
    public void onMovieItemClick(final int movieId, final @NonNull String movieName) {
        // Route user's request to open movie page to the main activity
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.openMoviePage(movieId, movieName);
    }
}
