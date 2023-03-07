package com.example.moviementor.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.net.URL;
import java.util.Date;
import java.util.List;

public class SearchResultMovieViewModel {
    private final int movieId;
    private final @NonNull String movieName;
    private final @Nullable Date releaseDate;
    private final @Nullable URL movieImageUrl;
    private final @NonNull String movieOverview;
    private final @NonNull List<String> genres;
    private final @NonNull List<String> contentWarnings;

    public SearchResultMovieViewModel(final int movieId, final @NonNull String movieName,
                                      final @Nullable Date releaseDate, final @Nullable URL movieImageUrl,
                                      final @NonNull String movieOverview, final @NonNull List<String> genres,
                                      final @NonNull List<String> contentWarnings) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.releaseDate = releaseDate;
        this.movieImageUrl = movieImageUrl;
        this.movieOverview = movieOverview;
        this.genres = genres;
        this.contentWarnings = contentWarnings;
    }

    public int getMovieId() {
        return this.movieId;
    }

    @Nullable
    public URL getMovieImageUrl() {
        return this.movieImageUrl;
    }

    @NonNull
    public String getMovieName() {
        return this.movieName;
    }

    @NonNull
    public List<String> getGenres() {
        return this.genres;
    }

    @Nullable
    public Date getReleaseDate() {
        return this.releaseDate;
    }

    @NonNull
    public String getMovieOverview() {
        return this.movieOverview;
    }

    @NonNull
    public List<String> getContentWarnings() {
        return this.contentWarnings;
    }
}
