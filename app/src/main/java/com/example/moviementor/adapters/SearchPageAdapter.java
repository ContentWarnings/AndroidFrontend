package com.example.moviementor.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.models.GenreViewModel;
import com.example.moviementor.models.SearchResultMovieViewModel;
import com.example.moviementor.other.Backend;

import java.util.ArrayList;
import java.util.List;

public class SearchPageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_SEARCH_BAR = 2;
    private static final int VIEW_TYPE_GENRE = 3;
    private static final int VIEW_TYPE_SEARCH_RESULT = 4;

    private final @NonNull List<Object> searchPageItems;
    private final @NonNull List<Object> genreItems;
    private boolean isDisplayingGenres;
    private @NonNull String searchString;

    // Defines how transparent background images for genre rows should be
    private int genreBackgroundAlphaValue;

    public SearchPageAdapter(final @NonNull List<Object> genreItems) {
        this.searchPageItems = new ArrayList<>();
        this.searchPageItems.addAll(genreItems);

        this.genreItems = genreItems;
        this.isDisplayingGenres = true;
        this.genreBackgroundAlphaValue = 0;
        this.searchString = "";
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

            // Replace all current search results with genre data again to re-populate
            // screen with list of genres
            this.isDisplayingGenres = true;
            this.searchPageItems.clear();
            this.searchPageItems.addAll(this.genreItems);
            return;
        }

        this.searchString = newSearchString;

        // If transitioning from displaying genres to search results, then clear adapter's
        // item list of genre data to stop displaying list of genres
        if (this.isDisplayingGenres) {
            this.isDisplayingGenres = false;
            this.searchPageItems.clear();
        }

        // Fetch search results for the new search string from the database
        Backend.fetchSearchResults(this, this.searchString);
    }

    public void setSearchResults(final @NonNull List<Object> searchResults, final @NonNull String searchString) {
        // If already switched back to displaying genres or search results are stale since search
        // string was updated, then ignore this late list of search result data coming in
        if (this.isDisplayingGenres || !this.searchString.equals(searchString)) {
            return;
        }

        // Get size of list before overwriting with new search results
        final int oldListSize = this.searchPageItems.size();

        // Replace search page items with search results that were queried from the database
        this.searchPageItems.clear();
        this.searchPageItems.addAll(searchResults);

        // Notify that all the old search result items were removed and then notify how many new
        // search results need to be populated on the page. Header and search bar should never be
        // updated so notify adapter of changes starting at position 2 instead of 0.
        notifyItemRangeRemoved(2, oldListSize);
        notifyItemRangeChanged(2, this.searchPageItems.size());
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
        } else if (viewType == VIEW_TYPE_SEARCH_BAR) {
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
        } else if (viewType == VIEW_TYPE_GENRE) {
            // Inflate custom layout for singular genre
            final View genreItemView = inflater.inflate(R.layout.genre_row, parent, false);

            // Return new view holder for inflated genre item
            return new SearchPageAdapter.GenreViewHolder(genreItemView);
        } else {
            // Inflate custom layout for search result item
            final View movieSearchResultView = inflater.inflate(R.layout.search_result_row, parent, false);

            // Return new view holder for inflated move search result
            return new SearchPageAdapter.SearchResultViewHolder(movieSearchResultView);
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

            // Bind this movie's title to the search result item view generated by the RecyclerView
            searchResultViewHolder.searchResultMovieTitle.setText(searchResultData.getMovieName());
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
        public final TextView searchResultMovieTitle;

        public SearchResultViewHolder(final @NonNull View searchResultView) {
            super(searchResultView);
            this.searchResultMovieTitle = searchResultView.findViewById(R.id.search_result_movie_title);
        }
    }
}
