package ru.paymon.android.activities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.viewmodels.MainViewModel;

import static ru.paymon.android.activities.KeyGuardActivity.CD_TIME_KEY_GUARD;
import static ru.paymon.android.activities.KeyGuardActivity.LAST_TIME_KEY_GUARD_SHOWED;
import static ru.paymon.android.view.AbsFragmentChat.CHAT_ID_KEY;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    public static final String INTENT_ACTION_OPEN_CHAT = "INTENT_ACTION_OPEN_CHAT";
    public static final String IS_GROUP = "IS_GROUP";
    private long lastTimeBackPressed;
    private ConstraintLayout connectingConstraint;
    private MainViewModel mainViewModel;
    private LiveData<Boolean> connectionState;
    private LiveData<Boolean> networkState;
    private LiveData<Boolean> authorizationState;
    private LiveData<Boolean> btcSyncData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        connectionState = mainViewModel.getServerConnectionState();
        networkState = mainViewModel.getNetworkConnectionState();
        authorizationState = mainViewModel.getAuthorizationState();
        btcSyncData = mainViewModel.getBtcSync();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        connectingConstraint = findViewById(R.id.connecting_constraint);

        if (!User.currentUser.confirmed) {
            Intent intent = new Intent(getApplicationContext(), EmailConfirmationActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        final BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        Navigation.findNavController(this, R.id.nav_host_fragment).addOnNavigatedListener((controller, destination) -> {
            int id = destination.getId();
            if (id == R.id.fragmentChat || id == R.id.fragmentGroupChat) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            } else {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });

        final Observer<Boolean> stateObserver = state -> checkConnection();
        networkState.observe(this, stateObserver);
        connectionState.observe(this, stateObserver);
        authorizationState.observe(this, stateObserver);
        btcSyncData.observe(this, state -> checkBtcSync());
    }

    private void checkConnection() {
        Boolean nState = networkState.getValue();
        Boolean cState = connectionState.getValue();
        Boolean aState = authorizationState.getValue();
        if (nState == null || cState == null || aState == null) return;
        String text = getString(R.string.connecting);
        ((TextView) connectingConstraint.findViewById(R.id.textView3)).setText(text);
        if (nState && cState && aState)
            connectingConstraint.setVisibility(View.GONE);
        else {
            connectingConstraint.setVisibility(View.VISIBLE);
        }
    }

    private void checkBtcSync() {
        Boolean isBtsSync = btcSyncData.getValue();
        if (isBtsSync == null) return;
        String text = getString(R.string.btc_sync);
        ((TextView) connectingConstraint.findViewById(R.id.textView3)).setText(text);
        if (isBtsSync)
            connectingConstraint.setVisibility(View.GONE);
        else
            connectingConstraint.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        NavOptions.Builder builder = new NavOptions.Builder();
        NavOptions navOptions = builder.setLaunchSingleTop(true).setClearTask(true).build();
        final NavDestination currentNavigationDestination = Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination();
        if (currentNavigationDestination != null) {
            final int id = currentNavigationDestination.getId();

            switch (item.getItemId()) {
                case R.id.fragmentChats:
                    if (id == R.id.fragmentChats)
                        return false;
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentChats, null, navOptions);
                    break;
                case R.id.fragmentContacts:
                    if (id == R.id.fragmentContacts)
                        return false;
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentContacts, null, navOptions);
                    break;
                case R.id.fragmentMoney:
                    if (id == R.id.fragmentMoney)
                        return false;
                    if (User.CLIENT_SECURITY_PASSWORD_VALUE != null && (System.currentTimeMillis() - LAST_TIME_KEY_GUARD_SHOWED >= CD_TIME_KEY_GUARD)) {
                        Intent intent = new Intent(getApplicationContext(), KeyGuardActivity.class);
                        intent.putExtra("request_code", 10);
                        startActivityForResult(intent, 10);
                    }else {
                        Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoney, null, navOptions);
                    }
                    break;
                case R.id.fragmentMoreMenu:
                    if (id == R.id.fragmentMoreMenu)
                        return false;
                    Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoreMenu, null, navOptions);
                    break;
            }

            return true;
        }

        return false;
    }


    @Override
    public void onBackPressed() {
        final NavDestination currentNavigationDestination = Navigation.findNavController(this, R.id.nav_host_fragment).getCurrentDestination();
        if (currentNavigationDestination != null) {
            final int id = currentNavigationDestination.getId();
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
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (User.CLIENT_SECURITY_PASSWORD_VALUE != null && (System.currentTimeMillis() - LAST_TIME_KEY_GUARD_SHOWED >= CD_TIME_KEY_GUARD)) {
            Intent intent = new Intent(getApplicationContext(), KeyGuardActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(INTENT_ACTION_OPEN_CHAT, false)) {
            final boolean isGroup = intent.getBooleanExtra(IS_GROUP, false);
            final int cid = intent.getIntExtra(CHAT_ID_KEY, 0);
            final Bundle bundle = new Bundle();
            bundle.putInt(CHAT_ID_KEY, cid);
            if (isGroup) {
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentGroupChat, bundle);
            } else {
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentChat, bundle);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 10){
            if(resultCode == RESULT_OK){
                NavOptions.Builder builder = new NavOptions.Builder();
                NavOptions navOptions = builder.setLaunchSingleTop(true).setClearTask(true).build();
                Navigation.findNavController(this, R.id.nav_host_fragment).navigate(R.id.fragmentMoney, null, navOptions);
            }
        }
    }
}
