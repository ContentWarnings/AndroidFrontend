package com.example.moviementor.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.other.ContentWarning;
import com.example.moviementor.other.StreamingProvider;

import java.net.URL;
import java.util.Date;
import java.util.List;

public class MovieViewModel {
    public static final int MISSING_RUNTIME = -1;
    public static final double MISSING_RATING = -1.0;
    public static final String MISSSING_MPA_RATING = "Unknown";

    private final int movieId;
    private final @NonNull String movieName;
    private final @Nullable Date releaseDate;
    private final int movieRuntime;
    private final @Nullable URL movieImageUrl;
    private final @NonNull String movieOverview;
    private final double movieRating;
    private final @NonNull String movieMpaRating;
    private final @NonNull List<String> genres;
    private final @NonNull List<StreamingProvider> streamingProviders;
    private final @NonNull List<ContentWarning> contentWarnings;

    public MovieViewModel(final int movieId, final @NonNull String movieName,
                          final @Nullable Date releaseDate, final int movieRuntime,
                          final @Nullable URL movieImageUrl, final @NonNull String movieOverview,
                          final double movieRating, final @NonNull String movieMpaRating,
                          final @NonNull List<String> genres,
                          final @NonNull List<StreamingProvider> streamingProviders,
                          final @NonNull List<ContentWarning> contentWarnings) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.releaseDate = releaseDate;
        this.movieRuntime = movieRuntime;
        this.movieImageUrl = movieImageUrl;
        this.movieOverview = movieOverview;
        this.movieRating = movieRating;
        this.movieMpaRating = movieMpaRating;
        this.genres = genres;
        this.streamingProviders = streamingProviders;
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
    public Date getReleaseDate() {
        return this.releaseDate;
    }

    public int getMovieRuntime() {
        return this.movieRuntime;
    }

    @Nullable
    public URL getMovieImageUrl() {
        return this.movieImageUrl;
    }

    @NonNull
    public String getMovieOverview() {
        return this.movieOverview;
    }

    public double getMovieRating() {
        return this.movieRating;
    }

    @NonNull
    public String getMovieMpaRating() {
        return this.movieMpaRating;
    }

    @NonNull
    public List<String> getGenres() {
        return this.genres;
    }

    @NonNull
    public List<StreamingProvider> getStreamingProviders() {
        return this.streamingProviders;
    }

    @NonNull
    public List<ContentWarning> getContentWarnings() {
        return this.contentWarnings;
    }
}
