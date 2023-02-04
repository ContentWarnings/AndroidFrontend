package com.example.moviementor.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class TrendingMovieViewModel {
    private String movieName;
    private Drawable movieImage;

    public TrendingMovieViewModel(@NonNull final String movieName, @NonNull final Drawable movieImage) {
        this.movieName = movieName;
        this.movieImage = movieImage;
    }

    @NonNull
    public String getMovieName() {
        return this.movieName;
    }

    @NonNull
    public Drawable getMovieImage() {
        return this.movieImage;
    }
}
