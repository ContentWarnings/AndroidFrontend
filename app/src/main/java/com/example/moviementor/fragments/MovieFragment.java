package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;

public class MovieFragment extends BaseFragment {
    private int movieId;

    public MovieFragment() {
        super(R.layout.movie_fragment, null);
    }

    public void assignMovie(final int movieId) {
        this.movieId = movieId;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
