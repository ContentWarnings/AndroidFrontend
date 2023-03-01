package com.example.moviementor.other;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SearchOptions {
    public static enum GenreFilter {
        ACTION, ADVENTURE, ANIMATION, COMEDY, CRIME, DOCUMENTARY, DRAMA, FAMILY,
        FANTASY, HISTORY, HORROR, MUSIC, MYSTERY, ROMANCE, SCIENCE_FICTION, TV_Movie,
        THRILLER, WAR, WESTERN
    }

    private @Nullable GenreFilter genreFilter;

    // Start out with no search options selected
    public SearchOptions() {
        this.genreFilter = null;
    }

    public void setGenreFilter(final @Nullable GenreFilter newGenreFilter) {
        this.genreFilter = newGenreFilter;
    }

    public @Nullable GenreFilter currentGenreFilterSelected() {
        return this.genreFilter;
    }

    // Helper function that returns genre filter enum from a genre's name
    public static @Nullable GenreFilter getGenreFilter(final @NonNull String genreName) {
        switch (genreName) {
            case "Action":
                return GenreFilter.ACTION;
            case "Adventure":
                return GenreFilter.ADVENTURE;
            case "Animation":
                return GenreFilter.ANIMATION;
            case "Comedy":
                return GenreFilter.COMEDY;
            case "Crime":
                return GenreFilter.CRIME;
            case "Documentary":
                return GenreFilter.DOCUMENTARY;
            case "Drama":
                return GenreFilter.DRAMA;
            case "Family":
                return GenreFilter.FAMILY;
            case "Fantasy":
                return GenreFilter.FANTASY;
            case "History":
                return GenreFilter.HISTORY;
            case "Horror":
                return GenreFilter.HORROR;
            case "Music":
                return GenreFilter.MUSIC;
            case "Mystery":
                return GenreFilter.MYSTERY;
            case "Romance":
                return GenreFilter.ROMANCE;
            case "Science Fiction":
                return GenreFilter.SCIENCE_FICTION;
            case "TV Movie":
                return GenreFilter.TV_Movie;
            case "Thriller":
                return GenreFilter.THRILLER;
            case "War":
                return GenreFilter.WAR;
            case "Western":
                return GenreFilter.WESTERN;
            default:
                return null;
        }
    }
}
