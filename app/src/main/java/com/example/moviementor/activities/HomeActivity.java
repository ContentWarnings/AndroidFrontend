package com.example.moviementor.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.moviementor.R;
import com.example.moviementor.adapters.TrendingMoviesAdapter;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.example.moviementor.other.SpanSizeLookupWithHeader;
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

        // Fetch currently trending movies from the database
        fetchTrendingMovies();
    }

    private void createAndPopulateTrendingMoviesList(final @NonNull List<TrendingMovieViewModel> trendingMoviesData) {
        // Get number of columns that grid will render
        final int numColumns = getResources().getInteger(R.integer.num_trending_list_columns);

        // Initialize RecyclerView and its adapter
        final RecyclerView trendingMoviesRecyclerView = findViewById(R.id.trending_movies_recycler_view);
        final TrendingMoviesAdapter trendingMoviesAdapter = new TrendingMoviesAdapter(trendingMoviesData);

        // Bind the adapter and a Grid Layout Manager to the RecyclerView
        trendingMoviesRecyclerView.setAdapter(trendingMoviesAdapter);
        final GridLayoutManager layoutManager = new GridLayoutManager(this, numColumns);
        layoutManager.setSpanSizeLookup(new SpanSizeLookupWithHeader(numColumns));
        trendingMoviesRecyclerView.setLayoutManager(layoutManager);

        // Now that RecyclerView is populated, remove the loading progress wheel
        final ProgressBar loadingProgressWheel = findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);
    }

    private void fetchTrendingMovies() {
        final AsyncHttpClient client = new AsyncHttpClient();

        // TODO: Remove apiKey once it is not needed anymore
        final String apiKey = "oOA8cKgOSs3JzWK3jyAcT7kwuzLavPSh47lvhpmG";
        client.addHeader("x-api-key", apiKey);

        // Hit the search API with no parameters to get data for currently trending movies
        client.get("https://3rash4qeq4.execute-api.us-east-1.amazonaws.com/prod/search", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                final List<TrendingMovieViewModel> trendingMoviesList = new ArrayList<>();

                try {
                    final JSONArray searchResults = new JSONObject(new String(responseBody)).getJSONArray("results");

                    // Decode each movie's data into a ViewModel object
                    for (int i = 0; i < searchResults.length(); i++) {
                        final JSONObject searchResult = searchResults.getJSONObject(i);

                        final @NonNull String movieTitle = searchResult.getString("title");
                        final @NonNull String movieImageUrl = searchResult.getString("img");

                        final TrendingMovieViewModel movieData = new TrendingMovieViewModel(movieTitle, movieImageUrl);
                        trendingMoviesList.add(movieData);
                    }
                }
                catch (final JSONException e) {
                    Log.e("HomeActivity: ", e.toString());
                    // TODO: Upon failure set home page view to an error screen with a reload button
                }

                // Done fetching and parsing movie data so populate the home page's list now
                createAndPopulateTrendingMoviesList(trendingMoviesList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("HomeActivity: ", error.toString());
                // TODO: Upon failure set home page view to an error screen with a reload button
            }
        });
    }
}
