package com.example.moviementor.other;

import android.os.Bundle;
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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AdvancedSearchOptionsModal extends BottomSheetDialogFragment {
    private static final int EMPTY_GENRE_FILTER = -1;

    // Object that keeps track of selected search options for user
    private @NonNull SearchOptions searchOptions;

    // Keeps track of user's currently selected genre filter in the modal
    private @Nullable SearchOptions.GenreFilter currentlySelectedGenreFilter;

    private boolean setupModal;

    // Default Constructor
    public AdvancedSearchOptionsModal() {
        super();
    }

    public void setSearchOptions(final @NonNull SearchOptions searchOptions) {
        this.searchOptions = searchOptions;
        this.currentlySelectedGenreFilter = null;
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

        // If modal was opened with previous genre filter selected, then find the associated button
        // for that genre filter and change it's state to selected
        final @IdRes int genreFilterSelectedButtonRes = getGenreFilterButtonRes(this.searchOptions.currentGenreFilterSelected());
        if (genreFilterSelectedButtonRes != EMPTY_GENRE_FILTER) {
            final RadioButton genreFilterButton = advancedSearchOptionsModal.findViewById(genreFilterSelectedButtonRes);
            genreFilterButton.setChecked(true);
        }

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
    }

    // Apply currently selected search options and close this modal
    private void applySearchOptions() {
        searchOptions.setGenreFilter(currentlySelectedGenreFilter);
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
}
