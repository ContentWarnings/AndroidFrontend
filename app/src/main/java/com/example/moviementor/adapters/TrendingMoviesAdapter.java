package com.example.moviementor.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.moviementor.R;
import com.example.moviementor.models.TrendingMovieViewModel;

import java.util.List;

public class TrendingMoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_ITEM = 2;

    private final @NonNull List<TrendingMovieViewModel> trendingMoviesList;
    private @Nullable OnItemClickListener listener;

    public TrendingMoviesAdapter(final @NonNull List<TrendingMovieViewModel> trendingMoviesList) {
        this.trendingMoviesList = trendingMoviesList;
        this.listener = null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_HEADER) {
            // Inflate view from header layout file
            final View headerView = inflater.inflate(R.layout.header, parent, false);

            // Get the current layout parameters from the parent RecyclerView
            final RelativeLayout.LayoutParams rvParams = (RelativeLayout.LayoutParams) parent.
                    findViewById(R.id.trending_movies_recycler_view).getLayoutParams();

            // Get the left and right margin from the RecyclerView's layout parameters
            final int leftMargin = rvParams.leftMargin;
            final int rightMargin = rvParams.rightMargin;

            // Get the current layout parameters for the header
            final RecyclerView.LayoutParams headerParams = (RecyclerView.LayoutParams) headerView.getLayoutParams();

            // Set the header's left and right margins equal to the negative left and right margins
            // of the parent RecyclerView. This effectively cancels out any horizontal margins on
            // the header view
            headerParams.setMargins(-leftMargin, 0, -rightMargin, 0);
            headerView.setLayoutParams(headerParams);

            // Return new view holder for inflated header
            return new TrendingMoviesAdapter.HeaderViewHolder(headerView);
        }

        // Inflate custom layout for singular trending movie
        final View trendingMovieItemView = inflater.inflate(R.layout.trending_movie_item, parent, false);

        // Return new view holder for inflated movie item
        return new ItemViewHolder(trendingMovieItemView);
    }

    @Override
    public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_HEADER) {
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;

            // Bind this page's header title to the header view generated by the RecyclerView
            headerViewHolder.headerTitle.setText(R.string.home_page_header_title);
        }
        else {
            final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;

            // Get the current movie data at specified position in list. Offset position by -1
            // since header is at position 0 in the RecyclerView
            final @NonNull TrendingMovieViewModel movieData = this.trendingMoviesList.get(position - 1);

            // Bind this movie's name to the movie item view's TextView
            itemViewHolder.trendingMovieName.setText(movieData.getMovieName());

            // If movie's image url is null (not available), then load the no image found placeholder
            // into this item's movie image
            if (movieData.getMovieImageUrl() == null) {
                Glide.with(itemViewHolder.trendingMovieImage.getContext())
                        .load(R.drawable.no_image_placeholder)
                        .into(itemViewHolder.trendingMovieImage);
            }
            // Load movie's image from non-null url, but if error is encountered then just use the no
            // image found placeholder
            else {
                Glide.with(itemViewHolder.trendingMovieImage.getContext())
                        .load(movieData.getMovieImageUrl().toString())
                        .error(R.drawable.no_image_placeholder)
                        .into(itemViewHolder.trendingMovieImage);
            }

            final int movieId = movieData.getMovieId();

            // Setup click listener to open this movie's full page if clicked on
            itemViewHolder.itemView.setOnClickListener(view -> {
                if (this.listener != null) {
                    listener.onMovieItemClick(movieId);
                }
            });
        }
    }

    // Returns the total number of trending movies received from the database plus 1 for the header
    @Override
    public int getItemCount() {
        return this.trendingMoviesList.size() + 1;
    }

    // Returns whether view is of type header or movie item.
    @Override
    public int getItemViewType(final int position) {
        // First view is the header, all other views are for movie items
        return (position == 0) ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }

    // Assign listener from parent fragment to this adapter
    public void setOnItemClickListener(final @NonNull OnItemClickListener listener) {
        this.listener = listener;
    }

    // Define listener interface for parent fragment to use if it wants to listen to an event
    // in the RecyclerView. Less prone to memory leaks in comparison to holding onto a reference
    // to the parent fragment in this adapter
    public interface OnItemClickListener {
        void onHeaderSearchButtonClick();
        void onMovieItemClick(final int movieId);
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView headerTitle;
        public final ImageButton headerSearchButton;

        public HeaderViewHolder(final @NonNull View headerView) {
            super(headerView);
            this.headerTitle = headerView.findViewById(R.id.header_title);
            this.headerSearchButton = headerView.findViewById(R.id.header_search_button);

            // Set up click listener on header's search button to call parent fragment's
            // listener function if click detected and listener is currently attached
            headerSearchButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onHeaderSearchButtonClick();
                }
            });
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        public final TextView trendingMovieName;
        public final ImageView trendingMovieImage;

        public ItemViewHolder(final @NonNull View trendingMovieItemView) {
            super(trendingMovieItemView);
            this.trendingMovieName = trendingMovieItemView.findViewById(R.id.trending_movie_name);
            this.trendingMovieImage = trendingMovieItemView.findViewById(R.id.trending_movie_image);
        }
    }
}
