package ru.paymon.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.net.RPC;

public class Utils {
    public static volatile DispatchQueue stageQueue = new DispatchQueue("stageQueue");
    public static volatile DispatchQueue netQueue = new DispatchQueue("netQueue");
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static boolean usingHardwareInput;
    public static float density = 1;
    public static Point displaySize = new Point();
    public static DisplayMetrics displayMetrics = new DisplayMetrics();
    public static double maxSize;


    public static String formatUserName(RPC.UserObject user) {
        String username;
        if (user.first_name != null && user.last_name != null && !user.first_name.equals("") && !user.last_name.equals("")) {
            username = user.first_name + " " + user.last_name;
        } else {
            username = user.login;
        }
        return username;
    }

    public static void checkDisplaySize(Context context, Configuration newConfiguration) {
        try {
            density = context.getResources().getDisplayMetrics().density;
            Configuration configuration = newConfiguration;
            if (configuration == null) {
                configuration = context.getResources().getConfiguration();
            }
            usingHardwareInput = configuration.keyboard != Configuration.KEYBOARD_NOKEYS && configuration.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO;
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    display.getMetrics(displayMetrics);
                    display.getSize(displaySize);
                }
            }
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }
            Log.d("payon-dbg", "display size = " + displaySize.x + " " + displaySize.y + " " + displayMetrics.xdpi + "x" + displayMetrics.ydpi);
        } catch (Exception e) {
            Log.e("payon-dbg", e.getMessage());
        }
    }

    public static int getAvailableTextLength(Paint textPaint, String text) {
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        int width = bounds.width();
        int availableLength = 0;
        if (width > maxSize) {
            width = 0;
            while (width < maxSize) {
                availableLength++;
                textPaint.getTextBounds(text, 0, availableLength, bounds);
                width = bounds.width();
            }
            return availableLength;
        }
        return text.length();
    }

    public static String formatDateTime(long timestamp, boolean format24h, boolean inChat) {
        String result;
        String pattern;

        Date now = new Date(System.currentTimeMillis());
        Date msgDate = new Date(timestamp * 1000L);
        if (now.getYear() != msgDate.getYear()) {
            if (inChat) {
                if (!format24h)
                    pattern = "d MMM yyyy hh:mm";
                else
                    pattern = "d MMM yyyy HH:mm";
            } else {
                if (!format24h)
                    pattern = "d MMM yyyy";
                else
                    pattern = "d MMM yyyy";
            }

            result = new SimpleDateFormat(pattern).format(new Date(timestamp * 1000L));
        } else if (now.getDay() - msgDate.getDay() > 1) {
            if (!format24h)
                pattern = "d MMM";
            else
                pattern = "d MMM";

            result = new SimpleDateFormat(pattern).format(new Date(timestamp * 1000L));
        } else if (now.getDay() - msgDate.getDay() == 1) {
            if (!format24h)
                pattern = "hh:mm";
            else
                pattern = "HH:mm";

            result = ApplicationLoader.applicationContext.getString(R.string.msg_time) + " " + new SimpleDateFormat(pattern).format(new Date(timestamp * 1000L));
        } else {
            if (!format24h)
                pattern = "hh:mm";
            else
                pattern = "HH:mm";

            result = new SimpleDateFormat(pattern).format(new Date(timestamp * 1000L));
        }

        return result;
    }

    public static boolean emailCorrect(String email) {
        Matcher matcher = Pattern.compile("^[-\\w.]+@([A-z0-9][-A-z0-9]+\\.)+[A-z]{2,4}$").matcher(email);
        return matcher.find();
    }

    public static boolean loginCorrect(String userLogin) {
        Matcher matcher;
        matcher = Pattern.compile("^[a-zA-Z0-9-_\\.]+$").matcher(userLogin);
        return userLogin.length() >= 3 && matcher.find();
    }

    public static void replaceFragmentWithAnimationSlideFade(final FragmentManager fragmentManager, final Fragment fragment, final String tag) {
        ApplicationLoader.applicationHandler.post(() -> {
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.animator.fade_to_back, R.animator.fade_to_up, R.animator.fade_to_back);
            fragmentTransaction.replace(R.id.container, fragment);
            fragmentTransaction.addToBackStack(tag);
            fragmentTransaction.commit();
        });
    }

    public static void replaceFragmentWithAnimationFade(FragmentManager fragmentManager, Fragment fragment, String tag) {

        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.animator.fade_to_up, R.animator.fade_to_back);
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(tag);
        fragmentTransaction.commit();
    }

    public static byte[] hexStringToBytes(String s) {
        int len = s.length();
        // safe for leading zero
        if (len % 2 != 0) {
            s = "0" + s;
            len++;
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean isNetworkConnected(final Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static void hideKeyboard(View view) {
        if (view == null) return;

        try {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (!imm.isActive())
                return;
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            Log.e(Config.TAG, e.getMessage());
        }
    }

    public static void setActionBarWithTitle(FragmentActivity fragmentActivity, String title) {
        ActionBar supportActionBar = ((AppCompatActivity) fragmentActivity).getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.show();
            supportActionBar.setTitle(title);
            supportActionBar.setDisplayShowCustomEnabled(false);
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        }
    }

    public static void setArrowBackInToolbar(FragmentActivity fragmentActivity) {
        final Toolbar toolbar = fragmentActivity.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            Utils.hideKeyboard(v);
            fragmentActivity.getSupportFragmentManager().popBackStack();
        });
    }
}
