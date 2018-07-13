package ru.paymon.android;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import ru.paymon.android.utils.Utils;
import ru.paymon.android.view.FragmentKeyGuard;

public class KeyGuardActivity extends AppCompatActivity {
    public static long LAST_TIME_KEY_GUARD_SHOWED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_key_guard);

        Utils.replaceFragmentWithAnimationSlideFade(getSupportFragmentManager(), FragmentKeyGuard.newInstance());
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        LAST_TIME_KEY_GUARD_SHOWED = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LAST_TIME_KEY_GUARD_SHOWED = System.currentTimeMillis();
    }
}
