package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.models.MovieViewModel;
import com.example.moviementor.other.Backend;

public class MovieFragment extends BaseFragment {
    private static final String STORED_MOVIE_ID_KEY = "STORED_MOVIE_ID";
    private static final String STORED_MOVIE_NAME_KEY = "STORED_MOVIE_NAME";

    private int movieId;
    private @NonNull String movieName;

    public MovieFragment() {
        super(R.layout.movie_fragment, null);
    }

    // Called right after constructor when MovieFragment is created to give page initial data
    // to use
    public void assignMovie(final int movieId, final @NonNull String movieName) {
        this.movieId = movieId;
        this.movieName = movieName;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If movie page was recreated, then recover movie's id and name in saved instance state
        if (savedInstanceState != null) {
            this.movieId = savedInstanceState.getInt(STORED_MOVIE_ID_KEY);
            this.movieName = savedInstanceState.getString(STORED_MOVIE_NAME_KEY);
        }

        // Setup back button in header
        final ImageButton backButton = requireView().findViewById(R.id.movie_page_header_back_button);
        backButton.setOnClickListener(viewBackButton -> {
            requireActivity().onBackPressed();
        });

        // Set header's movie title initially while movie's data is being fetched
        // and the rest of the page is waiting to be populated
        final TextView moviePageTitle = requireView().findViewById(R.id.movie_page_title);
        moviePageTitle.setText(this.movieName);

        // Fetch movie's data from API to populate the page
        Backend.fetchMovie(this, this.movieId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STORED_MOVIE_ID_KEY, this.movieId);
        outState.putString(STORED_MOVIE_NAME_KEY, this.movieName);
    }

    public void populateMoviePage(final @Nullable MovieViewModel movieData) {
        // If movie was not found or JSON returned for movie was invalid, then don't populate
        // this movie page
        if (movieData == null) {
            return;
        }

        // If movie's name fetched from API is different from movie name initially passed to the
        // movie page, then set header title of the page to the new movie's name
        if (!this.movieName.equals(movieData.getMovieName())) {
            final TextView moviePageTitle = requireView().findViewById(R.id.movie_page_title);
            moviePageTitle.setText(movieData.getMovieName());
        }

        // TODO: populate movie page with this movie's data
    }
}
