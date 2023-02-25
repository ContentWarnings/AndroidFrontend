package com.example.moviementor.other;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.adapters.SearchPageAdapter;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.models.SearchResultMovieViewModel;
import com.example.moviementor.models.TrendingMovieViewModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class Backend {
    private static final String SEARCH_TAG = "SEARCH";
    private static final int INVALID_MOVIE_ID = -1;
    private static final String DATE_FORMAT_STRING = "dd-mm-yyyy";

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

                        // Skip over any invalid results
                        if (searchResult == null) {
                            continue;
                        }

                        final @NonNull String movieTitle = searchResult.optString("title", "");
                        final @NonNull String movieImageUrl = searchResult.optString("img", "");

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

        // Cancel all in progress search API requests if new one is being made
        client.cancelRequestsByTAG(SEARCH_TAG, true);

        // Hit search API with specified search parameters
        client.get(getAbsoluteUrl(searchParamsUrl), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                final @NonNull List<Object> searchResultViewModels = new ArrayList<>();

                try {
                    final JSONArray searchResults = new JSONObject(new String(responseBody)).getJSONArray("results");

                    // Used to parse through date strings returned from the backend
                    final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US);

                    // Decode each movie's data into a SearchResultMovieViewModel object
                    for (int i = 0; i < searchResults.length(); i++) {
                        final JSONObject searchResult = searchResults.getJSONObject(i);

                        // Skip over any invalid search results
                        if (searchResult == null) {
                            continue;
                        }

                        final int movieId = searchResult.optInt("id", INVALID_MOVIE_ID);

                        // Can not find movie's id, so move onto next result
                        if (movieId == INVALID_MOVIE_ID) {
                            continue;
                        }

                        final @NonNull String movieName = searchResult.optString("title", "");
                        final @NonNull String releaseDateString = searchResult.optString("release", "");
                        final @NonNull String movieImageUrlString = searchResult.optString("img", "");
                        final @NonNull String movieOverview = searchResult.optString("overview", "");
                        final int runtime = searchResult.optInt("runtime", SearchResultMovieViewModel.MISSING_RUNTIME);
                        final @Nullable JSONArray genreJSONArray = searchResult.optJSONArray("genres");
                        final @Nullable JSONArray contentWarningsJSONArray = searchResult.optJSONArray("cw");

                        // Try parsing date string and default to null if unsuccessful
                        @Nullable Date releaseDate;
                        try {
                            releaseDate = formatter.parse(releaseDateString);
                        }
                        catch (final ParseException e) {
                            releaseDate = null;
                        }

                        // Try making URL object and default to null if unsuccessful
                        @Nullable URL movieImageUrl;
                        try {
                            movieImageUrl = new URL(movieImageUrlString);
                        }
                        catch(final MalformedURLException e) {
                            movieImageUrl = null;
                        }

                        // Parse through genre strings and add valid genres to the list
                        final @NonNull List<String> genreList = new ArrayList<>();
                        if (genreJSONArray != null) {
                            for (int j = 0; j < genreJSONArray.length(); j++) {
                                final @Nullable String genre = genreJSONArray.optString(j, null);

                                // Skip over any invalid strings in the genres array
                                if (genre == null) {
                                    continue;
                                }

                                genreList.add(genre);
                            }
                        }

                        // Parse through content warning strings and add valid content warnings to the list
                        final @NonNull List<String> contentWarningList = new ArrayList<>();
                        if (contentWarningsJSONArray != null) {
                            for (int j = 0; j < contentWarningsJSONArray.length(); j++) {
                                final @Nullable String contentWarning = contentWarningsJSONArray.optString(j, null);

                                // Skip over any invalid strings in the content warnings array
                                if (contentWarning == null) {
                                    continue;
                                }

                                contentWarningList.add(contentWarning);
                            }
                        }

                        final @NonNull Object searchResultViewModel =
                                new SearchResultMovieViewModel(movieId, movieName, releaseDate,
                                        movieImageUrl, movieOverview, runtime, genreList,
                                        contentWarningList);
                        searchResultViewModels.add(searchResultViewModel);
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set search page to display error message
                }

                // Done fetching and parsing movie search results, so populate
                // the search page with these updated results
                adapter.setSearchResults(searchResultViewModels, searchString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set search page to display error message
            }
        }).setTag(SEARCH_TAG);
    }
}
