package com.example.moviementor.models;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class GenreViewModel {
    private final @NonNull String genreName;
    private final @DrawableRes int genreBackgroundImageRes;

    public GenreViewModel(final @NonNull String genreName, final @DrawableRes int genreBackgroundImageRes) {
        this.genreName = genreName;
        this.genreBackgroundImageRes = genreBackgroundImageRes;
    }

    @NonNull
    public String getGenreName() {
        return this.genreName;
    }

    @DrawableRes
    public int getGenreBackgroundImageRes() {
        return this.genreBackgroundImageRes;
    }
}
