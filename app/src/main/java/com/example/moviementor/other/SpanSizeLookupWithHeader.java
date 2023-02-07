package com.example.moviementor.other;

import androidx.recyclerview.widget.GridLayoutManager;

// Helper class that provides the number of columns each view item should span in a
// GridLayoutManager. This implementation is designed to support grid layouts that contain a header
// with individual view items underneath. The default implementation of this class just causes each
// item to only span 1 column.
public class SpanSizeLookupWithHeader extends GridLayoutManager.SpanSizeLookup {
    private final int numColumns;

    public SpanSizeLookupWithHeader(final int numColumns) {
        this.numColumns = numColumns;
    }

    // Returns the number of columns that the item at a specified position should span. The item at
    // position 0 is the header, so it should span all columns of a grid layout, taking up a single
    // row. Every other item should only span 1 column, taking up a singular spot in the grid.
    @Override
    public int getSpanSize(int position) {
        if (position == 0) {
            return this.numColumns;
        }
        else {
            return 1;
        }
    }
}
