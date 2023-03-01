package com.example.moviementor.other;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.example.moviementor.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AdvancedSearchOptionsModal extends BottomSheetDialogFragment {
    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable
            ViewGroup container, final @Nullable Bundle savedInstanceState)
    {
        final View advancedSearchOptionsModal = inflater.inflate(R.layout.advanced_search_options_modal,
                container, false);

        final ImageButton closeModalButton = advancedSearchOptionsModal
                .findViewById(R.id.close_advanced_options_menu_button);

        // When "x" button is pressed in top left corner of modal, then close
        // the modal
        closeModalButton.setOnClickListener(view -> dismiss());

        return advancedSearchOptionsModal;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Bug fix to make dialog fragment open fully in landscape mode
        final BottomSheetBehavior<View> view = BottomSheetBehavior.from((View) requireView().getParent());
        view.setState(BottomSheetBehavior.STATE_EXPANDED);
        view.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
    }
}
