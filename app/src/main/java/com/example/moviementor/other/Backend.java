package com.example.moviementor.other;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.moviementor.adapters.SearchPageAdapter;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class Backend {
    // Base url that will be used for all API requests
    private static final String baseUrl = "https://api.moviementor.app/";

    private static final AsyncHttpClient client = createAndSetupClient();

    private static AsyncHttpClient createAndSetupClient() {
        final AsyncHttpClient client = new AsyncHttpClient();

        // Make client allow circular redirects, so that when deleting characters in search string,
        // the same reverse search requests can be made
        client.setEnableRedirects(true, false, true);

        return client;
    }

    private static String getAbsoluteUrl(final @NonNull String relativeUrl) {
        return baseUrl + relativeUrl;
    }

    private static String getRelativeSearchUrl(final @NonNull String searchString) {
        String relativeSearchUrl = "search/?";
        relativeSearchUrl += "q=" + searchString;
        // TODO: Add other search parameters like genre and page if available
        return relativeSearchUrl;
    }

    public static void fetchTrendingMovies(final @NonNull FeaturedFragment fragment) {
        // Hit the search API with no parameters to get data for currently trending movies
        client.get(getAbsoluteUrl("search"), new AsyncHttpResponseHandler() {
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
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set home page view to an error screen with a reload button
                }

                // Done fetching and parsing movie data so populate the home page's list now
                fragment.createAndPopulateTrendingMoviesList(trendingMoviesList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set home page view to an error screen with a reload button
            }
        });
    }

    public static void fetchSearchResults(final @NonNull SearchPageAdapter adapter, final @NonNull String searchString) {
        // Setup search params for search URL
        final String searchParamsUrl = getRelativeSearchUrl(searchString);

        // Hit search API with specified search parameters
        client.get(getAbsoluteUrl(searchParamsUrl), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    final JSONArray searchResults = new JSONObject(new String(responseBody)).getJSONArray("results");

                    // Decode each movie's data into a SearchResultViewModel object
                    for (int i = 0; i < searchResults.length(); i++) {
                        final JSONObject searchResult = searchResults.getJSONObject(i);
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set search page to display error message
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set search page to display error message
            }
        });
    }
}
