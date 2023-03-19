package com.example.moviementor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;

import java.util.List;

public class ContentWarningsSettingsAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private final @NonNull List<String> contentWarningNames;

    private @Nullable OnItemClickListener listener;

    public ContentWarningsSettingsAdapter(final @NonNull List<String> contentWarningNames) {
        this.contentWarningNames = contentWarningNames;
        this.listener = null;
    }

    // Returns the total number of content warnings list plus 1 for the header
    @Override
    public int getItemCount() {
        return this.contentWarningNames.size() + 1;
    }

    // Returns whether view is of type header or content warning item.
    @Override
    public int getItemViewType(final int position) {
        // First view is the header, all other views are for movie items
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
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

        // Inflate custom layout for the content warning row
        final View contentWarningItemView = inflater.inflate(R.layout.content_warning_settings_row,
                parent, false);

        // Return new view holder for singular content warning row
        return new ItemViewHolder(contentWarningItemView);
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;

            // Setup back button in header
            headerViewHolder.headerBackButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onHeaderBackButtonClick();
                }
            });

            // Setup click listener on header's search button to start user's request of jumping to
            // the search bar on the search page
            headerViewHolder.headerSearchButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHeaderSearchButtonClick();
                }
            });

        } else {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

            // Get the current content warning to display
            final @NonNull String contentWarningName = this.contentWarningNames.get(position - 1);

            // Bind this content warning name to the row's first line of text
            itemViewHolder.contentWarningName.setText(contentWarningName);

            // TODO: Display user's visibility status of the cw instead of just always defaulting to show
            itemViewHolder.contentWarningVisibility.setText("Show");

            // If this is the last content warning row, then hide the bottom divider since there is
            // no content warning row that will be displayed below
            if (position == this.contentWarningNames.size()) {
                final View contentWarningDivider = viewHolder.itemView
                        .findViewById(R.id.content_warning_divider);
                contentWarningDivider.setVisibility(View.GONE);
            }
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
        void onHeaderSearchButtonClick();
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final ImageView headerBackButton;
        public final ImageButton headerSearchButton;

        public HeaderViewHolder(final @NonNull View headerView) {
            super(headerView);
            this.headerBackButton = headerView.findViewById(R.id.content_warnings_page_back_button);
            this.headerSearchButton = headerView.findViewById(R.id.content_warnings_page_header_search_button);

            // Set up click listener on header's back button to call parent fragment's
            // listener function if click detected and listener is currently attached
            headerSearchButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHeaderBackButtonClick();
                }
            });

            // Set up click listener on header's search button to call parent fragment's
            // listener function if click detected and listener is currently attached
            headerSearchButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHeaderSearchButtonClick();
                }
            });
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView contentWarningName;
        public final TextView contentWarningVisibility;

        public ItemViewHolder(final @NonNull View contentWarningItemView) {
            super(contentWarningItemView);
            this.contentWarningName = contentWarningItemView.findViewById(R.id.content_warning_name);
            this.contentWarningVisibility = contentWarningItemView.findViewById(R.id.content_warning_visibility);
        }
    }
}
