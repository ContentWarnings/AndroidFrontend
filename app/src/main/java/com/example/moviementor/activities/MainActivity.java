package com.example.moviementor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.moviementor.R;
import com.example.moviementor.fragments.FeaturedFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Fragment is only loaded into container when savedInstanceState is null. This ensures that
        // the home page fragment is added only once: when the main app activity is first created
        if (savedInstanceState == null) {
            loadFragment(FeaturedFragment.class);
        }
    }

    private void loadFragment(final Class fragmentClass) {
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .add(R.id.fragment_container, fragmentClass, null).commit();
    }
}
