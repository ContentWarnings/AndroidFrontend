package com.example.moviementor.other;

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
}
