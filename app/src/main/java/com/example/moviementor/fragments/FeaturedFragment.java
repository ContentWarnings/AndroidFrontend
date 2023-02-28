package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.adapters.TrendingMoviesAdapter;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.SpanSizeLookupWithHeader;

import java.util.List;

public class FeaturedFragment extends BaseFragment implements TrendingMoviesAdapter.OnItemClickListener {

    public FeaturedFragment() {
        super(R.layout.featured_fragment, Tab.FEATURED);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bring progress bar to front so it's visible until RecyclerView is populated
        final ProgressBar loadingProgressWheel = view.findViewById(R.id.loading_circle);
        loadingProgressWheel.bringToFront();

        // Fetch currently trending movies from the database
        Backend.fetchTrendingMovies(this);
    }

    public void createAndPopulateTrendingMoviesList(final @NonNull List<TrendingMovieViewModel> trendingMoviesData) {
        // Get number of columns that grid will render
        final int numColumns = getResources().getInteger(R.integer.num_trending_list_columns);

        // Initialize RecyclerView and its adapter
        final RecyclerView trendingMoviesRecyclerView = requireView().findViewById(R.id.trending_movies_recycler_view);
        final TrendingMoviesAdapter trendingMoviesAdapter = new TrendingMoviesAdapter(trendingMoviesData);

        // Attach fragment as listener to the trending movies RecyclerView
        trendingMoviesAdapter.setOnItemClickListener(this);

        // Bind the adapter and a Grid Layout Manager to the RecyclerView
        trendingMoviesRecyclerView.setAdapter(trendingMoviesAdapter);
        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), numColumns);
        layoutManager.setSpanSizeLookup(new SpanSizeLookupWithHeader(numColumns));
        trendingMoviesRecyclerView.setLayoutManager(layoutManager);

        // Now that RecyclerView is populated, remove the loading progress wheel
        final ProgressBar loadingProgressWheel = requireView().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);
    }

    // Function called by the listener attached to the child RecyclerView's adapter. Only called by
    // listener when the header's search button is clicked on
    @Override
    public void onItemClick() {
        // Route user's request to open search bar to the main activity
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.jumpToSearchBar();
    }
}
