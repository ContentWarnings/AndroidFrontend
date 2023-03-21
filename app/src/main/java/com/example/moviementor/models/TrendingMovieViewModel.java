package com.example.moviementor.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URL;
import java.util.List;

public class TrendingMovieViewModel {
    private final int movieId;
    private final @NonNull String movieName;
    private final @Nullable URL movieImageUrl;
    private final @NonNull List<String> contentWarnings;

    public TrendingMovieViewModel(final int movieId,
                                  final @NonNull String movieName,
                                  final @Nullable URL movieImageUrl,
                                  final @NonNull List<String> contentWarnings) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.movieImageUrl = movieImageUrl;
        this.contentWarnings = contentWarnings;
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

    @NonNull
    public List<String> getContentWarnings() {
        return this.contentWarnings;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof TrendingMovieViewModel)) {
            return false;
        }

        return this.movieId == ((TrendingMovieViewModel) other).getMovieId();
    }
}
