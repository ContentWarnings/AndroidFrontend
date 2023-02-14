package com.example.moviementor.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.example.moviementor.R;
import com.example.moviementor.fragments.FeaturedFragment;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        setupNavigationFooter();

        // Fragment is only loaded into container when savedInstanceState is null. This ensures that
        // the home page fragment is added only once: when the main app activity is first created
        if (savedInstanceState == null) {
            loadFragment(FeaturedFragment.class);
        }
    }

    private void loadFragment(final Class fragmentClass) {
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragmentClass, null).commit();
    }

    private void setupNavigationFooter() {
        // Get the three navigation footer tabs
        final LinearLayout featuredTab = findViewById(R.id.featured_tab);
        final LinearLayout searchTab = findViewById(R.id.search_tab);
        final LinearLayout settingsTab = findViewById(R.id.settings_tab);

        featuredTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //loadFragment(FeaturedFragment.class);
            }
        });

        
    }
}
