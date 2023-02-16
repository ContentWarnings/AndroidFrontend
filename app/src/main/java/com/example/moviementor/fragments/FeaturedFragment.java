package com.example.moviementor.fragments;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private final static String IS_HIDDEN_KEY = "IS_HIDDEN";
    private boolean isHidden = true;

    public FeaturedFragment() {
        super(R.layout.featured_fragment);
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bring progress bar to front so it's visible until RecyclerView is populated
        final ProgressBar loadingProgressWheel = requireActivity().findViewById(R.id.loading_circle);
        loadingProgressWheel.bringToFront();

        // If this featured fragment is being created for the first time or it was visible right
        // before a configuration change (such as device rotation), make this fragment visible
        if (savedInstanceState == null || !savedInstanceState.getBoolean(IS_HIDDEN_KEY)) {
            this.onHiddenChanged(false);
        }
        // Otherwise, make this fragment hidden
        else {
            this.onHiddenChanged(true);
        }

        // Fetch currently trending movies from the database
        Backend.fetchTrendingMovies(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // If fragment should be hidden, update hidden boolean to true and make sure that tne
        // footer button for this fragment is deselected
        if (hidden) {
            this.isHidden = true;
            this.updateFeaturedTabButton(false);
        }
        // Otherwise, update hidden boolean to false and make sure that the footer button for this
        // fragment is selected
        else {
            this.isHidden = false;
            updateFeaturedTabButton(true);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        // Save current hidden/shown boolean status of fragment in outState, so that when the
        // device configuration changes (such as on rotation) the fragment's visibility status
        // can be recovered
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_HIDDEN_KEY, isHidden);
    }


    private void updateFeaturedTabButton(final boolean featuredTabSelected) {
        // Get footer icon for this fragment and its background
        final ImageView featuredFooterIcon = requireActivity().findViewById(R.id.featured_icon);
        final GradientDrawable gradientDrawable = (GradientDrawable) featuredFooterIcon.getBackground();

        // If this footer icon is selected, change its background color to purple
        if (featuredTabSelected) {
            gradientDrawable.setColor(getResources().
                    getColor(R.color.nav_button_selected_background_color, null));
        }
        // Otherwise, make its background color transparent
        else {
            gradientDrawable.setColor(getResources().
                    getColor(R.color.nav_button_unselected_background_color, null));
        }

        featuredFooterIcon.setBackground(gradientDrawable);

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
