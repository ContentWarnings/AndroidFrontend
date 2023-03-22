package com.example.moviementor.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviementor.R;
import com.example.moviementor.models.GenreViewModel;
import com.example.moviementor.models.SearchResultMovieViewModel;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.ContentWarningPrefsStorage.ContentWarningVisibility;
import com.example.moviementor.other.SearchOptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SearchPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_SEARCH_BAR = 2;
    private static final int VIEW_TYPE_GENRE = 3;
    private static final int VIEW_TYPE_SEARCH_RESULT = 4;
    private static final int VIEW_TYPE_LOAD_MORE = 5;

    private final @NonNull List<Object> searchPageItems;
    private final @NonNull List<Object> genreItems;

    private @NonNull Map<String, ContentWarningVisibility> cwPrefsMap;

    private @NonNull String searchString;

    private @NonNull View progressWheelView;
    private @NonNull View noMatchingResultsText;

    private @Nullable OnItemClickListener listener;

    // Defines how transparent background images for genre rows should be
    private int genreBackgroundAlphaValue;

    // Specifies the last page loaded for the current search string and whether or not
    // more search results are available to be queried
    private int lastSearchPage;
    private boolean moreSearchResultsAvailable;
    private boolean fetchingNextPage;

    // Stores and manages the current filter/sort options the user has selected for searching
    private final @NonNull SearchOptions searchOptions;

    public SearchPageAdapter(final @NonNull List<Object> genreItems,
                             final @NonNull Map<String, ContentWarningVisibility> cwPrefsMap) {
        this.searchPageItems = new ArrayList<>();
        this.searchPageItems.addAll(genreItems);

        this.genreItems = genreItems;
        this.genreBackgroundAlphaValue = 0;

        this.cwPrefsMap = cwPrefsMap;

        this.searchString = "";
        this.searchOptions = new SearchOptions();

        this.listener = null;

        this.lastSearchPage = -1;
        this.moreSearchResultsAvailable = false;
        this.fetchingNextPage = false;
    }

    public void assignUtilityViews(final @NonNull View progressWheelView,
                                   final @NonNull View noMatchingResultsText) {
        this.progressWheelView = progressWheelView;
        this.noMatchingResultsText = noMatchingResultsText;
    }

    public void assignAlphaValueForGenreBackgroundImages(final int alphaValue) {
        if (alphaValue < 0 || alphaValue > 255) {
            return;
        }
        this.genreBackgroundAlphaValue = alphaValue;
    }

    private void onSearchStringChange(final @NonNull String newSearchString) {
        // Don't modify search page at all if search string was not modified
        if (newSearchString.equals(this.searchString)) {
            return;
        }
        // If search string was reset to empty and no genre filter is currently selected, then
        // restore list of genres on page since there is nothing to search for
        else if (newSearchString.isEmpty() && this.searchOptions.currentGenreFilterSelected() == null) {
            this.searchString = newSearchString;

            // Make sure progress wheel and no matching results text are not visible anymore
            this.progressWheelView.setVisibility(View.GONE);
            this.noMatchingResultsText.setVisibility(View.GONE);

            // Since search string was modified, previous requests to get new pages are voided
            this.fetchingNextPage = false;

            // Replace all current search results with genre data again to re-populate
            // screen with list of genres
            final int previousLen = this.searchPageItems.size();
            this.searchPageItems.clear();
            notifyItemRangeRemoved(2, previousLen);
            this.searchPageItems.addAll(this.genreItems);
            notifyItemRangeInserted(2, this.searchPageItems.size());

            return;
        }

        this.searchString = newSearchString;

        // Can't load more search results until initial page of search results is populated
        this.moreSearchResultsAvailable = false;

        // Since search string was modified, previous requests to get new pages are voided
        this.fetchingNextPage = false;

        // Clear all items currently displayed on the search page
        final int previousLen = this.searchPageItems.size();
        this.searchPageItems.clear();
        notifyItemRangeRemoved(2, previousLen);

        // Make sure no matching search results text is not visible
        this.noMatchingResultsText.setVisibility(View.GONE);

        // May take a second to populate search results, so display loading wheel until the
        // results are populated
        this.progressWheelView.setVisibility(View.VISIBLE);

        // Fetch search results for the new search string from the database. Pass a copy of the
        // current search options so that it is not affected by changes to the adapter's search
        // options
        Backend.fetchSearchResults(this, this.searchString, this.searchOptions.copy());
    }

    public void onSearchOptionsChange() {
        // Notify search row to update since search options have changed and the filter button
        // may or may not need to toggle its active/inactive UI state
        notifyItemChanged(1);

        // If search string was already empty and the genre filter was cleared or previously empty,
        // then restore list of genres on page since there is nothing to search for
        if (this.searchString.isEmpty() && this.searchOptions.currentGenreFilterSelected() == null) {
            // Make sure progress wheel and no matching results text are not visible anymore
            this.progressWheelView.setVisibility(View.GONE);
            this.noMatchingResultsText.setVisibility(View.GONE);

            // Since search options were modified, previous requests to get new pages are voided
            this.fetchingNextPage = false;

            // Replace all current search results with genre data again to re-populate
            // screen with list of genres
            final int previousLen = this.searchPageItems.size();
            this.searchPageItems.clear();
            notifyItemRangeRemoved(2, previousLen);
            this.searchPageItems.addAll(this.genreItems);
            notifyItemRangeInserted(2, this.searchPageItems.size());

            return;
        }

        // Can't load more search results until initial page of search results is populated
        this.moreSearchResultsAvailable = false;

        // Since search options were modified, previous requests to get new pages are voided
        this.fetchingNextPage = false;

        // Clear all items currently displayed on the search page
        final int previousLen = this.searchPageItems.size();
        this.searchPageItems.clear();
        notifyItemRangeRemoved(2, previousLen);

        // Make sure no matching search results text is not visible
        this.noMatchingResultsText.setVisibility(View.GONE);

        // May take a second to populate search results, so display loading wheel until the
        // results are populated
        this.progressWheelView.setVisibility(View.VISIBLE);

        // Fetch search results for the new search options from the database. Pass a copy of the
        // current search options so that it is not affected by changes to the adapter's search
        // options
        Backend.fetchSearchResults(this, this.searchString, this.searchOptions.copy());
    }

    public void getNextPage() {
        // Don't try to get any more pages until this next one has populated
        this.moreSearchResultsAvailable = false;

        // Make RecyclerView aware that fetching next page is in progress
        this.fetchingNextPage = true;

        // Fetch next page of search results from the database. Pass a copy of the current search
        // options so that it is not affected by changes to the adapter's search options
        Backend.fetchSearchResultsPage(this, this.searchString, this.searchOptions.copy(), this.lastSearchPage + 1);
    }

    public void setSearchResults(final @NonNull List<Object> searchResults,
                                 final @NonNull String searchString,
                                 final @NonNull SearchOptions searchOptions) {
        // If search results are stale since search string or search options were recently updated,
        // then ignore this late list of search result data coming in
        if (!this.searchString.equals(searchString) || !this.searchOptions.equals(searchOptions)) {
            return;
        }
        // If no search results for this string were found, make it clear that no more
        // pages can be loaded and hide the progress wheel
        else if (searchResults.isEmpty()) {
            this.moreSearchResultsAvailable = false;
            this.progressWheelView.setVisibility(View.GONE);

            // Make text in middle of screen visible to let user know that no matching
            // results were found
            this.noMatchingResultsText.setVisibility(View.VISIBLE);

            return;
        }

        // Fill search page items with search results that were queried from the database
        this.searchPageItems.addAll(searchResults);

        // Add null at end of item list as a placeholder for the load more results view
        this.searchPageItems.add(null);

        // About to display search result data, so hide progress wheel again
        this.progressWheelView.setVisibility(View.GONE);

        // Just got first page of search results for current search string and search options
        this.lastSearchPage = 1;

        // If search string is blank and no genre filter is selected, then currently showing
        // trending movies which does not allow for pagination. In this case, make it clear that
        // no more pages of search results can be retrieved
        if (this.searchString.trim().isEmpty() && this.searchOptions.currentGenreFilterSelected() == null) {
            this.moreSearchResultsAvailable = false;
        }
        // Otherwise, the first page of search results are being populated, so you can get the next
        // page of search results now
        else {
            this.moreSearchResultsAvailable = true;
        }

        // Notify how many new search results need to be populated on the page. Header and search
        // bar should never be updated so notify adapter of insertions starting at position 2
        // instead of 0.
        notifyItemRangeInserted(2, this.searchPageItems.size());
    }

    // Should only get next page of search results if search is not empty (this means that
    // genre rows are not being shown and trending movies are not being shown since pagination is
    // not supported for trending movies), there are search results already populated on the screen,
    // and if there are more search results available. Search is not empty if search string is not
    // blank and/or a genre filter is selected
    public boolean shouldGetNextPage() {
        return  (!this.searchString.trim().isEmpty() ||
                  this.searchOptions.currentGenreFilterSelected() != null)
                && !this.searchPageItems.isEmpty()
                && moreSearchResultsAvailable;
    }

    public void populateNextPage(final @NonNull List<Object> searchResults,
                                 final @NonNull String searchString,
                                 final @NonNull SearchOptions searchOptions) {
        // If search string or search options were modified while retrieving next page, then ignore
        // the outdated page results
        if (!this.searchString.equals(searchString) || !this.searchOptions.equals(searchOptions)) {
            return;
        }
        // If no search results were found in the next page, return since no more
        // pages can be loaded
        else if (searchResults.isEmpty()) {
            // Just finished attempting to fetch next page results
            this.fetchingNextPage = false;

            // Need to refresh the load more results view so that the loading wheel is hidden
            // and the "No Results Available" text is displayed instead. This view is at the end
            // of the item list, so notify that the item at the last position in the RecyclerView
            // was changed. This is at the last item list position offset by 2 since the header
            // and search bar are the first two items displayed and minus 1 to get 0-based index.
            notifyItemChanged(this.searchPageItems.size() + 2 - 1);
            return;
        }

        // Remove the null object at the end of the current list since it will need to be added
        // back to the end of the list again after the new search results are appended
        this.searchPageItems.remove(this.searchPageItems.size() - 1);
        notifyItemRemoved(this.searchPageItems.size() + 2);

        // Append all the new page's search results to the end of the item list
        this.searchPageItems.addAll(searchResults);

        // Add null at end of item list as a placeholder for the load more results view
        this.searchPageItems.add(null);

        // Current page results were received, so you can get the next page of search results now
        this.lastSearchPage++;
        this.moreSearchResultsAvailable = true;
        this.fetchingNextPage = false;

        // Notify how many new search results were appended to the end of the item list. Add 1 to
        // all the page search results since null was re-appended to the end of the item list
        notifyItemRangeInserted(this.searchPageItems.size() + 2 - (searchResults.size() + 1),
                searchResults.size() + 1);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            // Inflate view from header layout file
            final View headerView = inflater.inflate(R.layout.header, parent, false);

            // Since we are displaying this header on the search page, hide the header's search
            // button and disable its functionality
            final ImageButton headerSearchButton = headerView.findViewById(R.id.header_search_button);
            headerSearchButton.setEnabled(false);
            headerSearchButton.setVisibility(View.GONE);

            // Get the current layout parameters from the parent RecyclerView
            final RelativeLayout.LayoutParams rvParams = (RelativeLayout.LayoutParams) parent.
                    findViewById(R.id.search_page_recycler_view).getLayoutParams();

            // Get the left and right margin from the RecyclerView's layout parameters
            final int leftMargin = rvParams.leftMargin;
            final int rightMargin = rvParams.rightMargin;

            // Get the current layout parameters for the header
            final RecyclerView.LayoutParams headerParams = (RecyclerView.LayoutParams) headerView.getLayoutParams();

            // Set the header's left and right margins equal to the negative left and right margins
            // of the parent RecyclerView. This effectively cancels out any horizontal margins on
            // the header view
            headerParams.setMargins(-leftMargin, 0, -rightMargin, 0);
            headerView.setLayoutParams(headerParams);

            // Return new view holder for inflated header
            return new HeaderViewHolder(headerView);
        }
        else if (viewType == VIEW_TYPE_SEARCH_BAR) {
            // Inflate view from search bar layout file
            final View searchRowView = inflater.inflate(R.layout.search_bar, parent, false);

            // Get actual search bar inside search row
            final SearchView searchBar = searchRowView.findViewById(R.id.search_bar);

            // Set a listener to open and close keyboard when search bar gains and loses focus
            searchBar.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                final InputMethodManager inputMethodManager = (InputMethodManager) parent.getContext()
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);

                if (!hasFocus) {
                    inputMethodManager.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                }
                else {
                    // Newer Android versions only worked with "showSoftInput" whereas older Android
                    // versions only worked with "toggleSoftInput", so just try the first approach
                    // and if it fails to open the keyboard, then try the second one
                    if (!inputMethodManager.showSoftInput(searchBar, InputMethodManager.SHOW_IMPLICIT)) {
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    }
                }
            });

            // Return new view holder for inflated search bar
            return new SearchPageAdapter.SearchBarViewHolder(searchRowView);
        }
        else if (viewType == VIEW_TYPE_GENRE) {
            // Inflate custom layout for singular genre
            final View genreItemView = inflater.inflate(R.layout.genre_row, parent, false);

            // Return new view holder for inflated genre item
            return new SearchPageAdapter.GenreViewHolder(genreItemView);
        }
        else if (viewType == VIEW_TYPE_SEARCH_RESULT) {
            // Inflate custom layout for search result item
            final View movieSearchResultView = inflater.inflate(R.layout.search_result_row, parent, false);

            // Return new view holder for inflated move search result
            return new SearchPageAdapter.SearchResultViewHolder(movieSearchResultView);
        }
        else {
            // Inflate custom layout for load more results view that is at the end of the search results list
            final View loadingMoreView = inflater.inflate(R.layout.load_more_row, parent, false);

            // Return new view holder for inflated load more results view
            return new SearchPageAdapter.LoadMoreViewHolder(loadingMoreView);
        }
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final int itemViewType = this.getItemViewType(position);

        if (itemViewType == VIEW_TYPE_HEADER) {
            final SearchPageAdapter.HeaderViewHolder headerViewHolder =
                    (SearchPageAdapter.HeaderViewHolder) viewHolder;

            // Bind this page's header title to the header view generated by the RecyclerView
            headerViewHolder.headerTitle.setText(R.string.search_page_header_title);
        }
        else if (itemViewType == VIEW_TYPE_SEARCH_BAR) {
            final View searchRowView = viewHolder.itemView;

            // Get actual search bar inside search row
            final SearchView searchBar = searchRowView.findViewById(R.id.search_bar);

            // Restore the search bar's search string when it is scrolled back onto the screen
            searchBar.setQuery(this.searchString, false);

            // Set listener on search bar that is triggered anytime something is typed into
            // the search bar
            searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                // Don't do anything when user presses submit since search results should already
                // be populated
                @Override
                public boolean onQueryTextSubmit(final @NonNull String s) {
                    return false;
                }

                // Whenever text in the search bar is modified, let the adapter handle the
                // modified search string
                @Override
                public boolean onQueryTextChange(final @NonNull String str) {
                    onSearchStringChange(str);
                    return false;
                }
            });

            final ImageButton filterButton = ((SearchBarViewHolder) viewHolder).filterButton;
            final Drawable filterImg = ResourcesCompat.getDrawable(filterButton.getResources(),
                        R.drawable.filter_icon, null);

            // If any search option is currently set by the user then give the filter button
            // an active tint
            if (this.searchOptions.currentGenreFilterSelected() != null
                    || this.searchOptions.currentSortOptionSelected() != null) {
                filterImg.setTint(ResourcesCompat
                        .getColor(filterButton.getResources(), R.color.filter_button_active, null));
            }
            // Otherwise, no filters selected so give the filter button an inactive tint
            else {
                filterImg.setTint(ResourcesCompat
                        .getColor(filterButton.getResources(), R.color.filter_button_inactive, null));
            }

            filterButton.setImageDrawable(filterImg);
        }
        else if (itemViewType == VIEW_TYPE_GENRE) {
            final SearchPageAdapter.GenreViewHolder genreViewHolder = (SearchPageAdapter.GenreViewHolder) viewHolder;

            // Get the current genre at specified position in list. Offset position by -2
            // since header and search bar are at position 0 and 1 in the RecyclerView
            final @NonNull GenreViewModel genreData = (GenreViewModel) this.searchPageItems.get(position - 2);

            // Bind this genre's name and background image to the genre item view generated by the RecyclerView
            genreViewHolder.genreName.setText(genreData.getGenreName());
            genreViewHolder.genreItemView.setBackgroundResource(genreData.getGenreBackgroundImageRes());

            // Make this background image partly transparent
            genreViewHolder.genreItemView.getBackground().setAlpha(genreBackgroundAlphaValue);

            // Setup a click listener on genre item view that detects when genre row/button has been
            // clicked on, so that the selected genre's results can be displayed
            genreViewHolder.itemView.setOnClickListener(view -> {
                // Get text being displayed on the genre row that was clicked on
                final TextView genreRowTextClicked = view.findViewById(R.id.genre_name);
                final String nameOfGenreRowClicked = (String) genreRowTextClicked.getText();

                // Set new genre filter that was selected
                final SearchOptions.GenreFilter selectedGenreFilter = SearchOptions
                        .getGenreFilter(nameOfGenreRowClicked);
                this.searchOptions.setGenreFilter(selectedGenreFilter);

                // Adapter should now get results for the selected genre filter
                onSearchOptionsChange();
            });
        }
        else if (itemViewType == VIEW_TYPE_SEARCH_RESULT) {
            final SearchPageAdapter.SearchResultViewHolder searchResultViewHolder =
                    (SearchPageAdapter.SearchResultViewHolder) viewHolder;

            // Restore this viewHolder to visible in case it was previously hidden
            searchResultViewHolder.itemView.setVisibility(View.VISIBLE);
            searchResultViewHolder.itemView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // Get the current movie search result at specified position in list. Offset position
            // by -2 since header and search bar are at position 0 and 1 in the RecyclerView
            final @NonNull SearchResultMovieViewModel searchResultData = (SearchResultMovieViewModel) this.searchPageItems.get(position - 2);

            // If movie contains a content warning that user has flagged as HIDE, then simply
            // hide this movie's view on the screen
            if (shouldBeHidden(searchResultData.getContentWarnings())) {
                searchResultViewHolder.itemView.setVisibility(View.GONE);
                searchResultViewHolder.itemView.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

                // If this is the last item in current search results and is being hidden, and
                // there are more search results available to fetch, then start the process of
                // fetching the next page
                // (last search result item is the second to last item in the list since
                // the last item is always a null object representing the load more row)
                if (position == (getItemCount() - 2) && shouldGetNextPage()) {
                    getNextPage();
                }

                return;
            }

           final @Nullable URL searchResultMovieImageUrl = searchResultData.getMovieImageUrl();

           // If movie's image url is null (not available), then load the no image found placeholder
           // into this row's movie image
           if (searchResultMovieImageUrl == null) {
               Glide.with(searchResultViewHolder.searchResultMovieImage.getContext())
                       .load(R.drawable.no_image_placeholder)
                       .into(searchResultViewHolder.searchResultMovieImage);
           }
           // Load movie's image from non-null url, but if error is encountered then just use the no
           // image found placeholder
           else {
               Glide.with(searchResultViewHolder.searchResultMovieImage.getContext())
                       .load(searchResultMovieImageUrl.toString())
                       .error(R.drawable.no_image_placeholder)
                       .into(searchResultViewHolder.searchResultMovieImage);
           }

           // Only display warning triangle icon for this search result if this movie contains
           // at least one content warning that the user has set as WARN
           if (shouldBeWarned(searchResultData.getContentWarnings())) {
               searchResultViewHolder.searchResultWarningIcon.setVisibility(View.VISIBLE);
           }
           else {
               searchResultViewHolder.searchResultWarningIcon.setVisibility(View.GONE);
           }

           searchResultViewHolder.searchResultMovieTitle.setText(searchResultData.getMovieName());

           // Get list of genre strings
           final @NonNull List<String> genresList = searchResultData.getGenres();

           // Get genre tile recycler view for current view holder and its associated adapter.
           // If valid GenreTilesAdapter was found, then populate the adapter's genresList
           // with new list of genre strings
           final RecyclerView.Adapter genreTilesAdapter = searchResultViewHolder.searchResultGenresRecyclerView.getAdapter();
           if (genreTilesAdapter instanceof GenreTilesAdapter) {
               ((GenreTilesAdapter) genreTilesAdapter).setGenreList(genresList);
           }

           final @Nullable Date releaseDate = searchResultData.getReleaseDate();
           final @NonNull String movieOverview = searchResultData.getMovieOverview();

           String movieYearAndDescription;
            // If no release date available, then don't include it
           if (releaseDate == null) {
               movieYearAndDescription = movieOverview;
           }
           // Otherwise just include the release date year
           else {
               // Date.getYear() is deprecated so convert to calendar instance and
               // get year from this instance
               final Calendar calendar = Calendar.getInstance();
               calendar.setTime(releaseDate);
               movieYearAndDescription = calendar.get(Calendar.YEAR) + " - " + movieOverview;
           }

           searchResultViewHolder.searchResultYearAndDescription.setText(movieYearAndDescription);

            // Get unique list of content warnings for this movie organized by priority
            // (WARN content warnings before SHOW content warnings)
            final @NonNull List<String> contentWarningList =
                    getUniqueContentWarningListSortedByPriority(searchResultData.getContentWarnings());

           // Get content warning tile recycler view for current view holder and its associated
           // adapter. If valid ContentWarningTilesAdapter was found, then populate the adapter's
           // contentWarningList with new list of content warning strings and update the adapter's
           // content warning preferences
           final RecyclerView.Adapter contentWarningTilesAdapter = searchResultViewHolder
                   .searchResultContentWarningsRecyclerView.getAdapter();
           if (contentWarningTilesAdapter instanceof ContentWarningTilesAdapter) {
               ((ContentWarningTilesAdapter) contentWarningTilesAdapter)
                       .setContentWarningListAndPrefs(contentWarningList, this.cwPrefsMap);
           }

           final int movieId = searchResultData.getMovieId();
           final @NonNull String movieName = searchResultData.getMovieName();

            // Setup click listener to open this movie's full page if clicked on
            searchResultViewHolder.itemView.setOnClickListener(view -> {
                if (this.listener != null) {
                    listener.onSearchResultClick(movieId, movieName);
                }
            });
        }
        else if (itemViewType == VIEW_TYPE_LOAD_MORE) {
            final SearchPageAdapter.LoadMoreViewHolder loadMoreViewHolder =
                    (SearchPageAdapter.LoadMoreViewHolder) viewHolder;

            // If there are no more search results available for the current search string and not currently
            // waiting for another page's results, then set the "No Results Available" text to visible
            // at the bottom of the RecyclerView
            if (!this.moreSearchResultsAvailable && !this.fetchingNextPage) {
                loadMoreViewHolder.noMoreResultsText.setVisibility(View.VISIBLE);
            }
            // Otherwise, hide the "No Results Available" text
            else {
                loadMoreViewHolder.noMoreResultsText.setVisibility(View.GONE);
            }

            // Make sure that loading wheel at the bottom of the RecyclerView is hidden since
            // the next page of search results has not been requested yet
            loadMoreViewHolder.loadMoreWheel.setVisibility(View.GONE);
        }
    }

    // Whenever this page is re-opened, this function is called to check if any content warning
    // preferences have changed, since the list of movie search results being displayed may need to
    // be re-filtered and/or re-displayed to adjust to these changes
    public void checkContentWarningPrefsChanged(Map<String, ContentWarningVisibility> newCwPrefsMap) {
        // If content warning preferences have changed, need to refresh list being displayed so
        // that search results can be hidden or shown again if needed and so that content warning
        // highlights can be added or removed if needed
        if (!this.cwPrefsMap.equals(newCwPrefsMap)) {
            this.cwPrefsMap = newCwPrefsMap;
            notifyDataSetChanged();
        }
    }

    // Helper function that decides whether or not movie should be shown based on the user's
    // content warning settings
    private boolean shouldBeHidden(final @NonNull List<String> movieContentWarnings) {
        for (final @NonNull String contentWarning : movieContentWarnings) {
            if (this.cwPrefsMap.getOrDefault(contentWarning, ContentWarningVisibility.SHOW) == ContentWarningVisibility.HIDE) {
                return true;
            }
        }
        return false;
    }

    // Helper function that decides whether or not movie has any content warnings that need to be
    // warned based on the user's content warning settings
    private boolean shouldBeWarned(final @NonNull List<String> movieContentWarnings) {
        for (final @NonNull String contentWarning : movieContentWarnings) {
            if (this.cwPrefsMap.getOrDefault(contentWarning, ContentWarningVisibility.SHOW) == ContentWarningVisibility.WARN) {
                return true;
            }
        }
        return false;
    }

    // Helper function that re-organizes a movie's content warning list such that all content
    // warnings that user has set to WARN appear first and removes any duplicates
    private List<String> getUniqueContentWarningListSortedByPriority(final @NonNull List<String> contentWarnings) {
        final List<String> contentWarningsWarn = new ArrayList<>();
        final List<String> contentWarningsShow = new ArrayList<>();

        final Set<String> seenContentWarnings = new HashSet<>();

        // Split movie's list of content warnings into two lists separated by whether or not their
        // visibility status is SHOW or WARN (Should never have CW with hide since the movie would
        // have been filtered out)
        for (final @NonNull String contentWarning : contentWarnings) {
            // Don't add duplicate content warning to the final list
            if (seenContentWarnings.contains(contentWarning)) {
                continue;
            }
            seenContentWarnings.add(contentWarning);

            if (this.cwPrefsMap.getOrDefault(contentWarning, ContentWarningVisibility.SHOW) == ContentWarningVisibility.WARN) {
                contentWarningsWarn.add(contentWarning);
            }
            else {
                contentWarningsShow.add(contentWarning);
            }
        }

        // Append list of content warnings with SHOW visibility to list of content warnings
        // with WARN visibility
        contentWarningsWarn.addAll(contentWarningsShow);

        return contentWarningsWarn;
    }

    // Returns the total number of genres or search results, plus 2 for the header and search bar
    @Override
    public int getItemCount() {
        return this.searchPageItems.size() + 2;
    }

    // Returns whether view is of type header, search bar, genre item, or search result item
    @Override
    public int getItemViewType(final int position) {
        // First view in search page is the header
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        // Second view in search page is the search bar
        else if (position == 1) {
            return VIEW_TYPE_SEARCH_BAR;
        }
        // Null object in item list is placeholder for load more results view at bottom of RecyclerView
        else if (searchPageItems.get(position - 2) == null) {
            return VIEW_TYPE_LOAD_MORE;
        }
        // All other views are either genre or search result items based on whatever data is
        // currently being stored by the adapter
        else {
            final @NonNull Object searchPageItem = this.searchPageItems.get(position - 2);
            return (searchPageItem instanceof GenreViewModel) ? VIEW_TYPE_GENRE : VIEW_TYPE_SEARCH_RESULT;
        }
    }

    // Assign listener from parent fragment to this adapter
    public void setOnItemClickListener(final @NonNull OnItemClickListener listener) {
        this.listener = listener;
    }

    // Define listener interface for parent fragment to use if it wants to listen to an event
    // in the RecyclerView. Less prone to memory leaks in comparison to holding onto a reference
    // to the parent fragment in this adapter
    public interface OnItemClickListener {
        void onFilterButtonClick(final @NonNull SearchOptions searchOptions);
        void onSearchResultClick(final int movieId, final @NonNull String movieName);
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView headerTitle;

        public HeaderViewHolder(final @NonNull View headerView) {
            super(headerView);
            this.headerTitle = headerView.findViewById(R.id.header_title);
        }
    }

    public class SearchBarViewHolder extends RecyclerView.ViewHolder {
        public final ImageButton filterButton;

        public SearchBarViewHolder(final @NonNull View searchBarView) {
            super(searchBarView);
            this.filterButton = searchBarView.findViewById(R.id.filter_button);

            // Set up click listener on the search filter button to open advanced search options
            // modal
            this.filterButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFilterButtonClick(searchOptions);
                }
            });
        }
    }

    public static class GenreViewHolder extends RecyclerView.ViewHolder {
        public final TextView genreName;
        public final View genreItemView;

        public GenreViewHolder(final @NonNull View genreItemView) {
            super(genreItemView);
            this.genreName = genreItemView.findViewById(R.id.genre_name);
            this.genreItemView = genreItemView;
        }
    }

    public static class SearchResultViewHolder extends RecyclerView.ViewHolder {
        public final ImageView searchResultMovieImage;
        public final ImageView searchResultWarningIcon;
        public final TextView searchResultMovieTitle;
        public final RecyclerView searchResultGenresRecyclerView;
        public final TextView searchResultYearAndDescription;
        public final RecyclerView searchResultContentWarningsRecyclerView;

        public SearchResultViewHolder(final @NonNull View searchResultView) {
            super(searchResultView);
            this.searchResultMovieImage = searchResultView.findViewById(R.id.search_result_movie_image);
            this.searchResultWarningIcon = searchResultView.findViewById(R.id.search_result_warning_icon);
            this.searchResultMovieTitle = searchResultView.findViewById(R.id.search_result_movie_title);

            // Set up empty RecyclerView for genre tiles
            this.searchResultGenresRecyclerView = searchResultView.findViewById(R.id.genre_tiles_recycler_view);
            this.searchResultGenresRecyclerView.setAdapter(new GenreTilesAdapter(new ArrayList<>()));
            this.searchResultGenresRecyclerView.setLayoutManager(new LinearLayoutManager(this.searchResultGenresRecyclerView
                    .getContext(), RecyclerView.HORIZONTAL, false));

            this.searchResultYearAndDescription = searchResultView.findViewById(R.id.search_result_year_and_description);

            // Set up empty RecyclerView for content warning tiles
            this.searchResultContentWarningsRecyclerView = searchResultView.
                    findViewById(R.id.content_warning_tiles_recycler_view);
            this.searchResultContentWarningsRecyclerView.setAdapter(new ContentWarningTilesAdapter(new ArrayList<>()));
            this.searchResultContentWarningsRecyclerView.setLayoutManager(new LinearLayoutManager(this.searchResultContentWarningsRecyclerView
                    .getContext(), RecyclerView.HORIZONTAL, false));
        }
    }

    public static class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        public final ProgressBar loadMoreWheel;
        public final TextView noMoreResultsText;

        public LoadMoreViewHolder(final @NonNull View loadMoreView) {
            super(loadMoreView);
            this.loadMoreWheel = loadMoreView.findViewById(R.id.loading_more_circle);
            this.noMoreResultsText = loadMoreView.findViewById(R.id.no_more_results);
        }
    }
}
