package ru.paymon.android.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import ru.paymon.android.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        ImageButton buttonBack = (ImageButton) findViewById(R.id.toolbar_back_btn);
        buttonBack.setOnClickListener(view1 -> onBackPressed());
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
