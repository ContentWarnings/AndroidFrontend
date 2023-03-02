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

    private SearchOptions(final @Nullable GenreFilter genreFilter) {
        this.genreFilter = genreFilter;
    }

    public void setGenreFilter(final @Nullable GenreFilter newGenreFilter) {
        this.genreFilter = newGenreFilter;
    }

    public @Nullable GenreFilter currentGenreFilterSelected() {
        return this.genreFilter;
    }

    // Returns name of genre for the genre filter enum currently selected. If no filter enum
    // is selected, then "Disregard" is returned to represent no genre filter choice
    @NonNull
    public String getGenreFilterName() {
        if (this.genreFilter == GenreFilter.ACTION) {
            return "Action";
        }
        else if (this.genreFilter == GenreFilter.ADVENTURE) {
            return "Adventure";
        }
        else if (this.genreFilter == GenreFilter.ANIMATION) {
            return "Animation";
        }
        else if (this.genreFilter == GenreFilter.COMEDY) {
            return "Comedy";
        }
        else if (this.genreFilter == GenreFilter.CRIME) {
            return "Crime";
        }
        else if (this.genreFilter == GenreFilter.DOCUMENTARY) {
            return "Documentary";
        }
        else if (this.genreFilter == GenreFilter.DRAMA) {
            return "Drama";
        }
        else if (this.genreFilter == GenreFilter.FAMILY) {
            return "Family";
        }
        else if (this.genreFilter == GenreFilter.FANTASY) {
            return "Fantasy";
        }
        else if (this.genreFilter == GenreFilter.HISTORY) {
            return "History";
        }
        else if (this.genreFilter == GenreFilter.HORROR) {
            return "Horror";
        }
        else if (this.genreFilter == GenreFilter.MUSIC) {
            return "Music";
        }
        else if (this.genreFilter == GenreFilter.MYSTERY) {
            return "Mystery";
        }
        else if (this.genreFilter == GenreFilter.ROMANCE) {
            return "Romance";
        }
        else if (this.genreFilter == GenreFilter.SCIENCE_FICTION) {
            return "Science Fiction";
        }
        else if (this.genreFilter == GenreFilter.TV_Movie) {
            return "TV Movie";
        }
        else if (this.genreFilter == GenreFilter.THRILLER) {
            return "Thriller";
        }
        else if (this.genreFilter == GenreFilter.WAR) {
            return "War";
        }
        else if (this.genreFilter == GenreFilter.WESTERN) {
            return "Western";
        }
        else {
            return "Disregard";
        }
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

    // Returns an identical copy of this search options object
    @NonNull
    public SearchOptions copy() {
        return new SearchOptions(this.genreFilter);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SearchOptions)) {
            return false;
        }

        return this.currentGenreFilterSelected() == ((SearchOptions) other).currentGenreFilterSelected();
    }
}
