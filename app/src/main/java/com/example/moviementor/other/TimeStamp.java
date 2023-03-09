package com.example.moviementor.other;

import androidx.annotation.NonNull;

// Specifies a timestamp where a content warning occurred in the movie
public class TimeStamp {
    public static final int MISSING_TIME = -1;

    private final int startMin;
    private final int endMin;

    public TimeStamp(final int startMin, final int endMin) {
        this.startMin = startMin;
        this.endMin = endMin;
    }

    public int getStartMin() {
        return this.startMin;
    }

    public int getEndMin() {
        return this.endMin;
    }

    // Helper function to get start time in formatted string hh:mm
    @NonNull
    public String getStartTimeString() {
        final int hour = this.startMin / 60;
        final int min = this.startMin % 60;

        // Pad the hour and min string to two digits if needed
        final String hourStr = ((hour >= 0 && hour <= 9) ? "0" : "") + hour;
        final String minStr = ((min >= 0 && min <= 9) ? "0" : "") + min;

        return hourStr + ':' + minStr;
    }

    // Helper function to get end time in formatted string hh:mm
    @NonNull
    public String getEndTimeString() {
        final int hour = this.endMin / 60;
        final int min = this.endMin % 60;

        // Pad the hour and min string to two digits if needed
        final String hourStr = ((hour >= 0 && hour <= 9) ? "0" : "") + hour;
        final String minStr = ((min >= 0 && min <= 9) ? "0" : "") + min;

        return hourStr + ':' + minStr;
    }
}
