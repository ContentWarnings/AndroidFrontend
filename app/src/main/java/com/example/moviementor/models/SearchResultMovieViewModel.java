package com.example.moviementor.models;

import androidx.annotation.NonNull;

import java.util.Date;

public class SearchResultMovieViewModel {
    private final int movieId;
    private final @NonNull String movieName;
    private final @NonNull Date releaseDate;
    private final @NonNull String movieImageUrl;
    private final @NonNull String movieOverview;
    private final int runtime;
    private final @NonNull String[] genres;
    private final @NonNull String[] contentWarnings;

    public SearchResultMovieViewModel(final int movieId, final @NonNull String movieName,
                                      final @NonNull Date releaseDate, final @NonNull String movieImageUrl,
                                      final @NonNull String movieOverview, final int runtime,
                                      final @NonNull String[] genres, final @NonNull String[] contentWarnings) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.releaseDate = releaseDate;
        this.movieImageUrl = movieImageUrl;
        this.movieOverview = movieOverview;
        this.runtime = runtime;
        this.genres = genres;
        this.contentWarnings = contentWarnings;
    }

}
