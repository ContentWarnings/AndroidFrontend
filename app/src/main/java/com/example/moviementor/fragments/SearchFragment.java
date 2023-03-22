package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.SearchView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.adapters.SearchPageAdapter;
import com.example.moviementor.models.GenreViewModel;
import com.example.moviementor.other.ContentWarningPrefsStorage;
import com.example.moviementor.other.SearchOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchFragment extends BaseFragment implements SearchPageAdapter.OnItemClickListener {
    // Static array containing data for each genre's name
    private static final String[] GENRE_NAMES = {"Action", "Adventure", "Animation", "Comedy",
            "Crime", "Documentary", "Drama", "Family", "Fantasy", "History", "Horror", "Music",
            "Mystery", "Romance", "Science Fiction", "TV Movie", "Thriller", "War", "Western"};
    // TODO: Replace temp image id with ids for each custom genre background image
    // Static array containing resource id for each genre's custom background image
    private static final int[] GENRE_BACKGROUND_IMAGE_IDS = {R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background};

    // Search bar is always displayed as second item in RecyclerView
    private static final int SEARCH_BAR_POSITION = 1;

    // Holds onto the adapter for the search page's RecyclerView
    private @Nullable SearchPageAdapter searchPageAdapter;

    private final @NonNull List<Object> genreList;
    private boolean shouldJumpToSearchBar;

    public SearchFragment() {
        super(R.layout.search_fragment, Tab.SEARCH);
        this.searchPageAdapter = null;
        this.genreList = createGenreList();
        this.shouldJumpToSearchBar = false;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSearchPage();

        // User requested to open search bar, but search page/fragment is just now being
        // created and may not be ready yet, so attach layout listener that will open search bar
        // immediately once search fragment is rendered
        if (this.shouldJumpToSearchBar) {
            // Don't jump to search bar again unless another request is made ton
            this.shouldJumpToSearchBar = false;

            // Add listener that triggers once search fragment is attached to the view hierarchy
            final RecyclerView searchPageRecyclerView = requireView().findViewById(R.id.search_page_recycler_view);
            searchPageRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove this listener immediately to avoid multiple calls
                    searchPageRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Search fragment is rendered, so now try to open search bar
                    jumpToSearchBar();
                }
            });
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        // If page is being shown again, need to check if any content warning preferences have
        // changed since, so that the list of movie search results can be re-displayed and
        // re-filtered if needed
        if (!hidden && this.searchPageAdapter != null) {
            // Get all current content warning preferences stored for the user
            final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                    .getInstance(requireActivity());
            final Map<String, ContentWarningPrefsStorage.ContentWarningVisibility> cwPrefsMap = cwPrefsStorage
                    .getAllContentWarningPrefs();

            this.searchPageAdapter.checkContentWarningPrefsChanged(cwPrefsMap);
        }

        // Search page is being shown again, and user requested to go straight to search bar
        if (!hidden && this.shouldJumpToSearchBar) {
            jumpToSearchBar();
        }
    }

    private void setupSearchPage() {
        // Find progress bar view and no matching results text view and pass them to the adapter
        // so their visibility can be controlled
        final View progressWheelView = requireView().findViewById(R.id.loading_circle);
        final View noMatchingSearchResultsView = requireView().findViewById(R.id.no_matching_results_found);

        // Initialize RecyclerView and its adapter
        final RecyclerView searchPageRecyclerView = requireView().findViewById(R.id.search_page_recycler_view);

        // If search page adapter has not been setup for this fragment yet, then create and
        // initialize it
        if (this.searchPageAdapter == null) {
            // Get all current content warning preferences stored for the user
            final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                    .getInstance(requireActivity());
            final Map<String, ContentWarningPrefsStorage.ContentWarningVisibility> cwPrefsMap = cwPrefsStorage
                    .getAllContentWarningPrefs();

            this.searchPageAdapter = new SearchPageAdapter(this.genreList, cwPrefsMap);

            // Attach fragment as listener to the search page RecyclerView
            this.searchPageAdapter.setOnItemClickListener(this);

            // Give the adapter an alpha value to apply on genre background images
            final int alphaValue = getResources().getInteger(R.integer.genre_background_image_alpha);
            this.searchPageAdapter.assignAlphaValueForGenreBackgroundImages(alphaValue);
        }

        this.searchPageAdapter.assignUtilityViews(progressWheelView, noMatchingSearchResultsView);

        // Bind the adapter and a Linear Layout Manager to the RecyclerView
        searchPageRecyclerView.setAdapter(this.searchPageAdapter);
        searchPageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Add a scroll listener to RecyclerView that gets next page of search results if user
        // scrolls to bottom of search results currently displayed on the screen
        searchPageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final @NonNull RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                // Get RecyclerView's adapter and make sure it is an instance of the SearchPageAdapter
                final @Nullable RecyclerView.Adapter adapter = recyclerView.getAdapter();
                if (!(adapter instanceof SearchPageAdapter)) {
                    return;
                }

                final @NonNull SearchPageAdapter searchPageAdapter = (SearchPageAdapter) adapter;

                // Check that RecyclerView can not be scrolled down any further and if next page
                // of search results is available. If so, retrieve the next page of search results
                if (!recyclerView.canScrollVertically(1) && searchPageAdapter.shouldGetNextPage()) {
                    // Try to hide the load more results progress wheel at the bottom of the RecyclerView
                    final View loadingMoreWheel = requireView().findViewById(R.id.loading_more_circle);
                    if (loadingMoreWheel != null) {
                        loadingMoreWheel.setVisibility(View.VISIBLE);
                    }

                    searchPageAdapter.getNextPage();
                }
            }
        });
    }

    // Open search page/fragment immediately to the search bar having focus
    private void jumpToSearchBar() {
        final RecyclerView searchPageRecyclerView = requireView().findViewById(R.id.search_page_recycler_view);
        final LinearLayoutManager layoutManager = (LinearLayoutManager) searchPageRecyclerView.getLayoutManager();

        // RecyclerView is not fully set up yet, so wait until onViewCreated() to open the
        // search bar
        if (layoutManager == null) {
            return;
        }

        // Don't jump to search bar again unless another request is made to
        this.shouldJumpToSearchBar = false;

        // Get position of first and last fully visible item in the RecyclerView right now
        int firstFullyVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastFullyVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

        // SearchFragment is still in the process of being shown
        if (firstFullyVisiblePosition == RecyclerView.NO_POSITION && lastFullyVisiblePosition == RecyclerView.NO_POSITION) {
            // Add listener that triggers once search fragment is attached to the view hierarchy
            searchPageRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // Remove this listener immediately to avoid multiple calls
                    searchPageRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    // Search fragment is rendered, so now try to open search bar again
                    jumpToSearchBar();
                }
            });
        }
        // Search bar is being displayed fully in the RecyclerView right now
        else if (SEARCH_BAR_POSITION >= firstFullyVisiblePosition && SEARCH_BAR_POSITION <= lastFullyVisiblePosition) {
            int childCount = searchPageRecyclerView.getChildCount();

            // Iterate through all items currently being displayed in the RecyclerView until
            // search bar view is found (search bar should either be the first or second child
            // view, but iterate through all just in case)
            for (int i = 0; i < childCount; i++) {
                final View childView = searchPageRecyclerView.getChildAt(i);

                // If current child view is not the search bar row, then continue
                if (childView.getId() != R.id.search_bar_row) {
                    continue;
                }

                // Try to get search bar reference inside of the search row
                final View searchBarView = childView.findViewById(R.id.search_bar);

                // Once search bar is found, give it focus
                if (searchBarView instanceof SearchView) {
                    childView.requestFocus();
                    break;
                }
            }
        }
        // Search bar is not currently being displayed in the RecyclerView
        else {
            // Need to scroll to position of search bar before it can gain focus
            searchPageRecyclerView.smoothScrollToPosition(SEARCH_BAR_POSITION);
            // Attach listener to open search bar once scrolling stops
            searchPageRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                public void onScrollStateChanged(final @NonNull RecyclerView recyclerView, final int state) {
                    // Wait until scrolling stops
                    if (state == RecyclerView.SCROLL_STATE_IDLE) {
                        int childCount = recyclerView.getChildCount();

                        // Iterate through all items currently being displayed in the RecyclerView until
                        // search bar view is found (search bar should either be the first or second child
                        // view, but iterate through all just in case)
                        for (int i = 0; i < childCount; i++) {
                            final View childView = recyclerView.getChildAt(i);

                            // If current child view is not the search bar row, then continue
                            if (childView.getId() != R.id.search_bar_row) {
                                continue;
                            }

                            // Try to get search bar reference inside of the search row
                            final View searchBarView = childView.findViewById(R.id.search_bar);

                            // Once search bar is found, give it focus
                            if (searchBarView instanceof SearchView) {
                                childView.requestFocus();
                                break;
                            }
                        }

                        recyclerView.removeOnScrollListener(this);
                    }
                }
            });
        }
    }

    public void requestJumpToSearchBar() {
        this.shouldJumpToSearchBar = true;
    }

    @NonNull
    private List<Object> createGenreList() {
        final List<Object> genreData = new ArrayList<>();

        // Go through static arrays of genre data and populate viewModel list
        for (int i = 0; i < GENRE_NAMES.length; i++) {
            final @NonNull String genreName = GENRE_NAMES[i];
            final @IdRes int genreBackgroundImageRes = GENRE_BACKGROUND_IMAGE_IDS[i];

            final Object genreItem = new GenreViewModel(genreName, genreBackgroundImageRes);

            genreData.add(genreItem);
        }

        return genreData;
    }

    // Function called by the listener attached to the child RecyclerView's adapter. Only called by
    // listener when the filter button next to the search bar is clicked on
    @Override
    public void onFilterButtonClick(final @NonNull SearchOptions searchOptions) {
        // Route user's request to open advanced search options to the main activity
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.openAdvancedSearchOptionsModal(searchOptions);
    }

    // Function that tells search adapter that the search options were modified, so that new search
    // results can be retrieved and populated in the SearchFragment's RecyclerView
    public void applyNewSearchOptions() {
        final RecyclerView searchPageRecyclerView = requireView().findViewById(R.id.search_page_recycler_view);
        final RecyclerView.Adapter adapter = searchPageRecyclerView.getAdapter();

        if (adapter instanceof SearchPageAdapter) {
            // Notify SearchPageAdapter that the search options were modified
            final SearchPageAdapter searchPageAdapter = (SearchPageAdapter) adapter;
            searchPageAdapter.onSearchOptionsChange();
        }
    }

    @Override
    public void onSearchResultClick(final int movieId, final @NonNull String movieName) {
        final MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.openMoviePage(movieId, movieName);
    }
}
