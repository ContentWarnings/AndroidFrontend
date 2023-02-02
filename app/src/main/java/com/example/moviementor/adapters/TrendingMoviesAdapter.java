package com.example.moviementor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;

import java.util.List;

public class TrendingMoviesAdapter extends RecyclerView.Adapter<TrendingMoviesAdapter.ViewHolder> {
    private List<String> stringList;

    public TrendingMoviesAdapter(List<String> stringList) {
        this.stringList = stringList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate custom layout for row of trending movies
        View trendingMovieRowView = inflater.inflate(R.layout.trending_movie_row, parent, false);

        // Return new view holder for this row
        return new ViewHolder(trendingMovieRowView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // Get the current movie data that will populate this view
        String name = this.stringList.get(position);

        // Set the name and image for the first trending movie in the row
        viewHolder.trendingMovieNameTextView.setText(name);
    }

    // Returns the total number of trending movies received from the database
    @Override
    public int getItemCount() {
        return this.stringList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView trendingMovieNameTextView;

        public ViewHolder(View trendingMovieRowView) {
            super(trendingMovieRowView);

            this.trendingMovieNameTextView = trendingMovieRowView.findViewById(R.id.trending_movie_name);
        }
    }
}
