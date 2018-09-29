package ru.paymon.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.navigation.Navigation;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.broadcastreceivers.NetworkStateReceiver;
import ru.paymon.android.net.NetworkManager;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NotificationManager.IListener {
    private static long back_pressed;
    private TextView textView;

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

        textView = findViewById(R.id.connecting_activity);

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

//    private void init(){
//        Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentPermissions.newInstance());
//
//        requestAppPermissions(new String[]{
//                        Manifest.permission.READ_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.VIBRATE,
//                        Manifest.permission.INTERNET,
//                        Manifest.permission.ACCESS_NETWORK_STATE},
//                R.string.msg_permissions_required, IMPORTANT_PERMISSIONS);
//    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.fragmentChats:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentChats);
                break;
            case R.id.fragmentContacts:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentContacts);
                break;
            case R.id.fragmentMoney:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoney);
                break;
            case R.id.fragmentMoreMenu:
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoreMenu);
                break;
        }

        return true;
    }

//    @Override
//    public void onBackPressed() {
//        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
//        if (fragment instanceof FragmentChats || fragment instanceof FragmentContacts || fragment instanceof FragmentMoney || fragment instanceof FragmentMoreMenu) {
//            if (back_pressed + 2000 > System.currentTimeMillis()) {
//                System.exit(0);
//            } else {
//                Toast.makeText(getBaseContext(), R.string.double_tap_to_close_the_app, Toast.LENGTH_LONG).show();
//            }
//            back_pressed = System.currentTimeMillis();
//        } else {
//            getSupportFragmentManager().popBackStack();
//        }
//    }


//    @Override
//    public void onPermissionsGranted(final int requestCode) {
//        switch (requestCode) {
//            case IMPORTANT_PERMISSIONS:
//                if (User.currentUser == null)
//                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentStart.newInstance());
//                else
//                    Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentLoader.newInstance());

//                NetworkManager.getInstance().connect();
//                final Intent connectorIntent = new Intent(ApplicationLoader.applicationContext, ConnectorService.class);
//                ApplicationLoader.applicationContext.startService(connectorIntent);
//                break;
//            case READ_CONTACTS_PERMISSION:
//                Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentContactsInvite.newInstance(), null);
//                break;
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyGuardActivity.showCD();
        NotificationManager.getInstance().addObserver(this,NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().addObserver(this,NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this,NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().removeObserver(this,NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if(event == NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED){
            textView.setVisibility(View.GONE);
        }else if(event == NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED){
            textView.setVisibility(View.VISIBLE);
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == 10) {
//            if (resultCode == RESULT_OK) {
//                init();
//            }
//        } else if (requestCode == 20) {
//            if (resultCode == RESULT_OK) {
//                User.CLIENT_SECURITY_PASSWORD_VALUE = null;
//                User.CLIENT_SECURITY_PASSWORD_HINT = null;
//                User.saveConfig();
//            }
//        } else {
//            List<Fragment> fragments = getSupportFragmentManager().getFragments();
//            for (Fragment fragment : fragments) {
//                if (fragment instanceof FragmentProfileEdit) {
//                    fragment.onActivityResult(requestCode, resultCode, data);
//                    break;
//                }
//                if (fragment instanceof FragmentGroupSettings) {
//                    fragment.onActivityResult(requestCode, resultCode, data);
//                    break;
//                }
//            }
//        }
//    }


}
