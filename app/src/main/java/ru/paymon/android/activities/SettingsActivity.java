package ru.paymon.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;

import ru.paymon.android.R;
import ru.paymon.android.User;

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 20){
            if(resultCode == RESULT_OK){
                User.CLIENT_SECURITY_PASSWORD_VALUE = null;
                User.CLIENT_SECURITY_PASSWORD_HINT = null;
                User.saveConfig();
            }
        }
    }
}
