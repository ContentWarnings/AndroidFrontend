package com.example.moviementor.other;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AdvancedSearchOptionsModal extends BottomSheetDialogFragment {
    private static final int EMPTY_GENRE_FILTER = -1;
    private static final double MODAL_PERCENT_HEIGHT_PARENT = 0.8;

    // Object that keeps track of selected search options for user
    private @NonNull SearchOptions searchOptions;

    // Keeps track of user's currently selected genre filter in the modal
    private @Nullable SearchOptions.GenreFilter currentlySelectedGenreFilter;

    // Keeps track of user's currently selected sort option in the modal
    private @Nullable SearchOptions.SortOption currentlySelectedSortOption;

    private boolean setupModal;

    // Default Constructor
    public AdvancedSearchOptionsModal() {
        super();
    }

    // Called immediately after constructor to pass data to the modal fragment
    public void setSearchOptions(final @NonNull SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
        this.currentlySelectedGenreFilter = null;
        this.currentlySelectedSortOption = null;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable
            ViewGroup container, final @Nullable Bundle savedInstanceState)
    {
        final View advancedSearchOptionsModal = inflater.inflate(R.layout.advanced_search_options_modal,
                container, false);

        // Set this boolean flag to false since modal has not been set up yet
        this.setupModal = false;

        // If trying to redisplay modal after configuration change, don't initialize modal at all
        if (savedInstanceState != null) {
            return advancedSearchOptionsModal;
        }

        // Otherwise, modal was opened by the user pressing the filter button, so the modal
        // will be properly initialized below
        this.setupModal = true;

        final ImageButton closeModalButton = advancedSearchOptionsModal
                .findViewById(R.id.close_advanced_options_menu_button);

        // When "x" button is pressed in top left corner of modal, then close
        // the modal
        closeModalButton.setOnClickListener(view -> dismiss());

        setupGenreFilterButtonListeners(advancedSearchOptionsModal);
        setupSortOptionButtonListeners(advancedSearchOptionsModal);

        // If modal was opened with previous genre filter selected, then find the associated button
        // for that genre filter and change it's state to selected
        final @IdRes int genreFilterSelectedButtonRes = getGenreFilterButtonRes(this.searchOptions.currentGenreFilterSelected());
        if (genreFilterSelectedButtonRes != EMPTY_GENRE_FILTER) {
            final RadioButton genreFilterButton = advancedSearchOptionsModal.findViewById(genreFilterSelectedButtonRes);
            genreFilterButton.setChecked(true);
        }

        // If modal was opened with previous sort option selected, then find the associated button
        // for that sort option and change it's state to selected. If no sort option was previously
        // selected, then just set the default "relevance_ascending" sort option button's state to
        // selected
        final @IdRes int sortOptionSelectedButtonRes = getSortOptionButtonRes(this.searchOptions.currentSortOptionSelected());
        final RadioButton sortOptionButton = advancedSearchOptionsModal.findViewById(sortOptionSelectedButtonRes);
        sortOptionButton.setChecked(true);

        // Get the modal's footer buttons
        final Button clearSearchOptionsButton = advancedSearchOptionsModal.findViewById(R.id.clear_search_options_button);
        final Button applySearchOptionsButton = advancedSearchOptionsModal.findViewById(R.id.apply_search_options_button);

        // Set click listener on clear button to clear all selected search options
        clearSearchOptionsButton.setOnClickListener(view -> {
            clearSearchOptions();
        });

        applySearchOptionsButton.setOnClickListener(view -> {
            applySearchOptions();
        });

        return advancedSearchOptionsModal;
    }

    @Override
    public void onStart() {
        super.onStart();

        // If modal was never initialized properly then just close it
        if (!setupModal) {
            this.dismiss();
        }

        // Bug fix to make dialog fragment open fully in landscape mode
        final BottomSheetBehavior<View> view = BottomSheetBehavior.from((View) requireView().getParent());
        view.setState(BottomSheetBehavior.STATE_EXPANDED);
        view.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);

        // Sets the max height of the dialog fragment to a percentage of the total screen height
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setMaxHeight((int) (displayMetrics.heightPixels * MODAL_PERCENT_HEIGHT_PARENT));
    }

    // Clears all currently selected search options in the modal
    private void clearSearchOptions() {
        // Get the four column radio groups for all the genre filter buttons
        final RadioGroup genreFilterButtonsColumn1 = requireView().findViewById(R.id.genre_filter_buttons_column_1);
        final RadioGroup genreFilterButtonsColumn2 = requireView().findViewById(R.id.genre_filter_buttons_column_2);
        final RadioGroup genreFilterButtonsColumn3 = requireView().findViewById(R.id.genre_filter_buttons_column_3);
        final RadioGroup genreFilterButtonsColumn4 = requireView().findViewById(R.id.genre_filter_buttons_column_4);

        // Clear any selected genre filter button
        genreFilterButtonsColumn1.clearCheck();
        genreFilterButtonsColumn2.clearCheck();
        genreFilterButtonsColumn3.clearCheck();
        genreFilterButtonsColumn4.clearCheck();

        // Make sure currently selected genre filter is set to null
        this.currentlySelectedGenreFilter = null;

        // Get the radio group containing all the sort option buttons
        final RadioGroup sortOptionButtons = requireView().findViewById(R.id.sort_option_radio_group);

        // Select the default sort option button
        final RadioButton defaultSortOptionButton = requireView().findViewById(R.id.relevance_ascending_button);
        defaultSortOptionButton.setChecked(true);

        // Make sure currently selected sort option is set to null
        this.currentlySelectedSortOption = null;
    }

    // Apply currently selected search options (if needed) and close this modal
    private void applySearchOptions() {
        final Activity activity = getActivity();

        // Get access to MainActivity
        if (activity instanceof MainActivity) {
            final MainActivity mainActivity = (MainActivity) activity;

            // Evaluate whether or not search options were modified when user presses apply button
            final boolean searchOptionsModified =
                    this.currentlySelectedGenreFilter != this.searchOptions.currentGenreFilterSelected()
                    || this.currentlySelectedSortOption != this.searchOptions.currentSortOptionSelected();

            // If the search options were modified, then set the newly selected genre filter and/or
            // sort option to the search options object held onto by the adapter
            if (searchOptionsModified) {
                this.searchOptions.setGenreFilter(this.currentlySelectedGenreFilter);
                this.searchOptions.setSortOption(this.currentlySelectedSortOption);

                // Notify the search page that new search search options were selected, so that
                // search results are re-fetched and re-populated on screen
                mainActivity.applyNewSearchOptions();
            }
        }

        this.dismiss();
    }

    private void setupGenreFilterButtonListeners(final @NonNull View advancedSearchOptionsModal) {
        // Get the four column radio groups for all the genre filter buttons
        final RadioGroup genreFilterButtonsColumn1 = advancedSearchOptionsModal.findViewById(R.id.genre_filter_buttons_column_1);
        final RadioGroup genreFilterButtonsColumn2 = advancedSearchOptionsModal.findViewById(R.id.genre_filter_buttons_column_2);
        final RadioGroup genreFilterButtonsColumn3 = advancedSearchOptionsModal.findViewById(R.id.genre_filter_buttons_column_3);
        final RadioGroup genreFilterButtonsColumn4 = advancedSearchOptionsModal.findViewById(R.id.genre_filter_buttons_column_4);

        // Define checked change listener that will be applied to all RadioGroup columns
        final RadioGroup.OnCheckedChangeListener checkedChangeListener = (radioGroup, checkedId) -> {
            // Genre filter button was unchecked from clearing RadioGroup so don't do anything
            if (checkedId == -1) {
                return;
            }

            // Get the RadioButton whose checked state was modified
            final RadioButton radioButton = radioGroup.findViewById(checkedId);

            // If radio button in this group was checked then update the currently selected genre
            // filter button to this new genre filter
            if (radioButton.isChecked()) {
                final Object genreName = radioButton.getTag();

                if (genreName instanceof String) {
                    this.currentlySelectedGenreFilter = SearchOptions.getGenreFilter((String) genreName);
                }

                // Clear checked buttons in all the other RadioGroup columns in order to only
                // keep one genre filter button selected at all times
                if (radioGroup.getId() != R.id.genre_filter_buttons_column_1) {
                    genreFilterButtonsColumn1.clearCheck();
                }
                if (radioGroup.getId() != R.id.genre_filter_buttons_column_2) {
                    genreFilterButtonsColumn2.clearCheck();
                }
                if (radioGroup.getId() != R.id.genre_filter_buttons_column_3) {
                    genreFilterButtonsColumn3.clearCheck();
                }
                if (radioGroup.getId() != R.id.genre_filter_buttons_column_4) {
                    genreFilterButtonsColumn4.clearCheck();
                }
            }
            // RadioButton was deselected
            else {
                // If deselected button was deselected directly without selecting another genre
                // filter button, then make the currentlySelectedGenreFilter equal to null since
                // no genre filter buttons are selected anymore
                final Object deselectedTag = radioButton.getTag();
                if (deselectedTag instanceof String &&
                        SearchOptions.getGenreFilter((String) deselectedTag) == currentlySelectedGenreFilter) {
                    this.currentlySelectedGenreFilter = null;
                }
            }
        };

        // Set checkedChange listener on each RadioGroup
        genreFilterButtonsColumn1.setOnCheckedChangeListener(checkedChangeListener);
        genreFilterButtonsColumn2.setOnCheckedChangeListener(checkedChangeListener);
        genreFilterButtonsColumn3.setOnCheckedChangeListener(checkedChangeListener);
        genreFilterButtonsColumn4.setOnCheckedChangeListener(checkedChangeListener);
    }

    private void setupSortOptionButtonListeners(final @NonNull View advancedSearchOptionsModal) {
        // Get the radio group containing all the sort option buttons
        final RadioGroup sortOptionButtons = advancedSearchOptionsModal.findViewById(R.id.sort_option_radio_group);

        // Apply checked change listener to the sort option buttons
        sortOptionButtons.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            // Sort option button was unchecked from clearing RadioGroup so don't do anything
            if (checkedId == -1) {
                return;
            }

            // Get the RadioButton whose checked state was modified
            final RadioButton radioButton = radioGroup.findViewById(checkedId);

            // If radio button in this group was checked then update the currently selected sort
            // option to the corresponding sort option of this button
            if (radioButton.isChecked()) {
                final Object sortOptionTag = radioButton.getTag();

                if (sortOptionTag instanceof String) {
                    this.currentlySelectedSortOption = SearchOptions.getSortOption((String) sortOptionTag);
                }
            }
        });
    }

    // Helper function to get RadioButton resource for a specific genre filter
    @IdRes
    private int getGenreFilterButtonRes(final @Nullable SearchOptions.GenreFilter genreFilter) {
        if (genreFilter == SearchOptions.GenreFilter.ACTION) {
            return R.id.action_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.ADVENTURE) {
            return R.id.adventure_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.ANIMATION) {
            return R.id.animation_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.COMEDY) {
            return R.id.comedy_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.CRIME) {
            return R.id.crime_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.DOCUMENTARY) {
            return R.id.documentary_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.DRAMA) {
            return R.id.drama_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.FAMILY) {
            return R.id.family_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.FANTASY) {
            return R.id.fantasy_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.HISTORY) {
            return R.id.history_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.HORROR) {
            return R.id.horror_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.MUSIC) {
            return R.id.music_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.MYSTERY) {
            return R.id.mystery_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.ROMANCE) {
            return R.id.romance_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.SCIENCE_FICTION) {
            return R.id.science_fiction_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.TV_Movie) {
            return R.id.tv_movie_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.THRILLER) {
            return R.id.thriller_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.WAR) {
            return R.id.war_filter_button;
        }
        else if (genreFilter == SearchOptions.GenreFilter.WESTERN) {
            return R.id.western_filter_button;
        }
        // No genre filters selected
        else {
            return EMPTY_GENRE_FILTER;
        }
    }

    // Helper function to get RadioButton resource for a specific sort option
    @IdRes
    private int getSortOptionButtonRes(final @Nullable SearchOptions.SortOption sortOption) {
        // If no sort option was previously selected, then just use the default
        // "relevance_ascending" sort option button
        if (sortOption == null) {
            return R.id.relevance_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RELEVANCE_DESCENDING) {
            return R.id.relevance_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.TITLE_ASCENDING) {
            return R.id.title_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.TITLE_DESCENDING) {
            return R.id.title_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RELEASE_DATE_ASCENDING) {
            return R.id.release_date_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RELEASE_DATE_DESCENDING) {
            return R.id.release_date_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RATING_ASCENDING) {
            return R.id.rating_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RATING_DESCENDING) {
            return R.id.rating_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.MPA_RATING_ASCENDING) {
            return R.id.mpa_rating_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.MPA_RATING_DESCENDING) {
            return R.id.mpa_rating_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.OVERVIEW_ASCENDING) {
            return R.id.overview_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.OVERVIEW_DESCENDING) {
            return R.id.overview_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RUNTIME_ASCENDING) {
            return R.id.runtime_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.RUNTIME_DESCENDING) {
            return R.id.runtime_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.GENRES_ASCENDING) {
            return R.id.genres_ascending_button;
        }
        else if (sortOption == SearchOptions.SortOption.GENRES_DESCENDING) {
            return R.id.genres_descending_button;
        }
        else if (sortOption == SearchOptions.SortOption.CONTENT_WARNING_ASCENDING) {
            return R.id.content_warnings_ascending_button;
        }
        else {
            return R.id.content_warnings_descending_button;
        }
    }
}
