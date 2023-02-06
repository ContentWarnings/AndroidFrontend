package com.example.moviementor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.moviementor.R;
import com.example.moviementor.adapters.TrendingMoviesAdapter;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

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

        final AsyncHttpClient client = new AsyncHttpClient();
        final String apiKey = "oOA8cKgOSs3JzWK3jyAcT7kwuzLavPSh47lvhpmG";

        client.addHeader("x-api-key", apiKey);

        client.get("https://3rash4qeq4.execute-api.us-east-1.amazonaws.com/prod/search", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    final JSONArray searchResults = new JSONObject(new String(responseBody)).getJSONArray("result");
                    Log.d("SearchResponse: ", "NumSearchResults: " + searchResults.length());
                } catch (final JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
            }
        });

        // Initialize dummy list of movie data
        final List<TrendingMovieViewModel> trendingMoviesList = new ArrayList<>();

        final String[] movieNames = new String[] { "The Avengers", "Harry Potter and the Sorcerer's Stone",
                "The Dark Knight", "Harry Potter and the Chamber of Secrets", "Harry Potter and the Prisoner of Azkaban",
                "Moonlight", "Goodfellas", "Iron Man 2", "Avatar", "Tron", "The Wizard of Oz", "Gone with the Wind",
                "Pulp Fiction", "Men in Black", "Indiana Jones and the Raiders of the Lost Ark", "Star Wars: A New Hope"};
        final Drawable moviePoster = getResources().getDrawable(R.drawable.test_movie_poster);

        for (String movieName : movieNames) {
            trendingMoviesList.add(new TrendingMovieViewModel(movieName, moviePoster));
        }

        return trendingMoviesList;
    }
}
