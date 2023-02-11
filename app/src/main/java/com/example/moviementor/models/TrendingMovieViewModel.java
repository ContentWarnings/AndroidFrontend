package com.example.moviementor.models;

// Drawable provides a generic API for dealing with a visual resource
// that may take different forms (Ex: png, jpc, svg...)
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

public class TrendingMovieViewModel {
    private final @NonNull String movieName;
    private final @NonNull Bitmap movieImage;

    public TrendingMovieViewModel(final @NonNull String movieName, final @NonNull Bitmap movieImage) {
        this.movieName = movieName;
        this.movieImage = movieImage;
    }

    @NonNull
    public String getMovieName() {
        return this.movieName;
    }

    @NonNull
    public Bitmap getMovieImage() {
        return this.movieImage;
    }
}
