package com.example.moviementor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;

import java.util.List;

public class ContentWarningTilesAdapter extends RecyclerView.Adapter {
    private final @NonNull List<String> contentWarningList;

    public ContentWarningTilesAdapter(final @NonNull List<String> contentWarningList) {
        this.contentWarningList = contentWarningList;
    }

    // Replace adapter's old list of content warnings with new list of content warnings
    public void setContentWarningList(final @NonNull List<String> newContentWarningList) {
        // If there were already content warnings in the adapter, then clear the old
        // list of content warnings and any content warning tile views that were present
        if (!this.contentWarningList.isEmpty()) {
            final int previousLen = this.contentWarningList.size();
            this.contentWarningList.clear();
            notifyItemRangeRemoved(0, previousLen);
        }

        // Add all the new content warnings to the empty contentWarningList and notify RecyclerView
        // of how many content warnings were added to the list
        this.contentWarningList.addAll(newContentWarningList);
        notifyItemRangeInserted(0, this.contentWarningList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate custom layout for singular content warning tile
        final View contentWarningTileView = inflater.inflate(R.layout.content_warning_tile, parent, false);

        // Return new view holder for inflated content warning tile
        return new ContentWarningTilesAdapter.ContentWarningTileViewHolder(contentWarningTileView);
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final ContentWarningTileViewHolder contentWarningTileViewHolder = (ContentWarningTileViewHolder) viewHolder;

        final @NonNull String contentWarning = this.contentWarningList.get(position);

        // Set the content warning tile's text
        contentWarningTileViewHolder.contentWarningText.setText(contentWarning);
    }

    @Override
    public int getItemCount() {
        return this.contentWarningList.size();
    }

    public static class ContentWarningTileViewHolder extends RecyclerView.ViewHolder {
        public final TextView contentWarningText;

        public ContentWarningTileViewHolder(final @NonNull View contentWarningTileView) {
            super(contentWarningTileView);
            this.contentWarningText = contentWarningTileView.findViewById(R.id.content_warning_tile_text);
        }
    }
}
