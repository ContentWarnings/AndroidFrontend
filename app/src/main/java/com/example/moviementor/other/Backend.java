package com.example.moviementor.other;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.adapters.SearchPageAdapter;
import com.example.moviementor.fragments.ContentWarningFragment;
import com.example.moviementor.fragments.ContentWarningFragment.ContentWarningVotingState;
import com.example.moviementor.fragments.ContentWarningSettingsFragment;
import com.example.moviementor.fragments.ContentWarningsFragment;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class Backend {
    private static final int INVALID_MOVIE_ID = -1;
    private static final String DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final int NO_PAGE_SPECIFIED = -1;

    // Base url that will be used for all API requests
    private static final String baseUrl = "https://api.moviementor.app/";

    private static final AsyncHttpClient client = createAndSetupClient();

    // Keeps track of last pending search request, so it can be cancelled if a new one is made
    // right after
    private static @Nullable RequestHandle previousSearchRequest = null;

    // Keeps track of last pending upvote/downvote for a content warning, so that content warning
    // can not be upvoted and downvoted simultaneously
    private static @Nullable RequestHandle previousCwVote = null;

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
                        final @Nullable JSONArray contentWarningsJSONArray = searchResult.optJSONArray("cw");

                        // Try making URL object and default to null if unsuccessful
                        @Nullable URL movieImageUrl;
                        try {
                            movieImageUrl = new URL(movieImageUrlString);
                        }
                        catch(final MalformedURLException e) {
                            movieImageUrl = null;
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

                        final TrendingMovieViewModel movieData = new
                                TrendingMovieViewModel(movieId, movieTitle, movieImageUrl, contentWarningList);
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
        previousCwVote = client.get(fetchMovieUrl, new AsyncHttpResponseHandler() {
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
                    final @NonNull String movieMpaRating = movieResult.optString("mpa", MovieViewModel.MISSING_MPA_RATING);
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

                    // Set streaming providers' link to null initially in case it cannot be
                    // retrieved
                    @Nullable Uri streamingUri = null;

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

                        final @NonNull String streamingUrlString = streamingInfo.optString("tmdb_link", "");

                        // Try making streaming Uri object and default to null if unsuccessful
                        try {
                            // First, validated that the url string is valid
                            final URL streamingUrl = new URL(streamingUrlString);

                            // If valid, make a uri from the string
                            streamingUri = Uri.parse(streamingUrlString);
                        }
                        catch(final MalformedURLException e) {
                            streamingUri = null;
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

                    movieViewModel = new MovieViewModel(movieId, movieTitle, releaseDate,
                            movieRuntime, movieImageUrl, movieOverview, movieRating, movieMpaRating,
                            genreList, streamingProviders, streamingUri, contentWarnings);
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

    public static void fetchContentWarningNames(final @NonNull ContentWarningsFragment contentWarningsFragment) {
        // Setup URL to get list of content warning names
        final String fetchContentWarningNamesUrl = getAbsoluteUrl("names");

        // Hit API to get list of all available content warning names
        client.get(fetchContentWarningNamesUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                final @NonNull List<String> contentWarningNamesList = new ArrayList<>();

                try {
                    final JSONArray contentWarningNames = new JSONObject(new String(responseBody))
                            .getJSONArray("cws");

                    for (int i = 0; i < contentWarningNames.length(); i++) {
                        final String contentWarningName = contentWarningNames.optString(i);

                        // If a valid content warning name is found then add it to th final list
                        if (contentWarningName != null) {
                            contentWarningNamesList.add(contentWarningName);
                        }
                    }

                    // Remove "none" content warning from list since it is only needed on backed
                    contentWarningNamesList.remove("None");

                    // Sort list of content warning names before returning
                    Collections.sort(contentWarningNamesList);
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set content warnings page to display error message
                }

                // Done fetching and parsing content warning names so populate the content warnings
                // settings page now
                contentWarningsFragment.setupContentWarningsPage(contentWarningNamesList);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set content warnings page view to an error screen with a reload button
            }
        });
    }

    public static void fetchContentWarningDescription(
            final @NonNull ContentWarningSettingsFragment contentWarningSettingsFragment,
            final @Nullable String contentWarningName) {
        if (contentWarningName == null) {
            return;
        }

        // Setup URL to get content warning's corresponding description
        final String fetchContentWarningDescriptionUrl = getAbsoluteUrl("descriptions") +
                "?name=" + contentWarningName;

        // Hit API to get list of all available content warning names
        client.get(fetchContentWarningDescriptionUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                @Nullable String contentWarningDescription = null;

                try {
                    final JSONObject contentWarningDescriptionObj = new JSONObject(new String(responseBody));
                    contentWarningDescription = contentWarningDescriptionObj.optString("response");
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set content warning settings page to display error message
                }

                // Done fetching content warning's description so populate the content warning's
                // settings page now
                contentWarningSettingsFragment.setupContentWarningSettingsPage(contentWarningDescription);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set content warning settings page view to an error screen with a reload button
            }
        });
    }

    public static void fetchContentWarningVoteStatus(final @NonNull ContentWarningFragment contentWarningFragment,
                                                     final @NonNull String contentWarningId) {
        // Setup URL to get user's voting status for this cw
        final String relativeUrl = "cw/" + contentWarningId + "/has-voted";
        final String fetchContentWarningVoteStatusUrl = getAbsoluteUrl(relativeUrl);

        // Hit API to get user's past voting status for this content warning
        client.get(fetchContentWarningVoteStatusUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                @Nullable ContentWarningVotingState contentWarningVoteStatus = null;

                try {
                    final JSONObject contentWarningVoteStatusObj = new JSONObject(new String(responseBody));
                    final @NonNull String contentWarningVoteStatusStr = contentWarningVoteStatusObj
                            .optString("response", "");

                    // Content warning voting status found for user
                    if (!contentWarningVoteStatusStr.isEmpty()) {
                        if (contentWarningVoteStatusStr.equals("upvoted")) {
                            contentWarningVoteStatus = ContentWarningVotingState.UPVOTED;
                        }
                        else if (contentWarningVoteStatusStr.equals("downvoted")) {
                            contentWarningVoteStatus = ContentWarningVotingState.DOWNVOTED;
                        }
                        else {
                            contentWarningVoteStatus = ContentWarningVotingState.NOT_VOTED;
                        }
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Upon failure set content warning details page view to an error screen with a reload button
                }

                // Done fetching content warning's voting status for user so properly initialize
                // the upvote/downvote buttons in the cw fragment now with this info
                contentWarningFragment.setupVotingButtons(contentWarningVoteStatus);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Upon failure set content warning details page view to an error screen with a reload button
            }
        });
    }

    public static void upvoteContentWarning(final @NonNull ContentWarningFragment contentWarningFragment,
                                            final @NonNull String contentWarningId) {
        // Don't let user try to upvote if pending upvote/downvote is still in progress for this
        // content warning
        if (previousCwVote != null && !previousCwVote.isFinished()) {
            // TODO: Notify user that upvoting failed
            contentWarningFragment.onUpvoteFinished(false);
            return;
        }

        // Setup URL to upvote this cw
        final String relativeUrl = "cw/" + contentWarningId + "/upvote";
        final String upvoteContentWarningUrl = getAbsoluteUrl(relativeUrl);

        // Hit API to try and upvote the content warning for this user
        previousCwVote = client.get(upvoteContentWarningUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                boolean upvoteSucceeded = false;

                try {
                    final JSONObject contentWarningUpvoteStatusObj = new JSONObject(new String(responseBody));
                    final String upvoteStatus = contentWarningUpvoteStatusObj.optString("response", "");

                    if (upvoteStatus.equals("Success")) {
                        upvoteSucceeded = true;
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Notify user that upvoting failed
                }

                // Let content warning fragment know whether or not upvote succeeded
                contentWarningFragment.onUpvoteFinished(upvoteSucceeded);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Notify user that upvoting failed
                contentWarningFragment.onUpvoteFinished(false);
            }
        });
    }

    public static void downvoteContentWarning(final @NonNull ContentWarningFragment contentWarningFragment,
                                              final @NonNull String contentWarningId) {
        // Don't let user try to downvote if pending upvote/downvote is still in progress for this
        // content warning
        if (previousCwVote != null && !previousCwVote.isFinished()) {
            // TODO: Notify user that downvoting failed
            contentWarningFragment.onDownvoteFinished(false);
            return;
        }

        // Setup URL to downvote this cw
        final String relativeUrl = "cw/" + contentWarningId + "/downvote";
        final String downvoteContentWarningUrl = getAbsoluteUrl(relativeUrl);

        // Hit API to try and downvote the content warning for this user
        previousCwVote = client.get(downvoteContentWarningUrl, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                boolean downvoteSucceeded = false;

                try {
                    final JSONObject contentWarningDownvoteStatusObj = new JSONObject(new String(responseBody));
                    final String downvoteStatus = contentWarningDownvoteStatusObj.optString("response", "");

                    if (downvoteStatus.equals("Success")) {
                        downvoteSucceeded = true;
                    }
                }
                catch (final JSONException e) {
                    Log.e("Backend: ", e.toString());
                    // TODO: Notify user that downvoting failed
                }

                // Let content warning fragment know whether or not downvote succeeded
                contentWarningFragment.onDownvoteFinished(downvoteSucceeded);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e("Backend: ", error.toString());
                // TODO: Notify user that downvoting failed
                contentWarningFragment.onDownvoteFinished(false);
            }
        });
    }
}
