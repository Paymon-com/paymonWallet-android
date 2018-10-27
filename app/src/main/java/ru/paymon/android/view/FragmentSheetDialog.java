package ru.paymon.android.view;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import ru.paymon.android.R;
import ru.paymon.android.filepicker.PickerManager;

public class FragmentSheetDialog extends BottomSheetDialogFragment {
    private LinearLayout buttonsAttachmentsInclude;
    Button button;
    float translation;

    public FragmentSheetDialog() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_attachments, container, false);
        buttonsAttachmentsInclude = (LinearLayout) view.findViewById(R.id.buttons_attachments);

        ImageButton imageAttachButton = (ImageButton) view.findViewById(R.id.image_chat_attachment);
        ImageButton docAttachButton = (ImageButton) view.findViewById(R.id.document_chat_attachment);

        button = (Button) view.findViewById(R.id.button_test_attach);

        button.setOnClickListener(v -> Toast.makeText(getContext(), "Click", Toast.LENGTH_SHORT).show());

        Fragment fragmentImage = new FragmentAttachmentImage();
        Fragment fragmentDocument = new FragmentAttachmentDocPicker();

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.attachment_container, fragmentImage);
        fragmentTransaction.commit();

        imageAttachButton.setOnClickListener(v -> {
            FragmentTransaction fragmentTransactionImage = getChildFragmentManager().beginTransaction();
            fragmentTransactionImage.replace(R.id.attachment_container, fragmentImage);
            fragmentTransactionImage.commit();
        });

        docAttachButton.setOnClickListener(v -> {
            FragmentTransaction fragmentTransactionDocument = getChildFragmentManager().beginTransaction();
            fragmentTransactionDocument.replace(R.id.attachment_container, fragmentDocument);
            fragmentTransactionDocument.commit();
        });

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
                translation = screenHeight - displaymetrics.heightPixels - (button.getHeight() / 2);
                button.setTranslationY(translation);
                bottomSheetBehavior.setPeekHeight(screenHeight);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                dialog.dismiss();
                                PickerManager.getInstance().clearSelections();
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        buttonsAttachmentsInclude.animate().alpha(1 - slideOffset).setDuration(0).start();
                        button.setTranslationY(translation * (slideOffset + 1));
                    }
                });
                ((View) bottomSheet.getParent()).setBackgroundColor(Color.TRANSPARENT);
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PickerManager.getInstance().clearSelections();
    }
}
