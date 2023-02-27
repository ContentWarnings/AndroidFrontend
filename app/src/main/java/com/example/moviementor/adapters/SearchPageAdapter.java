package com.example.moviementor.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviementor.R;
import com.example.moviementor.models.GenreViewModel;
import com.example.moviementor.models.SearchResultMovieViewModel;
import com.example.moviementor.other.Backend;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SearchPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_SEARCH_BAR = 2;
    private static final int VIEW_TYPE_GENRE = 3;
    private static final int VIEW_TYPE_SEARCH_RESULT = 4;
    private static final int VIEW_TYPE_LOAD_MORE = 5;

    private final @NonNull List<Object> searchPageItems;
    private final @NonNull List<Object> genreItems;

    private @NonNull String searchString;

    private final @NonNull View progressWheelView;

    // Defines how transparent background images for genre rows should be
    private int genreBackgroundAlphaValue;

    // Specifies the last page loaded for the current search string and whether or not
    // more search results are available to be queried
    private int lastSearchPage;
    private boolean moreSearchResultsAvailable;

    public SearchPageAdapter(final @NonNull List<Object> genreItems, final @NonNull View progressWheelView) {
        this.searchPageItems = new ArrayList<>();
        this.searchPageItems.addAll(genreItems);

        this.genreItems = genreItems;
        this.genreBackgroundAlphaValue = 0;

        this.searchString = "";

        this.progressWheelView = progressWheelView;

        this.lastSearchPage = -1;
        this.moreSearchResultsAvailable = false;
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
        // If search string was reset to empty, restore list of genres on page
        // since there is nothing to search for
        else if (newSearchString.isEmpty()) {
            this.searchString = newSearchString;

            // Make sure progress wheel is not visible anymore
            this.progressWheelView.setVisibility(View.GONE);

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

        // Clear all items currently displayed on the search page
        final int previousLen = this.searchPageItems.size();
        this.searchPageItems.clear();
        notifyItemRangeRemoved(2, previousLen);

        // May take a second to populate search results, so display loading wheel until the
        // results are populated
        this.progressWheelView.setVisibility(View.VISIBLE);

        // Fetch search results for the new search string from the database
        Backend.fetchSearchResults(this, this.searchString);
    }

    public void getNextPage() {
        // Don't try to get any more pages until this next one has populated
        this.moreSearchResultsAvailable = false;

        // Fetch next page of search results from the database
        Backend.fetchSearchResultsPage(this, this.searchString, this.lastSearchPage + 1);
    }

    public void setSearchResults(final @NonNull List<Object> searchResults, final @NonNull String searchString) {
        // If search results are stale since search string was recently updated, then ignore
        // this late list of search result data coming in
        if (!this.searchString.equals(searchString)) {
            return;
        }
        // If no search results for this string were found, make it clear that no more
        // pages can be loaded and hide the progress wheel
        else if (searchResults.isEmpty()) {
            this.moreSearchResultsAvailable = false;
            this.progressWheelView.setVisibility(View.GONE);
            return;
        }

        // Fill search page items with search results that were queried from the database
        this.searchPageItems.addAll(searchResults);

        // Add null at end of item list as a placeholder for the load more results view
        this.searchPageItems.add(null);

        // About to display search result data, so hide progress wheel again
        this.progressWheelView.setVisibility(View.GONE);

        // First page for this search string is being populated, so you can get the next page
        // of search results now
        this.lastSearchPage = 1;
        this.moreSearchResultsAvailable = true;

        // Notify how many new search results need to be populated on the page. Header and search
        // bar should never be updated so notify adapter of insertions starting at position 2
        // instead of 0.
        notifyItemRangeInserted(2, this.searchPageItems.size());
    }

    // Should only get next page of search results if not displaying genres rows, if there are already
    // search results populated on the screen, and if there are more search results available
    public boolean shouldGetNextPage() {
        return !this.searchString.isEmpty() && !this.searchPageItems.isEmpty() && moreSearchResultsAvailable;
    }

    public void populateNextPage(final @NonNull List<Object> searchResults, final @NonNull String searchString) {
        // If search string was modified while retrieving next page, then ignore
        // the outdated page results
        if (!this.searchString.equals(searchString)) {
            return;
        }
        // If no search results were found in the next page, return since no more
        // pages can be loaded
        else if (searchResults.isEmpty()) {
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
            final View searchBarView = inflater.inflate(R.layout.search_bar, parent, false);

            // Set a listener to open and close keyboard when search bar gains and loses focus
            ((SearchView) searchBarView).setOnQueryTextFocusChangeListener((v, hasFocus) -> {
                final InputMethodManager inputMethodManager = (InputMethodManager) parent.getContext()
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);

                if (!hasFocus) {
                    inputMethodManager.hideSoftInputFromWindow(searchBarView.getWindowToken(), 0);
                }
                else {
                    // Newer Android versions only worked with "showSoftInput" whereas older Android
                    // versions only worked with "toggleSoftInput", so just try the first approach
                    // and if it fails to open the keyboard, then try the second one
                    if (!inputMethodManager.showSoftInput(searchBarView, InputMethodManager.SHOW_IMPLICIT)) {
                        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                    }
                }
            });

            // Return new view holder for inflated search bar
            return new SearchPageAdapter.SearchBarViewHolder(searchBarView);
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
            final SearchView searchBarView = (SearchView) viewHolder.itemView;

            // Restore the search bar's search string when it is scrolled back onto the screen
            searchBarView.setQuery(this.searchString, false);

            // Set listener on search bar that is triggered anytime something is typed into
            // the search bar
            searchBarView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
        }
        else if (itemViewType == VIEW_TYPE_SEARCH_RESULT) {
            final SearchPageAdapter.SearchResultViewHolder searchResultViewHolder =
                    (SearchPageAdapter.SearchResultViewHolder) viewHolder;

            // Get the current movie search result at specified position in list. Offset position
            // by -2 since header and search bar are at position 0 and 1 in the RecyclerView
            final @NonNull SearchResultMovieViewModel searchResultData = (SearchResultMovieViewModel) this.searchPageItems.get(position - 2);

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

           // TODO: hide triangle warning icon by default

           // Get list of genre strings
           final @NonNull List<String> genresList = searchResultData.getGenres();

           // Get genre tile recycler view for current view holder and its associated adapter.
           // If valid GenreTilesAdapter was found, then populate the adapter's genresList
           // with new list of genre strings
           final RecyclerView.Adapter genreTilesAdapter = searchResultViewHolder.searchResultGenresRecyclerView.getAdapter();
           if (genreTilesAdapter instanceof GenreTilesAdapter) {
               ((GenreTilesAdapter) genreTilesAdapter).setGenresList(genresList);
           }

           searchResultViewHolder.searchResultMovieTitle.setText(searchResultData.getMovieName());
        }
        else if (itemViewType == VIEW_TYPE_LOAD_MORE) {
            final SearchPageAdapter.LoadMoreViewHolder loadMoreViewHolder =
                    (SearchPageAdapter.LoadMoreViewHolder) viewHolder;

            // If there are no more search results available for the current search string,
            // then set the "No Results Available" text to visible at the bottom of the RecyclerView
            if (!this.moreSearchResultsAvailable) {
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

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView headerTitle;

        public HeaderViewHolder(final @NonNull View headerView) {
            super(headerView);
            this.headerTitle = headerView.findViewById(R.id.header_title);
        }
    }

    public static class SearchBarViewHolder extends RecyclerView.ViewHolder {
        public SearchBarViewHolder(final @NonNull View searchBarView) {
            super(searchBarView);
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
