package com.example.moviementor.models;

import androidx.annotation.NonNull;

public class GenreViewModel {
    private final @NonNull String genreName;
    private final int genreBackgroundImageRes;

    public GenreViewModel(final @NonNull String genreName, final int genreBackgroundImageRes) {
        this.genreName = genreName;
        this.genreBackgroundImageRes = genreBackgroundImageRes;
    }

    @NonNull
    public String getGenreName() {
        return this.genreName;
    }

    public int getGenreBackgroundImageRes() {
        return this.genreBackgroundImageRes;
    }
}
