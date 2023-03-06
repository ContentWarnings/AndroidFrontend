package com.example.moviementor.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URL;

public class TrendingMovieViewModel {
    private final int movieId;
    private final @NonNull String movieName;
    private final @NonNull URL movieImageUrl;

    public TrendingMovieViewModel(final int movieId, final @NonNull String movieName,
                                  final @NonNull URL movieImageUrl) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.movieImageUrl = movieImageUrl;
    }

    public int getMovieId() {
        return this.movieId;
    }

    @NonNull
    public String getMovieName() {
        return this.movieName;
    }

    @Nullable
    public URL getMovieImageUrl() {
        return this.movieImageUrl;
    }
}
