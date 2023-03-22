package com.example.moviementor.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviementor.R;
import com.example.moviementor.activities.MainActivity;
import com.example.moviementor.adapters.GenreTilesAdapter;
import com.example.moviementor.adapters.StreamingProvidersAdapter;
import com.example.moviementor.models.MovieViewModel;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.ContentWarning;
import com.example.moviementor.other.ContentWarningPrefsStorage;
import com.example.moviementor.other.ContentWarningPrefsStorage.ContentWarningVisibility;
import com.example.moviementor.other.TimeStamp;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MovieFragment extends BaseFragment {
    private static final String STORED_MOVIE_ID_KEY = "STORED_MOVIE_ID";
    private static final String STORED_MOVIE_NAME_KEY = "STORED_MOVIE_NAME";

    private static final int UNASSIGNED_MOVIE = -1;

    private int movieId;
    private @NonNull String movieName;
    private @NonNull Map<String, ContentWarningVisibility> cwPrefsMap;

    private @Nullable MovieViewModel movieData;
    private @Nullable List<ContentWarning> movieContentWarnings;

    public MovieFragment() {
        super(R.layout.movie_fragment, null);
        this.movieId = UNASSIGNED_MOVIE;
        this.movieData = null;
        this.movieContentWarnings = null;
    }

    // Called right after constructor when MovieFragment is created to give page initial data
    // to use
    public void assignMovie(final int movieId, final @NonNull String movieName) {
        this.movieId = movieId;
        this.movieName = movieName;
        this.cwPrefsMap = new HashMap<>();
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If movie page was recreated, then recover movie's id and name in saved instance state
        if (savedInstanceState != null) {
            this.movieId = savedInstanceState.getInt(STORED_MOVIE_ID_KEY);
            this.movieName = savedInstanceState.getString(STORED_MOVIE_NAME_KEY);
        }

        // Get up-to-date content warning preferences stored for the user
        final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                .getInstance(requireActivity());
        this.cwPrefsMap = cwPrefsStorage.getAllContentWarningPrefs();

        // Setup back button in header
        final ImageButton backButton = requireView().findViewById(R.id.movie_page_header_back_button);
        backButton.setOnClickListener(viewBackButton -> {
            requireActivity().onBackPressed();
        });

        // Set header's movie title initially while movie's data is being fetched
        // and the rest of the page is waiting to be populated
        final TextView moviePageTitle = requireView().findViewById(R.id.movie_page_title);
        moviePageTitle.setText(this.movieName);

        // Need to fetch movie's data from API to populate the page with
        if (this.movieData == null) {
            Backend.fetchMovie(this, this.movieId);
        }
        // Already have movie's data, so just populate the page with this data
        else {
            this.movieId = this.movieData.getMovieId();
            populateMoviePage(this.movieData);
        }
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);

        // Page is being shown again and movie's data has already been fetched from API
        if (!hidden && this.movieData != null) {
            // Get all current content warning preferences stored for the user
            final ContentWarningPrefsStorage cwPrefsStorage = ContentWarningPrefsStorage
                    .getInstance(requireActivity());
            final Map<String, ContentWarningPrefsStorage.ContentWarningVisibility> newCwPrefsMap =
                    cwPrefsStorage.getAllContentWarningPrefs();

            // Need to check if any content warning preferences have changed since reopening page,
            // so that the warning banner and warning icons can be adjusted if needed
            if (!this.cwPrefsMap.equals(newCwPrefsMap)) {
                this.cwPrefsMap = newCwPrefsMap;
                refreshWarnings();
            }
        }
    }

    // Called whenever user has modified their content warning preferences before re-opening this
    // page. This function makes any changes, if needed, so that the page will reflect the
    // user's most up-to-date content warning settings
    private void refreshWarnings() {
        // If the movie has not retrieved its data from the backend yet or if the movie does not
        // have any content warnings, then don't need to refresh anything on the page
        if (this.movieData == null || this.movieContentWarnings == null || this.movieContentWarnings.isEmpty()) {
            return;
        }

        final ViewGroup warningBanner = requireView().findViewById(R.id.movie_page_warning_banner);

        // Only display warning banner at the top of the page if this movie contains at least
        // one content warning that the user has set as WARN or HIDE
        if (shouldShowWarningBanner(this.movieContentWarnings)) {
            warningBanner.setVisibility(View.VISIBLE);
        }
        else {
            warningBanner.setVisibility(View.GONE);
        }

        // Re-organize content warnings for this movie by priority
        // (WARN/HIDE content warnings before SHOW content warnings)
        this.movieContentWarnings = getContentWarningListSortedByPriority(this.movieData.getContentWarnings());

        // Clear current list of content warnings being displayed
        final LinearLayout contentWarningsList = requireView().findViewById(R.id.movie_page_content_warnings_list);
        contentWarningsList.removeAllViews();

        final LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Re-create all the content warning rows for the movie's list of content warnings
        for (int i = 0; i < this.movieContentWarnings.size(); i++) {
            final @NonNull ContentWarning contentWarning = this.movieContentWarnings.get(i);

            // Inflate custom layout for singular content warning item
            final View contentWarningItemView = inflater.inflate(R.layout.content_warning_item, contentWarningsList, false);

            // Have each content warning warning row keep track of its position in the list
            contentWarningItemView.setTag(i);

            final ImageView warningIcon = contentWarningItemView
                    .findViewById(R.id.content_warning_item_warning_icon);
            final TextView contentWarningName = contentWarningItemView
                    .findViewById(R.id.content_warning_item_name);
            final TextView contentWarningTimestamp = contentWarningItemView
                    .findViewById(R.id.content_warning_item_timestamp);

            // Only display warning icon next to this content warning if user has set this
            // content warning's visibility as WARN or HIDE
            if (this.cwPrefsMap.getOrDefault(contentWarning.getContentWarningName(), ContentWarningVisibility.SHOW)
                    != ContentWarningVisibility.SHOW) {
                warningIcon.setVisibility(View.VISIBLE);
            }
            else {
                warningIcon.setVisibility(View.GONE);
            }

            contentWarningName.setText(contentWarning.getContentWarningName());

            // If single content warning has multiple timestamps reported, then just display
            // text that this content warning occurs multiple times in the movie
            if (contentWarning.getTimestampList().size() > 1) {
                contentWarningTimestamp.setText(
                        getString(R.string.content_warning_item_multiple_timestamps));
            }
            // Content warning only has one timestamp, so display this timestamp
            // in the content warning item
            else if (contentWarning.getTimestampList().size() == 1) {
                // Get first and only timestamp for content warning
                final TimeStamp cwTimestamp = contentWarning.getTimestampList().get(0);

                // Convert timestamp into string of format "hh:mm - hh:mm"
                final String timestampString = cwTimestamp.getStartTimeString() + " - "
                        + cwTimestamp.getEndTimeString();

                contentWarningTimestamp.setText(timestampString);
            }
            // Don't display anything for timestamp text if content warning does not have any
            // reported

            // Open content warning's full details page when clicked on
            contentWarningItemView.setOnClickListener(view -> {
                // Get position of content warning row that was clicked on in list
                final int position = (Integer) view.getTag();

                // Get full object for content warning row that was clicked on
                final @NonNull ContentWarning contentWarningData = this.movieContentWarnings.get(position);

                // Open the full details page for this content warning
                final MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.openContentWarningPage(contentWarningData);
            });

            // Append this content warning item to the list of content warnings at the bottom
            // of the page
            contentWarningsList.addView(contentWarningItemView);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STORED_MOVIE_ID_KEY, this.movieId);
        outState.putString(STORED_MOVIE_NAME_KEY, this.movieName);
    }

    // When movie fragment is removed from view hierarchy, un-assign its movie id, so that outdated
    // movie data fetched from the API does not try to populate page anymore
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.movieId = UNASSIGNED_MOVIE;
    }

    public void populateMoviePage(final @Nullable MovieViewModel movieData) {
        // If movie was not found or JSON returned for movie was invalid, then don't populate
        // this movie page
        if (movieData == null) {
            return;
        }
        // If movie fragment has been removed from view hierarchy before movie's data reaches here
        // to populate the page, then ignore this outdated movie data
        else if (this.movieId == UNASSIGNED_MOVIE) {
            return;
        }

        this.movieData = movieData;

        // Hide loading progress wheel since movie data is ready to populate the page
        final ProgressBar loadingProgressWheel = requireView().findViewById(R.id.loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);

        // Hold onto list of content warnings for this movie organized by priority
        // (WARN/HIDE content warnings before SHOW content warnings)
        this.movieContentWarnings =
                getContentWarningListSortedByPriority(movieData.getContentWarnings());

        final ViewGroup warningBanner = requireView().findViewById(R.id.movie_page_warning_banner);

        // Only display warning banner at the top of the page if this movie contains at least
        // one content warning that the user has set as WARN or HIDE
        if (shouldShowWarningBanner(this.movieContentWarnings)) {
            warningBanner.setVisibility(View.VISIBLE);
        }
        else {
            warningBanner.setVisibility(View.GONE);
        }

        // If movie's name fetched from API is different from movie name initially passed to the
        // movie page, then set header title of the page to the new movie's name
        if (!this.movieName.equals(movieData.getMovieName())) {
            final TextView moviePageTitle = requireView().findViewById(R.id.movie_page_title);
            moviePageTitle.setText(movieData.getMovieName());
        }

        final @Nullable URL movieImageUrl = movieData.getMovieImageUrl();
        final ImageView moviePageImage = requireView().findViewById(R.id.movie_page_image);

        // If movie's image url is null (not available), then load the no image found placeholder
        // into the movie image for this page
        if (movieImageUrl == null) {
            Glide.with(requireContext()).load(R.drawable.no_image_placeholder).into(moviePageImage);
        }
        // Load movie's image from non-null url, but if error is encountered then just use the no
        // image found placeholder
        else {
            Glide.with(requireContext()).load(movieImageUrl.toString())
                    .error(R.drawable.no_image_placeholder)
                    .into(moviePageImage);
        }

        final @Nullable Date releaseDate = movieData.getReleaseDate();
        final int runtime = movieData.getMovieRuntime();

        // If both release date and runtime are missing, then don't bother displaying
        // text for either
        if (releaseDate != null || runtime != MovieViewModel.MISSING_RUNTIME) {
            String dateAndRuntimeString = "";

            if (releaseDate != null) {
                dateAndRuntimeString += getDateString(releaseDate);
            }
            if (runtime != MovieViewModel.MISSING_RUNTIME) {
                // Add new lines if displaying both release date and runtime
                if (releaseDate != null) {
                    dateAndRuntimeString += '\n';
                }
                dateAndRuntimeString += getRuntimeString(runtime);
            }

            final TextView movieDateAndRuntimeText = requireView()
                    .findViewById(R.id.movie_page_date_and_runtime_text);
            movieDateAndRuntimeText.setText(dateAndRuntimeString);
        }

        final List<String> genres = movieData.getGenres();
        final LinearLayout genreTilesList = requireView().findViewById(R.id.movie_page_genre_tiles_list);

        // Create LayoutInflater to inflate genre tile views
        final LayoutInflater inflater = LayoutInflater.from(requireContext());

        // Create genre tile for each of this movie's listed genres and append it to the genre tile
        // list located in the upper right column of the page
        for (final @NonNull String genreName : genres) {
            // Get resource of icon for this genre
            final @DrawableRes int genreIconRes = GenreTilesAdapter.getGenreIconRes(genreName);

            // Inflate custom layout for singular genre tile
            final View genreTileView = inflater.inflate(R.layout.genre_tile_expanded, genreTilesList, false);

            final ImageView genreTileIcon = genreTileView.findViewById(R.id.genre_tile_icon);
            final TextView genreTileName = genreTileView.findViewById(R.id.genre_tile_name);

            // Populate this genre tile with the current genre's name and associated icon
            genreTileIcon.setImageResource(genreIconRes);
            genreTileName.setText(genreName);

            // Append this genre tile to the list of genre tiles on the movie page
            genreTilesList.addView(genreTileView);
        }

        final double movieRating = movieData.getMovieRating();

        // Display rating bar for movie only if rating value is available. Upcoming movies in TMDB
        // have a rating value of 0, so don't show rating bar for these movies either
        if (movieRating != MovieViewModel.MISSING_RATING && movieRating != 0) {
            final RatingBar movieRatingBar = requireView().findViewById(R.id.movie_page_rating_bar);

            // Original movie rating is out of 10 so convert to out of five so that five star
            // rating bar can be used
            final double movieFiveStarRating = movieRating / 2.0;

            // Fill rating stars for this movie
            movieRatingBar.setRating((float) movieFiveStarRating);

            // Make rating bar visible now that it has been set up
            movieRatingBar.setVisibility(View.VISIBLE);
        }

        // Setup empty RecyclerView for streaming providers
        final RecyclerView streamingProvidersRecyclerView = requireView()
                .findViewById(R.id.movie_page_streaming_providers_recycler_view);

        // Setup adapter for RecyclerView with movie's list of streaming providers and the
        // streaming link
        final StreamingProvidersAdapter streamingProvidersAdapter =
                new StreamingProvidersAdapter(movieData.getStreamingProviders(),
                        movieData.getStreamingUri());

        // Set RecyclerView's adapter and layout manager
        streamingProvidersRecyclerView.setAdapter(streamingProvidersAdapter);
        streamingProvidersRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));

        final @NonNull String movieMpaRating = movieData.getMovieMpaRating();

        // Display mpa rating for movie only if its available
        if (!movieMpaRating.isEmpty() && !movieMpaRating.equals(MovieViewModel.MISSSING_MPA_RATING)) {
            final @NonNull String mpaRatingDescription = getMpaRatingDescription(movieMpaRating);

            final TextView mpaRatingBox = requireView().findViewById(R.id.mpa_rating_box);
            final TextView mpaRatingDescriptionText = requireView().findViewById(R.id.mpa_rating_description);

            // Populate movie's mpa rating and its associated description on the page
            mpaRatingBox.setText(movieMpaRating);
            mpaRatingDescriptionText.setText(mpaRatingDescription);

            // Set the box background around MPA rating
            final Drawable mpaBoxBackground = ContextCompat.getDrawable(requireContext(), R.drawable.mpa_rating_box_background);
            mpaRatingBox.setBackground(mpaBoxBackground);
        }

        // Make summary header visible
        final TextView moviePageSummaryHeader = requireView().findViewById(R.id.movie_page_summary_header);
        moviePageSummaryHeader.setVisibility(View.VISIBLE);

        // Fill movie's summary on the page
        final TextView moviePageSummary = requireView().findViewById(R.id.movie_page_summary);
        moviePageSummary.setText(movieData.getMovieOverview());

        // Make content warnings header visible
        final TextView moviePageContentWarningsHeader = requireView().findViewById(R.id.movie_page_content_warnings_header);
        moviePageContentWarningsHeader.setVisibility(View.VISIBLE);

        final LinearLayout contentWarningsList = requireView().findViewById(R.id.movie_page_content_warnings_list);

        // Remove any old, stale views from content warnings list before adding all the
        // new content warning views to the list
        contentWarningsList.removeAllViews();

        // Create a content warning row for each of this movie's reported content warnings and
        // append each to the content warning list located at the bottom of the page
        for (int i = 0; i < this.movieContentWarnings.size(); i++) {
            final @NonNull ContentWarning contentWarning = this.movieContentWarnings.get(i);

            // Inflate custom layout for singular content warning item
            final View contentWarningItemView = inflater.inflate(R.layout.content_warning_item, contentWarningsList, false);

            // Have each content warning warning row keep track of its position in the list
            contentWarningItemView.setTag(i);

            final ImageView warningIcon = contentWarningItemView
                    .findViewById(R.id.content_warning_item_warning_icon);
            final TextView contentWarningName = contentWarningItemView
                    .findViewById(R.id.content_warning_item_name);
            final TextView contentWarningTimestamp = contentWarningItemView
                    .findViewById(R.id.content_warning_item_timestamp);

            // Only display warning icon next to this content warning if user has set this
            // content warning's visibility as WARN or HIDE
            if (this.cwPrefsMap.getOrDefault(contentWarning.getContentWarningName(), ContentWarningVisibility.SHOW)
                    != ContentWarningVisibility.SHOW) {
                warningIcon.setVisibility(View.VISIBLE);
            }
            else {
                warningIcon.setVisibility(View.GONE);
            }

            contentWarningName.setText(contentWarning.getContentWarningName());

            // If single content warning has multiple timestamps reported, then just display
            // text that this content warning occurs multiple times in the movie
            if (contentWarning.getTimestampList().size() > 1) {
                contentWarningTimestamp.setText(
                        getString(R.string.content_warning_item_multiple_timestamps));
            }
            // Content warning only has one timestamp, so display this timestamp
            // in the content warning item
            else if (contentWarning.getTimestampList().size() == 1) {
                // Get first and only timestamp for content warning
                final TimeStamp cwTimestamp = contentWarning.getTimestampList().get(0);

                // Convert timestamp into string of format "hh:mm - hh:mm"
                final String timestampString = cwTimestamp.getStartTimeString() + " - "
                        + cwTimestamp.getEndTimeString();

                contentWarningTimestamp.setText(timestampString);
            }
            // Don't display anything for timestamp text if content warning does not have any
            // reported

            // Open content warning's full details page when clicked on
            contentWarningItemView.setOnClickListener(view -> {
                // Get position of content warning row that was clicked on in list
                final int position = (Integer) view.getTag();

                // Get full object for content warning row that was clicked on
                final @NonNull ContentWarning contentWarningData = this.movieContentWarnings.get(position);

                // Open the full details page for this content warning
                final MainActivity mainActivity = (MainActivity) requireActivity();
                mainActivity.openContentWarningPage(contentWarningData);
            });

            // Append this content warning item to the list of content warnings at the bottom
            // of the movie page
            contentWarningsList.addView(contentWarningItemView);
        }

        // If no content warnings are reported for this movie currently, then display
        // text that explains so
        if (this.movieContentWarnings.isEmpty()) {
            final TextView noContentWarningsText = requireView().findViewById(R.id.no_content_warnings_found);
            noContentWarningsText.setVisibility(View.VISIBLE);
        }
    }

    // Helper function that decides whether or not warning banner should be displayed at the top of
    // the page based on this movie's content warnings and the user's content warning settings
    private boolean shouldShowWarningBanner(final @NonNull List<ContentWarning> contentWarnings) {
        for (final @NonNull ContentWarning contentWarning : contentWarnings) {
            // If movie has a content warning that user has flagged to be warned or hidden, then
            // warning banner needs to be shown
            if (this.cwPrefsMap.getOrDefault(contentWarning.getContentWarningName(), ContentWarningVisibility.SHOW)
                    != ContentWarningVisibility.SHOW) {
                return true;
            }
        }
        return false;
    }

    // Helper function that re-organizes a movie's content warning list such that all content
    // warnings that user has set to WARN or HIDE appear before any content warnings set to SHOW
    @NonNull
    private List<ContentWarning> getContentWarningListSortedByPriority(final @NonNull List<ContentWarning> contentWarnings) {
        final List<ContentWarning> warnHideContentWarnings = new ArrayList<>();
        final List<ContentWarning> showContentWarnings = new ArrayList<>();

        // Split movie's list of content warnings into two lists separated by whether or not their
        // visibility status is SHOW/HIDE or WARN
        for (final @NonNull ContentWarning contentWarning : contentWarnings) {
            if (this.cwPrefsMap.getOrDefault(contentWarning.getContentWarningName(), ContentWarningVisibility.SHOW)
                    == ContentWarningVisibility.SHOW) {
                showContentWarnings.add(contentWarning);
            }
            else {
                warnHideContentWarnings.add(contentWarning);
            }
        }

        // Append list of content warnings with SHOW visibility to list of content warnings
        // with WARN or HIDE visibility
        warnHideContentWarnings.addAll(showContentWarnings);

        return warnHideContentWarnings;
    }

    // Helper function to parse Java date objects into desired format for displaying on the
    // movie page
    @NonNull
    private String getDateString(final @NonNull Date date) {
        final SimpleDateFormat releaseDateTextFormat =
                new SimpleDateFormat("MMMM d, y", Locale.US);
        return releaseDateTextFormat.format(date);
    }

    // Helper function to get desired text format for displaying movie runtimes on the movie page
    @NonNull
    private String getRuntimeString(final int runtime) {
        final int runtimeHours = runtime / 60;
        final int runtimeMins = runtime % 60;

        return runtimeHours + "h " + runtimeMins + "m";
    }

    // Helper function to get brief description for each mpa rating category
    @NonNull
    private String getMpaRatingDescription(final @NonNull String mpaRating) {
        getResources().getString(R.string.home_page_header_title);

        switch (mpaRating) {
            case "G":
                return getResources().getString(R.string.mpa_g_rating);
            case "PG":
                return getResources().getString(R.string.mpa_pg_rating);
            case "PG-13":
                return getResources().getString(R.string.mpa_pg_13_rating);
            case "R":
                return getResources().getString(R.string.mpa_r_rating);
            case "NC-17":
                return getResources().getString(R.string.mpa_nc_17_rating);
            default:
                return getResources().getString(R.string.mpa_nr_rating);
        }
    }
}
