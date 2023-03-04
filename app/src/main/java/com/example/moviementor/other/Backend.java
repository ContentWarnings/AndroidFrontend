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
import com.loopj.android.http.RequestHandle;

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
    private static final int INVALID_MOVIE_ID = -1;
    private static final String DATE_FORMAT_STRING = "yyyy-mm-dd";
    private static final int NO_PAGE_SPECIFIED = -1;

    // Base url that will be used for all API requests
    private static final String baseUrl = "https://api.moviementor.app/";

    private static final AsyncHttpClient client = createAndSetupClient();

    // Keeps track of last pending search request, so it can be cancelled if a new one is made
    // right after
    private static @Nullable RequestHandle previousSearchRequest = null;

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

    private static String getRelativeSearchUrl(final @NonNull String searchString,
                                               final @NonNull SearchOptions searchOptions,
                                               final int page) {
        // Keeps track of whether or not any search params have been added to the relative search
        // URL yet
        boolean addedSearchParams = false;

        String relativeSearchUrl = "search";

        if (!searchString.isEmpty()) {
            relativeSearchUrl += "/?q=" + searchString;
            addedSearchParams = true;
        }
        if (page != NO_PAGE_SPECIFIED) {
            // No search params added yet so need to add "/?" to relative search URL
            if (!addedSearchParams) {
                relativeSearchUrl += "/?";
                addedSearchParams = true;
            }
            // Search params have already been added, so just add "&" to relative search URL
            // so that next search param can be appended to the URL
            else {
                relativeSearchUrl += "&";
            }

            relativeSearchUrl += "p=" + page;
        }
        if (searchOptions.currentGenreFilterSelected() != null) {
            // No search params added yet so need to add "/?" to relative search URL
            if (!addedSearchParams) {
                relativeSearchUrl += "/?";
                addedSearchParams = true;
            }
            // Search params have already been added, so just add "&" to relative search URL
            // so that next search param can be appended to the URL
            else {
                relativeSearchUrl += "&";
            }

            relativeSearchUrl += "genre=" + searchOptions.getGenreFilterName();
        }
        if (searchOptions.currentSortOptionSelected() != null) {
            // No search params added yet so need to add "/?" to relative search URL
            if (!addedSearchParams) {
                relativeSearchUrl += "/?";
            }
            // Search params have already been added, so just add "&" to relative search URL
            // so that next search param can be appended to the URL
            else {
                relativeSearchUrl += "&";
            }

            relativeSearchUrl += "sort=" + searchOptions.getSortOptionName();
        }

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

    public static void fetchSearchResults(final @NonNull SearchPageAdapter adapter,
                                          final @NonNull String searchString,
                                          final @NonNull SearchOptions searchOptions) {
        // Setup search params for search URL
        final String searchParamsUrl = getRelativeSearchUrl(searchString, searchOptions, NO_PAGE_SPECIFIED);

        // Cancel previous search API request if it hasn't finished yet,
        // since it is now invalidated by this one
        if (previousSearchRequest != null && !previousSearchRequest.isFinished()) {
            previousSearchRequest.cancel(true);
        }

        // Hit search API with specified search parameters
        previousSearchRequest = client.get(getAbsoluteUrl(searchParamsUrl), new AsyncHttpResponseHandler() {
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
                                        movieImageUrl, movieOverview, genreList, contentWarningList);
                        searchResultViewModels.add(searchResultViewModel);
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set search page to display error message
                }

                // Done fetching and parsing movie search results, so populate
                // the search page with these updated results
                adapter.setSearchResults(searchResultViewModels, searchString, searchOptions);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set search page to display error message
            }
        });
    }

    public static void fetchSearchResultsPage(final @NonNull SearchPageAdapter adapter,
                                              final @NonNull String searchString,
                                              final @NonNull SearchOptions searchOptions,
                                              final int pageNum) {
        // Setup search params for search URL
        final String searchParamsUrl = getRelativeSearchUrl(searchString, searchOptions, pageNum);

        // Hit search API with specified search parameters
        previousSearchRequest = client.get(getAbsoluteUrl(searchParamsUrl), new AsyncHttpResponseHandler() {
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
                                        movieImageUrl, movieOverview, genreList,
                                        contentWarningList);
                        searchResultViewModels.add(searchResultViewModel);
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set search page to display error message
                }

                // Done fetching and parsing movie search results, so add on
                // the next page into the search results
                adapter.populateNextPage(searchResultViewModels, searchString, searchOptions);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set search page to display error message
            }
        });
    }
}
