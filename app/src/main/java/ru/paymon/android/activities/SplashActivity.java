package ru.paymon.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ru.paymon.android.User;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        if (User.currentUser != null)
            intent = new Intent(this, MainActivity.class);
        else
            intent = new Intent(this, StartActivity.class);
        startActivity(intent);
        finish();
    }
}
