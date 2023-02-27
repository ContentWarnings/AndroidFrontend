package com.example.moviementor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;

import java.util.List;

public class GenreTilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final @NonNull List<String> genreList;

    public GenreTilesAdapter(final @NonNull List<String> genreList) {
        this.genreList = genreList;
    }

    // Replace adapter's old list of genres with new list of genres
    public void setGenreList(final @NonNull List<String> newGenreList) {
        // If there were already genres in the adapter, then clear the old
        // list of genres and any genre tile views that were present
        if (!this.genreList.isEmpty()) {
            final int previousLen = this.genreList.size();
            this.genreList.clear();
            notifyItemRangeRemoved(0, previousLen);
        }

        // Add all the new genres to the empty genreList and notify RecyclerView
        // of how many genres were added to the list
        this.genreList.addAll(newGenreList);
        notifyItemRangeInserted(0, this.genreList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate custom layout for singular genre tile
        final View genreTileView = inflater.inflate(R.layout.genre_tile, parent, false);

        // Return new view holder for inflated genre tile
        return new GenreTileViewHolder(genreTileView);
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final GenreTileViewHolder genreTileViewHolder = (GenreTileViewHolder) viewHolder;

        final @NonNull String genreName = genreList.get(position);

        // Set the genre tile's text and corresponding icon according to the genre name found in
        // the list
        genreTileViewHolder.genreTileIcon.setImageResource(getGenreIconRes(genreName));
        genreTileViewHolder.genreTileName.setText(genreName);
    }

    @Override
    public int getItemCount() {
        return this.genreList.size();
    }

    public static class GenreTileViewHolder extends RecyclerView.ViewHolder {
        public final ImageView genreTileIcon;
        public final TextView genreTileName;

        public GenreTileViewHolder(final @NonNull View genreTileView) {
            super(genreTileView);
            this.genreTileIcon = genreTileView.findViewById(R.id.genre_tile_icon);
            this.genreTileName = genreTileView.findViewById(R.id.genre_tile_name);
        }
    }

    // Returns associated genre icon's resource id for the genre name passed in. If genre name does
    // not match any of the choices, then default dot icon's id is returned
    @DrawableRes
    private int getGenreIconRes(final @NonNull String genreName) {
        switch (genreName) {
            case "Action":
                return R.drawable.action_icon;
            case "Adventure":
                return R.drawable.adventure_icon;
            case "Animation":
                return R.drawable.animation_icon;
            case "Comedy":
                return R.drawable.comedy_icon;
            case "Crime":
                return R.drawable.crime_icon;
            case "Documentary":
                return R.drawable.documentary_icon;
            case "Drama":
                return R.drawable.drama_icon;
            case "Family":
                return R.drawable.family_icon;
            case "Fantasy":
                return R.drawable.fantasy_icon;
            case "History":
                return R.drawable.history_icon;
            case "Horror":
                return R.drawable.horror_icon;
            case "Music":
                return R.drawable.music_icon;
            case "Mystery":
                return R.drawable.mystery_icon;
            case "Romance":
                return R.drawable.romance_icon;
            case "Science Fiction":
                return R.drawable.scifi_icon;
            case "TV Movie":
                return R.drawable.tvmovie_icon;
            case "Thriller":
                return R.drawable.thriller_icon;
            case "War":
                return R.drawable.war_icon;
            case "Western":
                return R.drawable.western_icon;
            default:
                return R.drawable.genre_dot_icon;
        }
    }
}
