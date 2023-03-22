package com.example.moviementor.fragments;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.example.moviementor.other.ContentWarning;
import com.example.moviementor.other.TimeStamp;

import java.util.List;

public class ContentWarningFragment extends BaseFragment {
    private static final float UNSELECTED_BUTTON_ALPHA = 1.0f;
    private static final float SELECTED_BUTTON_ALPHA = 0.5f;

    private @Nullable ContentWarning contentWarning;

    public ContentWarningFragment() {
        super(R.layout.content_warning_fragment, null);
        this.contentWarning = null;
    }

    // Called right after constructor when ContentWarningFragment is created to give page content
    // warning data to populate page with
    public void assignContentWarning(final @NonNull ContentWarning contentWarning) {
        this.contentWarning = contentWarning;
    }

    @Override
    public void onViewCreated(final @NonNull View view, final @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // When content warning page is recreated due to a configuration change, then just go
        // back to previous page since the content warning data is lost on this page
        if (this.contentWarning == null) {
            requireActivity().onBackPressed();
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

        final RadioGroup contentWarningVotingButtons = view.findViewById(R.id.content_warning_voting_group);


        contentWarningVotingButtons.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            // Get the RadioButton whose checked state was modified
            final RadioButton voteButton = radioGroup.findViewById(checkedId);

            // Don't need to do anything when RadioButton is deselected
            if (!voteButton.isChecked()) {
                return;
            }

            // Get id of vote button that user checked
            final @IdRes int selectedVoteButtonId = voteButton.getId();

            // Get id of other vote button that was not pressed
            @IdRes int otherVoteButtonId;
            if (selectedVoteButtonId == R.id.content_warning_upvote_button) {
                otherVoteButtonId = R.id.content_warning_downvote_button;
            }
            else {
                otherVoteButtonId = R.id.content_warning_upvote_button;
            }

            final RadioButton otherVoteButton = requireView().findViewById(otherVoteButtonId);

            // Make vote button that was checked have a pressed, opaque appearance
            voteButton.setAlpha(SELECTED_BUTTON_ALPHA);

            // Make the other vote button fully visible again, signifying that it can be pressed now
            otherVoteButton.setAlpha(UNSELECTED_BUTTON_ALPHA);
        });
    }
}
