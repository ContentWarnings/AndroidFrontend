package com.example.moviementor.adapters;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.other.ContentWarningPrefsStorage.ContentWarningVisibility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContentWarningTilesAdapter extends RecyclerView.Adapter {
    private final @NonNull List<String> contentWarningList;
    private @NonNull Map<String, ContentWarningVisibility> cwPrefsMap;

    public ContentWarningTilesAdapter(final @NonNull List<String> contentWarningList) {
        this.contentWarningList = contentWarningList;
        this.cwPrefsMap = new HashMap<>();
    }

    // Replace adapter's old list of content warnings with new list of content warnings and update
    // the user's content warning preferences too
    public void setContentWarningListAndPrefs(final @NonNull List<String> newContentWarningList,
                                              final @NonNull Map<String, ContentWarningVisibility> cwPrefsMap) {
        // If there were already content warnings in the adapter, then clear the old
        // list of content warnings and any content warning tile views that were present
        if (!this.contentWarningList.isEmpty()) {
            final int previousLen = this.contentWarningList.size();
            this.contentWarningList.clear();
            notifyItemRangeRemoved(0, previousLen);
        }

        // Update the user's content warning preferences (in case they changed) before displaying
        // the new content warning tiles
        this.cwPrefsMap = cwPrefsMap;

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

        // If user flagged to be warned about this content warning then give the tile an alternate
        // background color and bolded text
        if (this.cwPrefsMap.getOrDefault(contentWarning, ContentWarningVisibility.SHOW) == ContentWarningVisibility.WARN) {
            contentWarningTileViewHolder.itemView.setBackgroundResource(R.drawable.content_warning_tile_warn_background);
            contentWarningTileViewHolder.contentWarningText.setTypeface(Typeface.DEFAULT_BOLD);
        }
        // Otherwise, give the tile a normal background color and normal text
        else {
            contentWarningTileViewHolder.itemView.setBackgroundResource(R.drawable.content_warning_tile_normal_background);
            contentWarningTileViewHolder.contentWarningText.setTypeface(Typeface.DEFAULT);
        }

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
