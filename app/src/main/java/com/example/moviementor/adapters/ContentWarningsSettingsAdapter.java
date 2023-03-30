package com.example.moviementor.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.other.ContentWarningPrefsStorage.ContentWarningVisibility;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentWarningsSettingsAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_SEARCH_BAR = 2;
    private static final int VIEW_TYPE_ITEM = 3;

    private final @NonNull List<String> unfilteredContentWarningNames;
    private @NonNull List<String> contentWarningNames;
    private final @NonNull Map<String, ContentWarningVisibility> cwPrefsMap;

    private @NonNull String searchString;
    private @Nullable OnItemClickListener listener;

    private final @NonNull TextView emptySearchText;

    public ContentWarningsSettingsAdapter(final @NonNull List<String> contentWarningNames,
                                          final @NonNull Map<String, ContentWarningVisibility> cwPrefsMap,
                                          final @NonNull TextView emptySearchText) {
        this.unfilteredContentWarningNames = contentWarningNames;
        this.contentWarningNames = new ArrayList<>(contentWarningNames);
        this.cwPrefsMap = cwPrefsMap;

        this.emptySearchText = emptySearchText;

        this.searchString = "";
        this.listener = null;
    }

    // Returns the total number of content warnings list plus 2 for the header and search bar
    @Override
    public int getItemCount() {
        return this.contentWarningNames.size() + 2;
    }

    // Returns whether view is of type header, search bar, or content warning item.
    @Override
    public int getItemViewType(final int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        else if (position == 1) {
            return VIEW_TYPE_SEARCH_BAR;
        }
        else {
            return VIEW_TYPE_ITEM;
        }
    }

    // Called to update visibility text of a content warning row in case a content warning's
    // individual settings were modified when returning to this page
    public void updateContentWarningPrefs(final @NonNull Map<String, ContentWarningVisibility> newCwPrefsMap) {
        // Union all the keys from the new and old maps
        final Set<String> allKeys = new HashSet<>();
        allKeys.addAll(newCwPrefsMap.keySet());
        allKeys.addAll(this.cwPrefsMap.keySet());

        for (final String contentWarningName : allKeys) {
            // Found difference between the new and old content warning preferences
            if (newCwPrefsMap.getOrDefault(contentWarningName, null)
                    != this.cwPrefsMap.getOrDefault(contentWarningName, null))  {

                final ContentWarningVisibility newCwPref = newCwPrefsMap.getOrDefault(contentWarningName, null);

                // If no preference found for this content warning in the new cw preferences map,
                // then remove the preference for this content warning in the original map
                if (newCwPref == null) {
                    this.cwPrefsMap.remove(contentWarningName);
                }
                // Otherwise, just update the value in the old cw preferences map with
                // the value found in the new cw preferences map for this content warning
                else {
                    this.cwPrefsMap.put(contentWarningName, newCwPref);
                }

                // Find and update content warning row that had its visibility status modified.
                // Offset position to update in RecyclerView by 2 since header and search bar are
                // at the beginning
                final int rowChangePosition = this.contentWarningNames.indexOf(contentWarningName);
                notifyItemChanged(rowChangePosition + 2);

                // Only one content warning's preferences can be updated at a time before returning
                // to this page, so just return early since the rest of the old and new map will
                // be identical
                return;
            }
        }
    }

    // Called whenever the search bar's string is modified to re-filter what content warnings
    // are being displayed on the page
    private void onSearchStringChange(final @NonNull String newSearchString) {
        // Don't do anything since the search string was actually not modified at all
        if (this.searchString.equals(newSearchString)) {
            return;
        }
        // If search bar text was cleared then add all content warnings back onto the page
        // that were previously filtered out
        else if (newSearchString.isEmpty()) {
            this.searchString = newSearchString;

            final int oldFilteredListSize = this.contentWarningNames.size();
            this.contentWarningNames = new ArrayList<>(this.unfilteredContentWarningNames);

            notifyItemRangeRemoved(2, oldFilteredListSize);
            notifyItemRangeInserted(2, this.contentWarningNames.size());
        }
        // Otherwise, need to re-filter the list of content warnings being displayed on the page
        else {
            this.searchString = newSearchString;

            final int oldFilteredListSize = this.contentWarningNames.size();
            this.contentWarningNames = new ArrayList<>();
            final String lowerCaseSearchString = this.searchString.toLowerCase();
            for (final @NonNull String contentWarningName : this.unfilteredContentWarningNames) {
                if (contentWarningName.toLowerCase().contains(lowerCaseSearchString)) {
                    this.contentWarningNames.add(contentWarningName);
                }
            }

            notifyItemRangeRemoved(2, oldFilteredListSize);
            notifyItemRangeInserted(2, this.contentWarningNames.size());
        }

        // Toggle on/off empty search text based on whether or not all content warnings were
        // filtered out from being displayed on the page
        if (this.contentWarningNames.isEmpty()) {
            this.emptySearchText.setVisibility(View.VISIBLE);
            this.emptySearchText.bringToFront();
        }
        else {
            this.emptySearchText.setVisibility(View.GONE);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            // Inflate view from header layout file
            final View headerView = inflater.inflate(R.layout.content_warnings_settings_header,
                    parent, false);

            // Get the current layout parameters from the parent RecyclerView
            final RelativeLayout.LayoutParams rvParams = (RelativeLayout.LayoutParams) parent.
                    findViewById(R.id.content_warnings_settings_recycler_view).getLayoutParams();

            // Get the left and right margin from the RecyclerView's layout parameters
            final int leftMargin = rvParams.leftMargin;
            final int rightMargin = rvParams.rightMargin;

            // Get the current layout parameters for the header
            final RecyclerView.LayoutParams headerParams = (RecyclerView.LayoutParams)
                    headerView.getLayoutParams();

            // Set the header's left and right margins equal to the negative left and right margins
            // of the parent RecyclerView. This effectively cancels out any horizontal margins on
            // the header view
            headerParams.setMargins(-leftMargin, 0, -rightMargin, 0);
            headerView.setLayoutParams(headerParams);

            // Return new view holder for inflated header
            return new HeaderViewHolder(headerView);
        }
        else if (viewType == VIEW_TYPE_SEARCH_BAR) {
            // Inflate custom layout for the search bar row
            final View contentWarningsSearchBarView = inflater.inflate(R.layout.content_warnings_search_bar,
                    parent, false);

            // Get actual search bar inside search row
            final SearchView searchBar = contentWarningsSearchBarView.findViewById(R.id.search_bar);

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
            return new SearchBarViewHolder(contentWarningsSearchBarView);
        }
        else {
            // Inflate custom layout for the content warning row
            final View contentWarningItemView = inflater.inflate(R.layout.content_warning_settings_row,
                    parent, false);

            // Return new view holder for singular content warning row
            return new ItemViewHolder(contentWarningItemView);
        }
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;

            // Set up click listener on header's back button to call parent fragment's
            // listener function if click detected and listener is currently attached
            headerViewHolder.headerBackButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHeaderBackButtonClick();
                }
            });
        }
        else if (getItemViewType(position) == VIEW_TYPE_SEARCH_BAR) {
            final View searchRowView = viewHolder.itemView;

            // Get actual search bar inside search row
            final SearchView searchBar = searchRowView.findViewById(R.id.search_bar);

            // Restore the search bar's search string when it is scrolled back onto the screen
            searchBar.setQuery(this.searchString, false);

            // Set listener on search bar that is triggered anytime something is typed into
            // the search bar
            searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                // Don't do anything when user presses submit since content warnings should already
                // be filtered
                @Override
                public boolean onQueryTextSubmit(final @NonNull String s) {
                    return false;
                }

                // Whenever text in the search bar is modified, need to handle the change
                @Override
                public boolean onQueryTextChange(final @NonNull String str) {
                    onSearchStringChange(str.trim());
                    return false;
                }
            });
        }
        else {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

            // Get the current content warning to display
            final @NonNull String contentWarningName = this.contentWarningNames.get(position - 2);

            // Bind this content warning name to the row's first line of text
            itemViewHolder.contentWarningName.setText(contentWarningName);

            // Get current visibility status user has set for this content warning. If not found
            // in the stored map of content warning settings, then default to "SHOW" since this is
            // the default visibility option for all content warnings
            final ContentWarningVisibility cwVisibilityPref = this.cwPrefsMap
                    .getOrDefault(contentWarningName, ContentWarningVisibility.SHOW);
            final @StringRes int visibilityStringRes = getVisibilityStringRes(cwVisibilityPref);

            // Display visibility status of current content warning in the row
            itemViewHolder.contentWarningVisibility.setText(visibilityStringRes);

            // Setup click listener to open this content warning's full settings page if clicked on
            itemViewHolder.itemView.setOnClickListener(view -> {
                if (this.listener != null) {
                    this.listener.onContentWarningRowClick(contentWarningName, cwVisibilityPref);
                }
            });

            // If this is the last content warning row, then hide the bottom divider since there is
            // no content warning row that will be displayed below
            if (position - 2 == this.contentWarningNames.size() - 1) {
                itemViewHolder.contentWarningDivider.setVisibility(View.GONE);
            }
            else {
                itemViewHolder.contentWarningDivider.setVisibility(View.VISIBLE);
            }
        }
    }

    // Helper function to get resource id for string that describes a content warning row's
    // current visibility status
    @StringRes
    private int getVisibilityStringRes(final ContentWarningVisibility cwVisibility) {
        if (cwVisibility == ContentWarningVisibility.WARN) {
            return R.string.content_warnings_page_visibility_status_warn;
        }
        else if (cwVisibility == ContentWarningVisibility.HIDE) {
            return R.string.content_warnings_page_visibility_status_hide;
        }
        else {
            return R.string.content_warnings_page_visibility_status_show;
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
        void onHeaderBackButtonClick();
        void onContentWarningRowClick(final @NonNull String contentWarningName,
                                      final ContentWarningVisibility contentWarningVisibility);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final ImageView headerBackButton;

        public HeaderViewHolder(final @NonNull View headerView) {
            super(headerView);
            this.headerBackButton = headerView.findViewById(R.id.content_warnings_page_back_button);
        }
    }

    public class SearchBarViewHolder extends RecyclerView.ViewHolder {
        public SearchBarViewHolder(final @NonNull View searchBarView) {
            super(searchBarView);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView contentWarningName;
        public final TextView contentWarningVisibility;
        public final View contentWarningDivider;

        public ItemViewHolder(final @NonNull View contentWarningItemView) {
            super(contentWarningItemView);
            this.contentWarningName = contentWarningItemView.findViewById(R.id.content_warning_name);
            this.contentWarningVisibility = contentWarningItemView.findViewById(R.id.content_warning_visibility);
            this.contentWarningDivider = contentWarningItemView.findViewById(R.id.content_warning_divider);
        }
    }
}
