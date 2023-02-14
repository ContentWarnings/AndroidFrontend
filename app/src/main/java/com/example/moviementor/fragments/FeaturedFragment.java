package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.adapters.TrendingMoviesAdapter;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.SpanSizeLookupWithHeader;

import java.util.List;

public class FeaturedFragment extends Fragment {
    public FeaturedFragment() {
        super(R.layout.featured_fragment);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @NonNull Bundle savedInstanceState) {
        // Bring progress bar to front so it's visible until RecyclerView is populated
        final ProgressBar loadingProgressWheel = requireActivity().findViewById(R.id.loading_circle);
        loadingProgressWheel.bringToFront();

        // Fetch currently trending movies from the database
        Backend.fetchTrendingMovies(this);
    }

    public void createAndPopulateTrendingMoviesList(final @NonNull List<TrendingMovieViewModel> trendingMoviesData) {
        // Get number of columns that grid will render
        final int numColumns = getResources().getInteger(R.integer.num_trending_list_columns);

        // Initialize RecyclerView and its adapter
        final RecyclerView trendingMoviesRecyclerView = requireActivity().findViewById(R.id.trending_movies_recycler_view);
        final TrendingMoviesAdapter trendingMoviesAdapter = new TrendingMoviesAdapter(trendingMoviesData);

        // Bind the adapter and a Grid Layout Manager to the RecyclerView
        trendingMoviesRecyclerView.setAdapter(trendingMoviesAdapter);
        final GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), numColumns);
        layoutManager.setSpanSizeLookup(new SpanSizeLookupWithHeader(numColumns));
        trendingMoviesRecyclerView.setLayoutManager(layoutManager);

        // Now that RecyclerView is populated, remove the loading progress wheel
        final ProgressBar loadingProgressWheel = requireActivity().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);
    }
}
