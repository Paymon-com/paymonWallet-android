package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

import ru.paymon.android.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        ImageButton buttonBack = (ImageButton) findViewById(R.id.toolbar_back_btn);
        buttonBack.setOnClickListener(view1 -> super.onBackPressed());

        final FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.animator.fade_to_back, R.animator.fade_to_up, R.animator.fade_to_back);
        fragmentTransaction.replace(R.id.settings_container, FragmentSettings.newInstance());
        fragmentTransaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
