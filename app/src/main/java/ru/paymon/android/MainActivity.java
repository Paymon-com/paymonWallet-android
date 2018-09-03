package ru.paymon.android;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.utils.AbsRuntimePermission;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChat;
import ru.paymon.android.view.FragmentChats;
import ru.paymon.android.view.FragmentContacts;
import ru.paymon.android.view.FragmentContactsInvite;
import ru.paymon.android.view.FragmentLoader;
import ru.paymon.android.view.FragmentMoney;
import ru.paymon.android.view.FragmentMoreMenu;
import ru.paymon.android.view.FragmentPermissions;
import ru.paymon.android.view.FragmentRegistrationEmailConfirmation;
import ru.paymon.android.view.FragmentStart;

import static ru.paymon.android.Config.IMPORTANT_PERMISSIONS;
import static ru.paymon.android.Config.READ_CONTACTS_PERMISSION;

public class MainActivity extends AbsRuntimePermission implements NotificationManager.IListener {
    private static long back_pressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (User.CLIENT_SECURITY_PASSWORD_VALUE != null) {
            Intent intent = new Intent(getApplicationContext(), KeyGuardActivity.class);
            intent.putExtra("request_code", 10);
            startActivityForResult(intent, 10);
            return;
        }

        init();
    }

    private void init() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        bottomNavigationView.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.bottom_menu_chats:
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.getInstance());
                    break;
                case R.id.bottom_menu_contacts:
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentContacts.getInstance());
                    break;
                case R.id.ic_menu_money:
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentMoney.newInstance());
                    break;
                case R.id.bottom_menu_more:
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentMoreMenu.getInstance());
                    break;
            }

            return true;
        });

        Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentPermissions.newInstance());

        requestAppPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.VIBRATE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE},
                R.string.msg_permissions_required, IMPORTANT_PERMISSIONS);
    }


    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof FragmentChats || fragment instanceof FragmentContacts || fragment instanceof FragmentMoney || fragment instanceof FragmentMoreMenu) {
            if (back_pressed + 2000 > System.currentTimeMillis()) {
                System.exit(0);
            } else {
                Toast.makeText(getBaseContext(), R.string.double_tap_to_close_the_app, Toast.LENGTH_LONG).show();
            }
            back_pressed = System.currentTimeMillis();
        } else {
            getSupportFragmentManager().popBackStack();
//            super.onBackPressed();
        }
    }


    @Override
    public void onPermissionsGranted(final int requestCode) {
        switch (requestCode) {
            case IMPORTANT_PERMISSIONS:
                if (User.currentUser == null)
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentStart.newInstance());
                else
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentLoader.newInstance());

//                NetworkManager.getInstance().connect();
                final Intent connectorIntent = new Intent(ApplicationLoader.applicationContext, ConnectorService.class);
                ApplicationLoader.applicationContext.startService(connectorIntent);
                break;
            case READ_CONTACTS_PERMISSION:
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentContactsInvite.newInstance(), null);
                break;
        }
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.userAuthorized) {
            if (!User.currentUser.confirmed || User.currentUser.email.isEmpty())
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentRegistrationEmailConfirmation.newInstance());
            else
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.getInstance());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        KeyGuardActivity.showCD();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.userAuthorized);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10) {
            if (resultCode == RESULT_OK) {
                init();
            }
        } else if (requestCode == 20) {
            if (resultCode == RESULT_OK) {
                User.CLIENT_SECURITY_PASSWORD_VALUE = null;
                User.CLIENT_SECURITY_PASSWORD_HINT = null;
                User.saveConfig();
            }
        }
    }
}
