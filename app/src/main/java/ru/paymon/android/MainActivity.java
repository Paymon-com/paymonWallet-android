package ru.paymon.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import ru.paymon.android.components.BottomNavigationViewHelper;
import ru.paymon.android.net.ConnectorService;
import ru.paymon.android.utils.AbsRuntimePermission;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentChats;
import ru.paymon.android.view.FragmentContactsInvite;
import ru.paymon.android.view.FragmentLoader;
import ru.paymon.android.view.FragmentMoreMenu;
import ru.paymon.android.view.FragmentPermissions;
import ru.paymon.android.view.FragmentRegistrationEmailConfirmation;
import ru.paymon.android.view.FragmentStart;

import static ru.paymon.android.Config.IMPORTANT_PERMISSIONS;
import static ru.paymon.android.Config.READ_CONTACTS_PERMISSION;

public class MainActivity extends AbsRuntimePermission implements NotificationManager.IListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.bottom_menu_chats:
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.getInstance());
                    break;
                case R.id.bottom_menu_contacts:
                    break;
                case R.id.ic_menu_money:
                    break;
                case R.id.bottom_menu_games:
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
    public void onPermissionsGranted(final int requestCode) {
        switch (requestCode){
            case IMPORTANT_PERMISSIONS:
                if (User.currentUser == null)
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentStart.newInstance());
                else
                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentLoader.newInstance());

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
                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentChats.newInstance());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.userAuthorized);
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
}
