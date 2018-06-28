package ru.paymon.android;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChats;
import ru.paymon.android.view.FragmentStart;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NotificationManager.IListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.checkDisplaySize(getApplicationContext(), null);
        Utils.maxSize = Utils.displaySize.x - Utils.displaySize.x / 100.0 * 45;
        Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentStart.newInstance(), null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int id = item.getItemId();
        Fragment pendingFragment = null;

        switch (id) {
            case R.id.nav_dialogs:
                pendingFragment = FragmentChats.getInstance();
                break;
            case R.id.nav_contacts:
                break;
            case R.id.nav_ico:
                break;
            case R.id.nav_profit:
                break;
            case R.id.nav_money:
                break;
            case R.id.nav_games:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_invite:
                break;
            case R.id.nav_faq:
                break;
            case R.id.nav_exit:
                break;
        }

        if (pendingFragment != null) {
//            drawer.closeDrawer(GravityCompat.START);
            Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), pendingFragment, null);
        }

        return true;
    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationManager.userAuthorized) {
            if (!User.currentUser.confirmed || User.currentUser.email.isEmpty()) {
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentRegistrationEmailConfirmation.getInstance(), null);
            } else {
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.getInstance(), null);
            }
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
