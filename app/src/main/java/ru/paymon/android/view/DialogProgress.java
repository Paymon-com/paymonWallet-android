package ru.paymon.android.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;

import ru.paymon.android.R;


public class DialogProgress extends Dialog {

    public DialogProgress(Context context) {
        super(context);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(view);
    }
}