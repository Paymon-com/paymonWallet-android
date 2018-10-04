package ru.paymon.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import androidx.navigation.Navigation;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NotificationManager.IListener {
//    private static long back_pressed;
    private ConstraintLayout connectingConstraint;

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

        connectingConstraint = findViewById(R.id.connecting_constraint);

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

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


    @Override
    protected void onResume() {
        super.onResume();
        KeyGuardActivity.showCD();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didDisconnectedFromTheServer);
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.didEstablishedSecuredConnection);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.NETWORK_STATE_CONNECTED || event == NotificationManager.NotificationEvent.didEstablishedSecuredConnection) {
            connectingConstraint.setVisibility(View.GONE);
        } else if (event == NotificationManager.NotificationEvent.NETWORK_STATE_DISCONNECTED || event == NotificationManager.NotificationEvent.didDisconnectedFromTheServer) {
            connectingConstraint.setVisibility(View.VISIBLE);
        }
    }
}
