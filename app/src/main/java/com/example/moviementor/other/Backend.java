package com.example.moviementor.other;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.adapters.SearchPageAdapter;
import com.example.moviementor.fragments.FeaturedFragment;
import com.example.moviementor.fragments.MovieFragment;
import com.example.moviementor.models.MovieViewModel;
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

                        final int movieId = searchResult.optInt("id", INVALID_MOVIE_ID);

                        // Can not find movie's id, so move onto next result
                        if (movieId == INVALID_MOVIE_ID) {
                            continue;
                        }

                        final @NonNull String movieTitle = searchResult.optString("title", "");
                        final @NonNull String movieImageUrlString = searchResult.optString("img", "");

                        // Try making URL object and default to null if unsuccessful
                        @Nullable URL movieImageUrl;
                        try {
                            movieImageUrl = new URL(movieImageUrlString);
                        }
                        catch(final MalformedURLException e) {
                            movieImageUrl = null;
                        }

                        final TrendingMovieViewModel movieData = new
                                TrendingMovieViewModel(movieId, movieTitle, movieImageUrl);
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

    public static void fetchMovie(final @NonNull MovieFragment movieFragment, final int movieId) {
        // Setup URL to get movie
        final String fetchMovieUrl = getAbsoluteUrl("movie") + "/" + movieId;

        // Hit API to get movie with specified movie's id as a parameter
        client.get(fetchMovieUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Object that will store all the parsed movie's data
                @Nullable MovieViewModel movieViewModel = null;

                try {
                    final JSONObject movieResult = new JSONObject(new String(responseBody));

                    final int movieId = movieResult.optInt("id", INVALID_MOVIE_ID);

                    // Can not find movie's id, so move onto next result
                    if (movieId == INVALID_MOVIE_ID) {
                        throw new JSONException("No movie data found when fetching movie's data");
                    }

                    final @NonNull String movieTitle = movieResult.optString("title", "");
                    final @NonNull String movieImageUrlString = movieResult.optString("img", "");
                    final @NonNull String releaseDateString = movieResult.optString("release", "");
                    final int movieRuntime = movieResult.optInt("runtime", MovieViewModel.MISSING_RUNTIME);
                    final @NonNull String movieOverview = movieResult.optString("overview", "");
                    final double movieRating = movieResult.optDouble("rating", MovieViewModel.MISSING_RATING);
                    final @NonNull String movieMpaRating = movieResult.optString("mpa", "");
                    final @Nullable JSONArray genreJSONArray = movieResult.optJSONArray("genres");
                    final @Nullable JSONObject streamingInfo = movieResult.optJSONObject("streaming_info");
                    final @Nullable JSONArray contentWarningsJSONArray = movieResult.optJSONArray("cw");

                    // Try making URL object and default to null if unsuccessful
                    @Nullable URL movieImageUrl;
                    try {
                        movieImageUrl = new URL(movieImageUrlString);
                    }
                    catch(final MalformedURLException e) {
                        movieImageUrl = null;
                    }

                    // Used to parse through date string returned from the backend
                    final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_STRING, Locale.US);

                    // Try parsing date string and default to null if unsuccessful
                    @Nullable Date releaseDate;
                    try {
                        releaseDate = formatter.parse(releaseDateString);
                    }
                    catch (final ParseException e) {
                        releaseDate = null;
                    }

                    // Parse through genre strings and add valid genres to the list
                    final @NonNull List<String> genreList = new ArrayList<>();
                    if (genreJSONArray != null) {
                        for (int i = 0; i < genreJSONArray.length(); i++) {
                            final @Nullable String genre = genreJSONArray.optString(i, null);

                            // Skip over any invalid strings in the genres array
                            if (genre == null) {
                                continue;
                            }

                            genreList.add(genre);
                        }
                    }

                    // Initialize empty list of streaming providers available for this movie
                    final List<StreamingProvider> streamingProviders = new ArrayList<>();

                    if (streamingInfo != null) {
                        final @Nullable JSONArray streamingProvidersJSONArray = streamingInfo
                                .optJSONArray("providers");

                        if (streamingProvidersJSONArray != null) {
                            // Parse through each streaming provider in the JSON array and add each
                            // valid provider to the list for this movie
                            for (int i = 0; i < streamingProvidersJSONArray.length(); i++) {
                                final @Nullable JSONArray streamingProviderJSON =
                                        streamingProvidersJSONArray.optJSONArray(i);

                                // If invalid streaming provider is found, then skip over it
                                if (streamingProviderJSON == null) {
                                    continue;
                                }

                                // Split JSON for provider into two strings
                                final @NonNull String providerString = streamingProviderJSON
                                        .optString(0, "");
                                final @NonNull String providerImageUrlString = streamingProviderJSON
                                        .optString(1, "");

                                // If provider's name or option is not mentioned, then skip to next
                                // provider available for this movie
                                if (providerString.isEmpty()) {
                                    continue;
                                }

                                // Split first string for provider entry into the provider's name
                                // and option
                                final String[] providerNameAndOption = providerString
                                        .split(StreamingProvider.PROVIDER_NAME_OPTION_SEPARATOR);

                                // If provider name and option were not found, then continue to next
                                // provider entry available
                                if (providerNameAndOption.length != 2) {
                                    continue;
                                }

                                final @NonNull String providerName = providerNameAndOption[0];
                                final @NonNull String providerOptionString = providerNameAndOption[1];

                                // Convert provider option string into one of three options allowed:
                                // buy, rent, or stream
                                final @Nullable StreamingProvider.ProviderOption providerOption =
                                        StreamingProvider.getProviderOption(providerOptionString);

                                // If provider option string was not one of three options allowed,
                                // then continue to next provider entry available
                                if (providerOption == null) {
                                    continue;
                                }

                                // Try making URL object for the provider's image and default to
                                // null if unsuccessful
                                @Nullable URL providerImageUrl;
                                try {
                                    providerImageUrl = new URL(providerImageUrlString);
                                }
                                catch(final MalformedURLException e) {
                                    providerImageUrl = null;
                                }

                                final StreamingProvider streamingProvider = new
                                        StreamingProvider(providerName, providerOption, providerImageUrl);
                                streamingProviders.add(streamingProvider);
                            }
                        }
                    }

                    final List<ContentWarning> contentWarnings = new ArrayList<>();

                    if (contentWarningsJSONArray != null) {
                        for (int i = 0; i < contentWarningsJSONArray.length(); i++) {
                            final @Nullable JSONObject contentWarningJSONObject = contentWarningsJSONArray
                                    .optJSONObject(i);

                            // If invalid content warning JSON object was found, then skip over it
                            if (contentWarningJSONObject == null) {
                                continue;
                            }

                            final @NonNull String cwId = contentWarningJSONObject
                                    .optString("id", "");

                            // If no id was found for this content warning, then skip over it
                            if (cwId.isEmpty()) {
                                continue;
                            }

                            final @NonNull String cwName = contentWarningJSONObject
                                    .optString("name", "");

                            // If no name was found for this content warning, then skip over it
                            // since there's no content warning name to display
                            if (cwName.isEmpty()) {
                                continue;
                            }

                            final @NonNull String cwDescription = contentWarningJSONObject
                                    .optString("desc", "");

                            final List<TimeStamp> cwTimestamps = new ArrayList<>();

                            final @Nullable JSONArray timestampJSONArray = contentWarningJSONObject
                                    .optJSONArray("time");
                            if (timestampJSONArray != null) {
                                for (int j = 0; j < timestampJSONArray.length(); j++) {
                                    final @Nullable JSONArray timestampJSON = timestampJSONArray
                                            .optJSONArray(j);

                                    // If invalid timestamp is found, then skip over it
                                    if (timestampJSON == null || timestampJSON.length() != 2) {
                                        continue;
                                    }

                                    final int startTime = timestampJSON
                                            .optInt(0, TimeStamp.MISSING_TIME);
                                    final int endTime = timestampJSON
                                            .optInt(1, TimeStamp.MISSING_TIME);

                                    // If either the start or end time is missing or if the start
                                    // time occurs after the end time, then skip over this timestamp
                                    // since it is invalid
                                    if (startTime == TimeStamp.MISSING_TIME ||
                                        endTime == TimeStamp.MISSING_TIME ||
                                        startTime > endTime) {
                                        continue;
                                    }

                                    final @NonNull TimeStamp timeStamp =
                                            new TimeStamp(startTime, endTime);
                                    cwTimestamps.add(timeStamp);
                                }
                            }

                            final ContentWarning contentWarning = new
                                    ContentWarning(cwId, cwName, cwDescription, cwTimestamps);
                            contentWarnings.add(contentWarning);
                        }
                    }

                    movieViewModel = new MovieViewModel(movieId, movieTitle,
                            releaseDate, movieImageUrl, movieOverview, genreList, streamingProviders, contentWarnings);
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set movie page view to an error screen with a reload button
                }

                // Done fetching and parsing movie's data, so populate the movie display
                // page with this movie's data
                movieFragment.populateMoviePage(movieViewModel);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set movie page view to an error screen with a reload button
            }
        });
    }
}
