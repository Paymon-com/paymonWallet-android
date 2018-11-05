package ru.paymon.android.components;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.Executors;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;


public class CustomDialogProgress extends Dialog {

    public CustomDialogProgress(final Context context, final String hint, final int resId) {
        super(context);
        getWindow().setBackgroundDrawable(new ColorDrawable(0xFF323232));

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom_progress, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.dialog_progress_money_image);
        TextView hintTextView = (TextView) view.findViewById(R.id.dialog_progress_money_hint);
        TextView loadTextView = (TextView) view.findViewById(R.id.dialog_progress_money_load);

        imageView.setImageResource(resId);
        hintTextView.setText(hint);
        loadTextView.setText(ApplicationLoader.applicationContext.getString(R.string.loading_one));

        setContentView(view);

        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                try {
                    ApplicationLoader.applicationHandler.post(()->loadTextView.setText(ApplicationLoader.applicationContext.getString(R.string.loading_two)));
                    Thread.sleep(500);
                    ApplicationLoader.applicationHandler.post(()->loadTextView.setText(ApplicationLoader.applicationContext.getString(R.string.loading_three)));
                    Thread.sleep(500);
                    ApplicationLoader.applicationHandler.post(()->loadTextView.setText(ApplicationLoader.applicationContext.getString(R.string.loading_one)));
                    Thread.sleep(500);
                } catch (Exception e) {

                }
            }
        });
    }

    @Override
    public void show() {
        WindowManager.LayoutParams lWindowParams = new WindowManager.LayoutParams();
        lWindowParams.copyFrom(getWindow().getAttributes());
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT; // this is where the magic happens
        lWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        super.show();
        getWindow().setAttributes(lWindowParams);
    }
}