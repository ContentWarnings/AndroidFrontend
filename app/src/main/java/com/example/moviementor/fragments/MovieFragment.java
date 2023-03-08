package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.moviementor.R;
import com.example.moviementor.adapters.GenreTilesAdapter;
import com.example.moviementor.models.MovieViewModel;
import com.example.moviementor.other.Backend;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MovieFragment extends BaseFragment {
    private static final String STORED_MOVIE_ID_KEY = "STORED_MOVIE_ID";
    private static final String STORED_MOVIE_NAME_KEY = "STORED_MOVIE_NAME";

    private static final int UNASSIGNED_MOVIE = -1;

    private int movieId;
    private @NonNull String movieName;

    public MovieFragment() {
        super(R.layout.movie_fragment, null);
        this.movieId = UNASSIGNED_MOVIE;
    }

    // Called right after constructor when MovieFragment is created to give page initial data
    // to use
    public void assignMovie(final int movieId, final @NonNull String movieName) {
        this.movieId = movieId;
        this.movieName = movieName;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // If movie page was recreated, then recover movie's id and name in saved instance state
        if (savedInstanceState != null) {
            this.movieId = savedInstanceState.getInt(STORED_MOVIE_ID_KEY);
            this.movieName = savedInstanceState.getString(STORED_MOVIE_NAME_KEY);
        }

        // Setup back button in header
        final ImageButton backButton = requireView().findViewById(R.id.movie_page_header_back_button);
        backButton.setOnClickListener(viewBackButton -> {
            requireActivity().onBackPressed();
        });

        // Set header's movie title initially while movie's data is being fetched
        // and the rest of the page is waiting to be populated
        final TextView moviePageTitle = requireView().findViewById(R.id.movie_page_title);
        moviePageTitle.setText(this.movieName);

        // Fetch movie's data from API to populate the page
        Backend.fetchMovie(this, this.movieId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STORED_MOVIE_ID_KEY, this.movieId);
        outState.putString(STORED_MOVIE_NAME_KEY, this.movieName);
    }

    // When movie fragment is removed from view hierarchy, unassign its movie id, so that outdated
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

        // TODO: populate movie page with this movie's data
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
}
