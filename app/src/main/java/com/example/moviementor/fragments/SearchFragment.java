package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moviementor.R;
import com.example.moviementor.adapters.SearchPageAdapter;
import com.example.moviementor.models.GenreViewModel;
import com.example.moviementor.other.SpanSizeLookupWithHeader;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends BaseFragment {
    // Static array containing data for each genre's name
    private static final String[] genreNames = {"Action", "Adventure", "Animation", "Comedy",
            "Crime", "Documentary", "Drama", "Family", "Fantasy", "History", "Horror", "Music",
            "Mystery", "Romance", "Science Fiction", "TV Movie", "Thriller", "War", "Western"};
    // TODO: Replace temp image id with ids for each custom genre background image
    // Static array containing resource id for each genre's custom background image
    private static final int[] genreBackgroundImageIds = {R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background,
            R.drawable.temp_genre_background, R.drawable.temp_genre_background};

    private final @NonNull List<Object> genreList;

    public SearchFragment() {
        super(R.layout.search_fragment, Tab.SEARCH);
        this.genreList = createGenreList();
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSearchPage();
    }

    private void setupSearchPage() {
        // Initialize RecyclerView and its adapter
        final RecyclerView searchPageRecyclerView = requireView().findViewById(R.id.search_page_recycler_view);
        final SearchPageAdapter searchPageAdapter = new SearchPageAdapter(this.genreList);

        // Give the adapter an alpha value to apply on genre background images
        final int alphaValue = getResources().getInteger(R.integer.genre_background_image_alpha);
        searchPageAdapter.assignAlphaValueForGenreBackgroundImages(alphaValue);

        // Bind the adapter and a Linear Layout Manager to the RecyclerView
        searchPageRecyclerView.setAdapter(searchPageAdapter);
        searchPageRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    @NonNull
    private List<Object> createGenreList() {
        final List<Object> genreData = new ArrayList<>();

        // Go through static arrays of genre data and populate viewModel list
        for (int i = 0; i < genreNames.length; i++) {
            final @NonNull String genreName = genreNames[i];
            final @IdRes int genreBackgroundImageRes = genreBackgroundImageIds[i];

            final Object genreItem = new GenreViewModel(genreName, genreBackgroundImageRes);

            genreData.add(genreItem);
        }

        return genreData;
    }
}
