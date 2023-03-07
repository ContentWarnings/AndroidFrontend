package com.example.moviementor.other;

import androidx.annotation.NonNull;

import java.util.List;

// Data class to hold onto the information for a single content warning
public class ContentWarning {
    private final @NonNull String id;
    private final @NonNull String contentWarningName;
    private final @NonNull String contentWarningDescription;
    private final @NonNull List<TimeStamp> timestampList;

    public ContentWarning(final @NonNull String id, final @NonNull String contentWarningName,
                           final @NonNull String contentWarningDescription,
                           final @NonNull List<TimeStamp> timestampList) {
        this.id = id;
        this.contentWarningName = contentWarningName;
        this.contentWarningDescription = contentWarningDescription;
        this.timestampList = timestampList;
    }

    @NonNull
    public String getContentWarningId() {
        return this.id;
    }

    @NonNull
    public String getContentWarningName() {
        return this.contentWarningName;
    }

    @NonNull
    public String getContentWarningDescription() {
        return this.contentWarningDescription;
    }

    @NonNull
    public List<TimeStamp> getTimestampList() {
        return this.timestampList;
    }
}
