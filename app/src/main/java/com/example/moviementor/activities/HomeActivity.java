package com.example.moviementor.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.moviementor.R;
import com.example.moviementor.adapters.TrendingMoviesAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        createAndPopulateTrendingMoviesList();
    }

    private void createAndPopulateTrendingMoviesList() {
        RecyclerView trendingMoviesRecyclerView = findViewById(R.id.trending_movies_recycler_view);

        // Initialize dummy list of movie data
        List<String> stringList = new ArrayList<>();
        stringList.add("movie1");
        stringList.add("movie2");
        stringList.add("movie3");
        stringList.add("movie4");
        stringList.add("movie5");
        stringList.add("movie6");
        stringList.add("movie7");
        stringList.add("movie8");
        stringList.add("movie9");
        stringList.add("movie10");
        stringList.add("movie11");
        stringList.add("movie12");
        stringList.add("movie13");
        stringList.add("movie14");
        stringList.add("movie15");
        stringList.add("movie16");

        TrendingMoviesAdapter trendingMoviesAdapter = new TrendingMoviesAdapter(stringList);
        trendingMoviesRecyclerView.setAdapter(trendingMoviesAdapter);
        trendingMoviesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
