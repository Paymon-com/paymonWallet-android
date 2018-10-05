package ru.paymon.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NotificationManager.IListener {
    private long lastTimeBackPressed;
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

        Navigation.findNavController(this, R.id.nav_host_fragment).addOnNavigatedListener((controller, destination) -> {
            int id = destination.getId();
            if(id == R.id.fragmentChat || id == R.id.fragmentGroupChat){
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            }else{
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavOptions.Builder builder = new NavOptions.Builder();
        NavOptions navOptions = builder.setLaunchSingleTop(true).setClearTask(true).build();

        switch (item.getItemId()) {
            case R.id.fragmentChats:
                if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.fragmentChats)
                    return false;
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentChats, null, navOptions);
                break;
            case R.id.fragmentContacts:
                if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.fragmentContacts)
                    return false;
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentContacts, null, navOptions);
                break;
            case R.id.fragmentMoney:
                if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.fragmentMoney)
                    return false;
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoney, null, navOptions);
                break;
            case R.id.fragmentMoreMenu:
                if (Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId() == R.id.fragmentMoreMenu)
                    return false;
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoreMenu, null, navOptions);
                break;
        }

        return true;
    }


    @Override
    public void onBackPressed() {
        int id = Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination().getId();
        if (id == R.id.fragmentChats || id == R.id.fragmentContacts || id == R.id.fragmentMoney || id == R.id.fragmentMoreMenu) {
            if (lastTimeBackPressed + 2000 > System.currentTimeMillis()) {
                System.exit(0);
            } else {
                Toast.makeText(getBaseContext(), R.string.double_tap_to_close_the_app, Toast.LENGTH_LONG).show();
            }
            lastTimeBackPressed = System.currentTimeMillis();
        } else {
            Navigation.findNavController(this, R.id.nav_host_fragment).popBackStack();
        }
    }


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
