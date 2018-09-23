package ru.paymon.android;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class StartActivity extends AppCompatActivity implements NotificationManager.IListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("AAA", "Start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this,NotificationManager.NotificationEvent.userAuthorized);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this,NotificationManager.NotificationEvent.userAuthorized);
    }

    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.userAuthorized) {
            if (!User.currentUser.confirmed || (User.currentUser.email != null && User.currentUser.email.isEmpty())) {
                Log.e("AAA", "NO");

            }
            else {
                Log.e("AAA", "YES");

//                Intent intent = new Intent(this, ChatsActivity.class);
//                startActivity(intent);
            }
        }
    }
}
