package com.example.moviementor.adapters;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviementor.R;
import com.example.moviementor.other.StreamingProvider;

import java.util.List;

public class StreamingProvidersAdapter extends RecyclerView.Adapter<StreamingProvidersAdapter.StreamingProviderViewHolder> {
    private final @NonNull List<StreamingProvider> streamingProviders;
    private final @Nullable Uri streamingUri;

    public StreamingProvidersAdapter(final @NonNull List<StreamingProvider> streamingProviders,
                                     final @Nullable Uri streamingUri) {
        this.streamingProviders = streamingProviders;
        this.streamingUri = streamingUri;
    }

    @Override
    public StreamingProviderViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate custom layout for singular streaming provider item
        final View streamingProviderView = inflater.inflate(R.layout.streaming_provider_item, parent, false);

        // Return new view holder for inflated stream provider item
        return new StreamingProviderViewHolder(streamingProviderView);
    }

    @Override
    public void onBindViewHolder(final @NonNull StreamingProviderViewHolder viewHolder, final int position) {

        final @NonNull StreamingProvider streamingProvider = this.streamingProviders.get(position);

        // If provider's image url is null (not available), then load the no image found placeholder
        // into this item's image
        if (streamingProvider.getProviderImageUrl() == null) {
            Glide.with(viewHolder.streamingProviderImage.getContext())
                    .load(R.drawable.no_image_placeholder)
                    .into(viewHolder.streamingProviderImage);
        }
        // Load provider's image from non-null url, but if error is encountered then just use the no
        // image found placeholder
        else {
            Glide.with(viewHolder.streamingProviderImage.getContext())
                    .load(streamingProvider.getProviderImageUrl().toString())
                    .error(R.drawable.no_image_placeholder)
                    .into(viewHolder.streamingProviderImage);
        }

        viewHolder.streamingProviderOption.setText(streamingProvider.getProviderOptionString());

        if (this.streamingUri != null) {
            // Setup click listener on the provider's image to open the movie's
            // corresponding TMDB streaming page if clicked on
            viewHolder.streamingProviderImage.setOnClickListener(view -> {
                // Launch activity to open streaming page in the browser
                final Intent streamingPageIntent = new Intent(Intent.ACTION_VIEW, this.streamingUri);
                viewHolder.streamingProviderImage.getContext().startActivity(streamingPageIntent);
            });

        }
    }

    // Returns the total number of streaming provider options for this movie
    @Override
    public int getItemCount() {
        return this.streamingProviders.size();
    }

    public class StreamingProviderViewHolder extends RecyclerView.ViewHolder {
        public final ImageView streamingProviderImage;
        public final TextView streamingProviderOption;

        public StreamingProviderViewHolder(final @NonNull View streamingProviderView) {
            super(streamingProviderView);
            this.streamingProviderImage = streamingProviderView.findViewById(R.id.streaming_provider_image);
            this.streamingProviderOption = streamingProviderView.findViewById(R.id.streaming_provider_option);
        }
    }
}
