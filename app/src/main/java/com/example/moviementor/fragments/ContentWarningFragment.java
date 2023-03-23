package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.other.Backend;
import com.example.moviementor.other.ContentWarning;
import com.example.moviementor.other.TimeStamp;

import java.util.ArrayList;
import java.util.List;

public class ContentWarningFragment extends BaseFragment {
    private static final float UNSELECTED_BUTTON_ALPHA = 1.0f;
    private static final float SELECTED_BUTTON_ALPHA = 0.5f;

    private static final String STORED_CW_ID = "CW_ID";
    private static final String STORED_CW_NAME = "CW_NAME";
    private static final String STORED_CW_SUMMARY = "CW_SUMMARY";
    private static final String STORED_CW_TIMESTAMPS = "CW_TIMESTAMPS";

    public enum ContentWarningVotingState {
        NOT_VOTED,
        UPVOTED,
        DOWNVOTED
    }

    private @Nullable ContentWarning contentWarning;
    private @Nullable ContentWarningVotingState contentWarningVotingState;

    public ContentWarningFragment() {
        super(R.layout.content_warning_fragment, null);
        this.contentWarning = null;
        this.contentWarningVotingState = null;
    }

    // Called right after constructor when ContentWarningFragment is created to give page content
    // warning data to populate page with
    public void assignContentWarning(final @NonNull ContentWarning contentWarning) {
        this.contentWarning = contentWarning;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Recover content warning data from saved instance state if available
        if (savedInstanceState != null) {
            recoverContentWarning(savedInstanceState);
        }

        // If content warning page has no access to content warning data even from
        // savedInstanceState, then just leave page empty
        if (this.contentWarning == null) {
            return;
        }

        // Setup back button in header
        final ImageButton backButton = view
                .findViewById(R.id.content_warning_page_header_back_button);
        backButton.setOnClickListener(viewBackButton -> {
            requireActivity().onBackPressed();
        });

        // Set header's title with name of this content warning prefixed by "CW:"
        final TextView contentWarningPageTitle = view
                .findViewById(R.id.content_warning_page_title);
        final String cwPageTitleText = getString(R.string.content_warning_page_title_prefix) + " "
                + this.contentWarning.getContentWarningName();
        contentWarningPageTitle.setText(cwPageTitleText);

        final LinearLayout timestampsList = view.findViewById(R.id.content_warning_timestamps_list);
        final List<TimeStamp> timeStamps = this.contentWarning.getTimestampList();

        // Determine whether or not multiple timestamps need to be displayed for this movie
        boolean multipleTimestamps = timeStamps.size() > 1;

        // Display all available timestamps for this content warning on the page
        for (final @NonNull TimeStamp timeStamp : timeStamps) {
            final TextView timeStampText = new TextView(view.getContext());

            // Convert timestamp into string of format "hh:mm - hh:mm"
            final String timestampString = timeStamp.getStartTimeString() + " to "
                    + timeStamp.getEndTimeString();

            timeStampText.setText(timestampString);

            timeStampText.setTextColor(getResources()
                    .getColor(R.color.content_warning_page_timestamp_text, null));

            // Adjust text size based on whether or not multiple timestamps need to be show for
            // content warning
            if (multipleTimestamps) {
                timeStampText.setTextSize(getResources()
                        .getDimension(R.dimen.content_warning_page_multiple_timestamps_text_size));
            }
            else {
                timeStampText.setTextSize(getResources()
                        .getDimension(R.dimen.content_warning_page_single_timestamp_text_size));
            }

            // Make sure timestamp is centered horizontally on the page
            timeStampText.setGravity(Gravity.CENTER_HORIZONTAL);

            timestampsList.addView(timeStampText);
        }

        // Populate the content warning's summary, but display message indicating that summary text
        // is empty if applicable
        final TextView contentWarningSummaryText = view
                .findViewById(R.id.content_warning_page_summary);
        final String contentWarningSummary = (!this.contentWarning.getContentWarningDescription().isEmpty()) ?
                this.contentWarning.getContentWarningDescription() : getString(R.string.no_content_warning_summary);
        contentWarningSummaryText.setText(contentWarningSummary);

        // Find out if/what user has voted for this content warning before, so that upvote/downvote
        // buttons can be setup to reflect how the user voted for this content warning previously
        Backend.fetchContentWarningVoteStatus(this, this.contentWarning.getContentWarningId());
    }

    @Override
    public void onHiddenChanged(final boolean hidden) {
        super.onHiddenChanged(hidden);

        // Page is being shown again and have access to content warning data
        if (!hidden && this.contentWarning != null) {
            // User may have changed their vote for this content warning in another page, so need
            // to hide the cw voting buttons until user's most recent voting status is acquired
            final ViewGroup contentWarningVotingButtons = requireView()
                    .findViewById(R.id.content_warning_voting_buttons);
            contentWarningVotingButtons.setVisibility(View.GONE);

            final ViewGroup upvoteButton = requireView()
                    .findViewById(R.id.content_warning_upvote_button);
            final ViewGroup downvoteButton = requireView()
                    .findViewById(R.id.content_warning_downvote_button);

            // Make both buttons appear unselected again until the user's voting status for this
            // content warning is retrieved
            upvoteButton.setAlpha(UNSELECTED_BUTTON_ALPHA);
            downvoteButton.setAlpha(UNSELECTED_BUTTON_ALPHA);

            // Display loading progress wheel at bottom of page until voting buttons are ready to
            // be displayed
            final ProgressBar loadingProgressWheel = requireView()
                    .findViewById(R.id.voting_buttons_loading_circle);
            loadingProgressWheel.setVisibility(View.VISIBLE);

            Backend.fetchContentWarningVoteStatus(this, this.contentWarning.getContentWarningId());
        }
    }

    // Called by API to setup initial state of buttons based off whatever the user has voted
    // for this content warning previously
    public void setupVotingButtons(final @Nullable ContentWarningVotingState cwVotingState) {
        // Could not find whether or not user has previously voted for this content warning, so
        // don't display voting buttons to user
        if (cwVotingState == null) {
            return;
        }
        // If content warning fragment has been removed from view hierarchy before setting up the
        // voting buttons, then ignore this outdated attempt
        else if (this.contentWarning == null) {
            return;
        }

        // Hold onto user's previous voting status for this content warning
        this.contentWarningVotingState = cwVotingState;

        final ViewGroup upvoteButton = requireView().findViewById(R.id.content_warning_upvote_button);
        final ViewGroup downvoteButton = requireView().findViewById(R.id.content_warning_downvote_button);

        // Only need to set initial state of voting buttons if user has voted for this cw before
        if (cwVotingState == ContentWarningVotingState.UPVOTED) {
            upvoteButton.setAlpha(SELECTED_BUTTON_ALPHA);
        }
        else if (cwVotingState == ContentWarningVotingState.DOWNVOTED) {
            downvoteButton.setAlpha(SELECTED_BUTTON_ALPHA);
        }

        upvoteButton.setOnClickListener(view -> {
            // Don't let user upvote the content warning twice
            if (this.contentWarningVotingState == ContentWarningVotingState.UPVOTED) {
                return;
            }

            // Otherwise, notify backend that user is upvoting this content warning
            Backend.upvoteContentWarning(this, this.contentWarning.getContentWarningId());
        });

        downvoteButton.setOnClickListener(view -> {
            // Don't let user downvote the content warning twice
            if (this.contentWarningVotingState == ContentWarningVotingState.DOWNVOTED) {
                return;
            }

            // Otherwise, notify backend that user is downvoting this content warning
            Backend.downvoteContentWarning(this, this.contentWarning.getContentWarningId());
        });

        // Hide loading progress wheel at bottom of page since voting buttons are ready
        // to be displayed
        final ProgressBar loadingProgressWheel = requireView()
                .findViewById(R.id.voting_buttons_loading_circle);
        loadingProgressWheel.setVisibility(View.GONE);

        // Make voting buttons visible with their initial state
        final LinearLayout contentWarningVotingButtons = requireView()
                .findViewById(R.id.content_warning_voting_buttons);
        contentWarningVotingButtons.setVisibility(View.VISIBLE);
    }

    // Get results of whether upvote succeeded or not so voting buttons can be updated on success
    public void onUpvoteFinished(final boolean upvoteSucceeded) {
        // If content warning fragment has been removed from view hierarchy before retrieving result
        // of upvote request, then ignore this outdated request
        if (this.contentWarning == null) {
            return;
        }
        // Couldn't fulfill this specific upvote request, so don't do anything
        else if (!upvoteSucceeded) {
            // TODO: Display dismissable message to user that cw could not be upvoted at this time
            return;
        }

        // Set user's voting status to UPVOTED
        this.contentWarningVotingState = ContentWarningVotingState.UPVOTED;

        final ViewGroup upvoteButton = requireView().findViewById(R.id.content_warning_upvote_button);
        final ViewGroup downvoteButton = requireView().findViewById(R.id.content_warning_downvote_button);

        // Make upvote button slightly opaque and downvote button fully visible to signify to user
        // that upvote button is currently selected
        upvoteButton.setAlpha(SELECTED_BUTTON_ALPHA);
        downvoteButton.setAlpha(UNSELECTED_BUTTON_ALPHA);
    }

    // Get results of whether downvote succeeded or not so voting buttons can be updated on success
    public void onDownvoteFinished(final boolean downvoteSucceeded) {
        // If content warning fragment has been removed from view hierarchy before retrieving result
        // of downvote request, then ignore this outdated request
        if (this.contentWarning == null) {
            return;
        }
        // Couldn't fulfill this specific downvote request, so don't do anything
        else if (!downvoteSucceeded) {
            // TODO: Display dismissable message to user that cw could not be downvoted at this time
            return;
        }

        // Set user's voting status to DOWNVOTED
        this.contentWarningVotingState = ContentWarningVotingState.DOWNVOTED;

        final ViewGroup upvoteButton = requireView().findViewById(R.id.content_warning_upvote_button);
        final ViewGroup downvoteButton = requireView().findViewById(R.id.content_warning_downvote_button);

        // Make downvote button slightly opaque and upvote button fully visible to signify to user
        // that downvote button is currently selected
        downvoteButton.setAlpha(SELECTED_BUTTON_ALPHA);
        upvoteButton.setAlpha(UNSELECTED_BUTTON_ALPHA);
    }

    // Store all of content warning's data in case of configuration change
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        if (this.contentWarning == null) {
            return;
        }

        outState.putString(STORED_CW_ID, this.contentWarning.getContentWarningId());
        outState.putString(STORED_CW_NAME, this.contentWarning.getContentWarningName());
        outState.putString(STORED_CW_SUMMARY, this.contentWarning.getContentWarningDescription());

        final ArrayList<String> timeStampStrings = new ArrayList<>();

        for (final @NonNull TimeStamp timeStamp : this.contentWarning.getTimestampList()) {
            final String timeStampStr = timeStamp.getStartTimeString() + "-" + timeStamp.getEndTimeString();
            timeStampStrings.add(timeStampStr);
        }

        outState.putStringArrayList(STORED_CW_TIMESTAMPS, timeStampStrings);
    }

    // Helper function to parse through and recover content warning from savedInstanceState
    private void recoverContentWarning(final @NonNull Bundle savedInstanceState) {
        final String cwId = savedInstanceState.getString(STORED_CW_ID);
        final String cwName = savedInstanceState.getString(STORED_CW_NAME);
        final String cwSummary = savedInstanceState.getString(STORED_CW_SUMMARY);
        final List<String> timeStampStrings = savedInstanceState.getStringArrayList(STORED_CW_TIMESTAMPS);

        if (cwId != null && cwName != null && cwSummary != null &&timeStampStrings != null) {
            final List<TimeStamp> cwTimeStamps = new ArrayList<>();

            for (final @Nullable String timeStampStr : timeStampStrings) {
                if (timeStampStr == null) {
                    continue;
                }

                final String[] startAndEndTimeStr = timeStampStr.split("-");

                if (startAndEndTimeStr.length != 2) {
                    continue;
                }

                int startTime, endTime;

                try {
                    final int startTimeHour = Integer.parseInt(startAndEndTimeStr[0].substring(0, 2));
                    final int startTimeMin = Integer.parseInt(startAndEndTimeStr[0].substring(3, 5));

                    final int endTimeHour = Integer.parseInt(startAndEndTimeStr[1].substring(0, 2));
                    final int endTimeMin = Integer.parseInt(startAndEndTimeStr[1].substring(3, 5));

                    startTime = startTimeHour * 60 + startTimeMin;
                    endTime = endTimeHour * 60 + endTimeMin;
                }
                catch (final NumberFormatException e) {
                    continue;
                }

                cwTimeStamps.add(new TimeStamp(startTime, endTime));
            }

            this.contentWarning = new ContentWarning(cwId, cwName, cwSummary, cwTimeStamps);
        }
    }
}
