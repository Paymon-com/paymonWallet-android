package ru.paymon.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;


import java.util.LinkedList;
import java.util.List;

import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChats;
import ru.paymon.android.view.FragmentStart;

import static ru.paymon.android.Config.KEY_CONFIRMED_EMAIL;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, NotificationManager.IListener {
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.checkDisplaySize(getApplicationContext(), null);
        Utils.maxSize = Utils.displaySize.x - Utils.displaySize.x / 100.0 * 45;

        NotificationManager.getInstance().addObserver(this, NotificationManager.userAuthorized);
        Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentStart.newInstance(), null);
//        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
//            @Override
//            public void onDrawerSlide(View drawerView, float slideOffset) {
//
//            }
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//
//            }
//
//            @Override
//            public void onDrawerStateChanged(int newState) {
//
//            }
//        });


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

            if (User.currentUser.email.isEmpty()) {
//                final FragmentEmailConfirmation fragmentEmailConfirmation = new FragmentEmailConfirmation();
//                Utils.replaceFragmentWithAnimationFade(getSupportFragmentManager(), fragmentEmailConfirmation, "confirm_email");
            } else {
                boolean confirmed = User.currentUser.confirmed;
                if (!confirmed) {
//                    final Bundle confirmedEmail = new Bundle();
//                    confirmedEmail.putString(KEY_CONFIRMED_EMAIL, User.currentUser.email);
//                    final FragmentEmailConfirmation fragmentEmailConfirmation = new FragmentEmailConfirmation();
//                    fragmentEmailConfirmation.setArguments(confirmedEmail);
//                    Utils.replaceFragmentWithAnimationFade(getSupportFragmentManager(), fragmentEmailConfirmation, "confirm_email");
                } else {
//                    if (isAuthorization) {
                        Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.getInstance(), null);
//                        isAuthorization = false;
//                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager.getInstance().addObserver(this, NotificationManager.userAuthorized);
    }
}
