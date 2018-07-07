package ru.paymon.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChats;
import ru.paymon.android.view.FragmentLoader;
import ru.paymon.android.view.FragmentRegistrationEmailConfirmation;
import ru.paymon.android.view.FragmentStart;

public class MainActivity extends AppCompatActivity implements NotificationManager.IListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (User.currentUser == null)
            Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentStart.newInstance());
        else
            Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentLoader.newInstance());
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.userAuthorized) {
            if (!User.currentUser.confirmed || User.currentUser.email.isEmpty())
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentRegistrationEmailConfirmation.newInstance());
            else
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.newInstance());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.userAuthorized);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.userAuthorized);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
