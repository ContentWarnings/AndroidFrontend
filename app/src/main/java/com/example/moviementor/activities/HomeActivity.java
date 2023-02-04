package com.example.moviementor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.TextView;

import com.example.moviementor.R;
import com.example.moviementor.adapters.TrendingMoviesAdapter;
import com.example.moviementor.models.TrendingMovieViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        // Set the home page's title to trending
        final TextView header_title = findViewById(R.id.header_title);
        header_title.setText(R.string.home_page_header_title);

        // Sets up list of trending movies on home page
        createAndPopulateTrendingMoviesList();
    }

    private void createAndPopulateTrendingMoviesList() {
        // Fetch trending movies from the database
        final @NonNull List<TrendingMovieViewModel> trendingMoviesList = fetchTrendingMovies();

        // Initialize RecyclerView and its adapter
        final RecyclerView trendingMoviesRecyclerView = findViewById(R.id.trending_movies_recycler_view);
        final TrendingMoviesAdapter trendingMoviesAdapter = new TrendingMoviesAdapter(trendingMoviesList);

        // Bind the adapter and a Grid Layout Manager to the RecyclerView
        trendingMoviesRecyclerView.setAdapter(trendingMoviesAdapter);
        trendingMoviesRecyclerView.setLayoutManager(
                new GridLayoutManager(this, getResources().getInteger(R.integer.num_trending_list_columns)));
    }

    @NonNull
    private List<TrendingMovieViewModel> fetchTrendingMovies() {
        // TODO: Replace Dummy Data with API Call to Get Trending Movie Data

        // Initialize dummy list of movie data
        final List<TrendingMovieViewModel> trendingMoviesList = new ArrayList<>();
        trendingMoviesList.add(new TrendingMovieViewModel("The Avengers", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Harry Potter and the Sorcerer's Stone", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("The Dark Knight", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Harry Potter and the Chamber of Secrets", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Harry Potter and the Prisoner of Azkaban", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Moonlight", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Goodfellas", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Iron Man 2", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Avatar", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Tron", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("The Wizard of Oz", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Gone with the Wind", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Pulp Fiction", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Men in Black", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Indiana Jones and the Raiders of the Lost Ark", getResources().getDrawable(R.drawable.test_movie_poster)));
        trendingMoviesList.add(new TrendingMovieViewModel("Star Wars: A New Hope", getResources().getDrawable(R.drawable.test_movie_poster)));

        return trendingMoviesList;
    }
}
