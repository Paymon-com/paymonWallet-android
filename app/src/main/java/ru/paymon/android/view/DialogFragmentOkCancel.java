//package ru.paymon.android.view;
//
//import android.app.Dialog;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.DialogFragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//
//public class DialogFragmentOkCancel extends DialogFragment implements NotificationManager.IListener {
//    private static String message;
//    private static View.OnClickListener okClick = null;
//    private static View.OnClickListener cancelClick = null;
//    private FragmentActivity activity;
//
//    public static void show(final FragmentManager fm, String msg, @Nullable View.OnClickListener clickOk, @Nullable View.OnClickListener clickCancel) {
//        final android.support.v4.app.DialogFragment newFragment = new CustomAlertDialogOkCancelFragment();
//        newFragment.show(fm, null);
//
//        if (clickOk != null) {
//            okClick = clickOk;
//        }
//
//        if (clickCancel != null) {
//            cancelClick = clickCancel;
//        }
//        message = msg;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        this.activity = getActivity();
//        NotificationManager.getInstance().addObserver(this, NotificationManager.cancelDialog);
//
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//
//        View view = LayoutInflater.from(activity).inflate(R.layout.fragment_dialog_custom_alert_ok_cancel, null);
//
//        Button ok = (Button) view.findViewById(R.id.fragment_dialog_custom_alert_ok_button);
//        Button cancel = (Button) view.findViewById(R.id.fragment_dialog_custom_alert_cancel_button);
//        TextView text = (TextView) view.findViewById(R.id.fragment_dialog_custom_alert_text_view);
//
//        text.setText(message);
//        if (okClick != null) {
//            ok.setOnClickListener(okClick);
//        } else {
//            ok.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    getDialog().dismiss();
//                }
//            });
//        }
//
//
//        if (cancelClick != null) {
//            cancel.setOnClickListener(cancelClick);
//        } else {
//            cancel.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    getDialog().dismiss();
//                }
//            });
//        }
//
//        final DialogBuilder builder = new DialogBuilder(activity);
//        builder.setView(view);
//
//        return builder.create();
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//        NotificationManager.getInstance().removeObserver(this, NotificationManager.cancelDialog);
//    }
//
//    @Override
//    public void didReceivedNotification(int id, Object... args) {
//        if (id == NotificationManager.cancelDialog) {
//            getDialog().dismiss();
//        }
//    }
//}
