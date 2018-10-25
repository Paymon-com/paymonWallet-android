package ru.paymon.android.view;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import ru.paymon.android.R;

public class FragmentSheetDialog extends BottomSheetDialogFragment {
    private LinearLayout buttonsAttachmentsInclude;

    public FragmentSheetDialog() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attachment, container, false);
        buttonsAttachmentsInclude = (LinearLayout) view.findViewById(R.id.buttons_attachments);

        ImageButton imageAttachButton = (ImageButton) view.findViewById(R.id.image_chat_attachment);
        ImageButton docAttachButton = (ImageButton) view.findViewById(R.id.document_chat_attachment);

        View navControllerView = view.findViewById(R.id.nav_attachment_fragment);
        NavController navController = Navigation.findNavController(navControllerView);
        imageAttachButton.setOnClickListener(v -> navController.navigate(R.id.fragmentImage));
        docAttachButton.setOnClickListener(v -> navController.navigate(R.id.fragmentDocPicker));

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        if (dialog != null) {
            View bottomSheet = dialog.findViewById(R.id.design_bottom_sheet);
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            View view = getView();
            view.post(() -> {
                View parent = (View) view.getParent();
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) (parent).getLayoutParams();
                CoordinatorLayout.Behavior behavior = params.getBehavior();
                BottomSheetBehavior bottomSheetBehavior = (BottomSheetBehavior) behavior;
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int screenHeight = (displaymetrics.heightPixels / 3) * 2;
                bottomSheetBehavior.setPeekHeight(screenHeight);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                dialog.dismiss();
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        buttonsAttachmentsInclude.animate().alpha(1 - slideOffset).setDuration(0).start();
                    }
                });
                ((View) bottomSheet.getParent()).setBackgroundColor(Color.TRANSPARENT);
            });
        }

    }


}
