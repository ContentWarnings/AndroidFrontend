package com.example.moviementor.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URL;
import java.util.Date;
import java.util.List;

public class SearchResultMovieViewModel {
    public static final int MISSING_RUNTIME = -1;

    private final int movieId;
    private final @NonNull String movieName;
    private final @Nullable Date releaseDate;
    private final @Nullable URL movieImageUrl;
    private final @NonNull String movieOverview;
    private final int runtime;
    private final @NonNull List<String> genres;
    private final @NonNull List<String> contentWarnings;

    public SearchResultMovieViewModel(final int movieId, final @NonNull String movieName,
                                      final @Nullable Date releaseDate, final @Nullable URL movieImageUrl,
                                      final @NonNull String movieOverview, final int runtime,
                                      final @NonNull List<String> genres, final @NonNull List<String> contentWarnings) {
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
