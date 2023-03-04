package com.example.moviementor.other;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SearchOptions {
    public static enum GenreFilter {
        ACTION, ADVENTURE, ANIMATION, COMEDY, CRIME, DOCUMENTARY, DRAMA, FAMILY,
        FANTASY, HISTORY, HORROR, MUSIC, MYSTERY, ROMANCE, SCIENCE_FICTION, TV_Movie,
        THRILLER, WAR, WESTERN
    }

    // Default is Relevance Ascending which is represented by a null (none) sort option value
    public static enum SortOption {
        RELEVANCE_DESCENDING, TITLE_ASCENDING, TITLE_DESCENDING, RELEASE_DATE_ASCENDING,
        RELEASE_DATE_DESCENDING, RATING_ASCENDING, RATING_DESCENDING, MPA_RATING_ASCENDING,
        MPA_RATING_DESCENDING, OVERVIEW_ASCENDING, OVERVIEW_DESCENDING, RUNTIME_ASCENDING,
        RUNTIME_DESCENDING, GENRES_ASCENDING, GENRES_DESCENDING, CONTENT_WARNING_ASCENDING,
        CONTENT_WARNING_DESCENDING
    }

    private @Nullable GenreFilter genreFilter;
    private @Nullable SortOption sortOption;

    // Start out with no search options selected
    public SearchOptions() {
        this.genreFilter = null;
        this.sortOption = null;
    }

    private SearchOptions(final @Nullable GenreFilter genreFilter,
                          final @Nullable SortOption sortOption) {
        this.genreFilter = genreFilter;
        this.sortOption = sortOption;
    }

    public void setGenreFilter(final @Nullable GenreFilter newGenreFilter) {
        this.genreFilter = newGenreFilter;
    }

    public void setSortOption(final @Nullable SortOption newSortOption) {
        this.sortOption = newSortOption;
    }

    public @Nullable GenreFilter currentGenreFilterSelected() {
        return this.genreFilter;
    }

    public @Nullable SortOption currentSortOptionSelected() {
        return this.sortOption;
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

    // Helper function that returns sort option enum from a sort option's string
    public static @Nullable SortOption getSortOption(final @NonNull String sortOption) {
        switch (sortOption) {
            case "Relevance Ascending":
                return null;
            case "Relevance Descending":
                return SortOption.RELEVANCE_DESCENDING;
            case "Title Ascending":
                return SortOption.TITLE_ASCENDING;
            case "Title Descending":
                return SortOption.TITLE_DESCENDING;
            case "Release Date Ascending":
                return SortOption.RELEASE_DATE_ASCENDING;
            case "Release Date Descending":
                return SortOption.RELEASE_DATE_DESCENDING;
            case "Rating Ascending":
                return SortOption.RATING_ASCENDING;
            case "Rating Descending":
                return SortOption.RATING_DESCENDING;
            case "Mpa Rating Ascending":
                return SortOption.MPA_RATING_ASCENDING;
            case "Overview Ascending":
                return SortOption.OVERVIEW_ASCENDING;
            case "Overview Descending":
                return SortOption.OVERVIEW_DESCENDING;
            case "Runtime Ascending":
                return SortOption.RUNTIME_ASCENDING;
            case "Runtime Descending":
                return SortOption.RUNTIME_DESCENDING;
            case "Genres Ascending":
                return SortOption.GENRES_ASCENDING;
            case "Genres Descending":
                return SortOption.GENRES_DESCENDING;
            case "Content Warnings Ascending":
                return SortOption.CONTENT_WARNING_ASCENDING;
            default:
                return SortOption.CONTENT_WARNING_DESCENDING;
        }
    }

    // Returns an identical copy of this search options object
    @NonNull
    public SearchOptions copy() {
        return new SearchOptions(this.genreFilter, this.sortOption);
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof SearchOptions)) {
            return false;
        }

        final SearchOptions otherOptions = (SearchOptions) other;

        return this.currentGenreFilterSelected() == otherOptions.currentGenreFilterSelected()
                && this.currentSortOptionSelected() == otherOptions.currentSortOptionSelected();
    }
}
