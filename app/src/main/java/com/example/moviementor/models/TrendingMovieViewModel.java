package com.example.moviementor.models;

import androidx.annotation.NonNull;

public class TrendingMovieViewModel {
    private final @NonNull String movieName;
    private final @NonNull String movieImageUrl;

    public TrendingMovieViewModel(final @NonNull String movieName, final @NonNull String movieImageUrl) {
        this.movieName = movieName;
        this.movieImageUrl = movieImageUrl;
    }

    @NonNull
    public String getMovieName() {
        return this.movieName;
    }

    @NonNull
    public String getMovieImageUrl() {
        return this.movieImageUrl;
    }
}
