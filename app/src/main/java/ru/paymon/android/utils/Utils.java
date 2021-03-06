package ru.paymon.android.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.net.RPC;

import static android.content.Context.CLIPBOARD_SERVICE;
import static ru.paymon.android.User.CLIENT_BASIC_DATE_FORMAT_IS_24H;

public class Utils {
    public static volatile DispatchQueue netQueue = new DispatchQueue("netQueue");
    public static volatile DispatchQueue stageQueue = new DispatchQueue("stageQueue");
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    public static boolean copyFile(File source, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                is.close();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean renameFile(final File from, final File to) {
        return from.renameTo(to);
    }

    public static boolean renameFile(final String fromAbsolutePAth, final String toAbsolutePath) {
        return renameFile(new File(fromAbsolutePAth), new File(toAbsolutePath));
    }

    public static boolean renameFile(final String directory, final String fromFileName, final String toFileName) {
        return renameFile(directory + "/" + fromFileName, directory + "/" + toFileName);
    }

    public static String formatUserName(RPC.UserObject user) {
        String username;
        if (user.first_name != null && user.last_name != null && !user.first_name.equals("") && !user.last_name.equals("")) {
            username = user.first_name + " " + user.last_name;
        } else {
            username = user.login;
        }
        return username;
    }

    public static String formatDateTime(long timestamp, boolean inChat) {
        final Date now = new Date(System.currentTimeMillis());
        final Date msgDate = new Date(timestamp * 1000L);

        final int yearDiff = Integer.parseInt((String) DateFormat.format("yyyy", now)) - Integer.parseInt((String) DateFormat.format("yyyy", msgDate));
        final int monthDiff = Integer.parseInt((String) DateFormat.format("MM", now)) - Integer.parseInt((String) DateFormat.format("MM", msgDate));
        final int dayDiff = Integer.parseInt((String) DateFormat.format("dd", now)) - Integer.parseInt((String) DateFormat.format("dd", msgDate));

        String pattern;

        if (dayDiff == 0 && monthDiff == 0)
            pattern = "HH:mm";
        else if (dayDiff == 1 && monthDiff == 0)
            if (CLIENT_BASIC_DATE_FORMAT_IS_24H)
                return ApplicationLoader.applicationContext.getString(R.string.other_yesterday) + " " + DateFormat.format("HH:mm", msgDate);
            else
                return ApplicationLoader.applicationContext.getString(R.string.other_yesterday) + " " + DateFormat.format("hh:mm aa", msgDate);
        else if (dayDiff > 1 || monthDiff > 0)
            pattern = "d MMM";
        else if (yearDiff != 0)
            if (inChat)
                pattern = "d MMM yyyy HH:mm";
            else
                pattern = "d MMM yyyy";
        else
            pattern = "HH:mm";

        if (!CLIENT_BASIC_DATE_FORMAT_IS_24H)
            pattern = pattern.replace("HH:mm", "hh:mm aa");

        return (String) DateFormat.format(pattern, msgDate);
    }


    public static boolean emailCorrect(String email) {
        Matcher matcher = Pattern.compile("^[-\\w.]+@([A-z0-9][-A-z0-9]+\\.)+[A-z]{2,4}$").matcher(email);
        return matcher.find() && !email.isEmpty();
    }

    public static boolean loginCorrect(String userLogin) {
        Matcher matcher;
        matcher = Pattern.compile("^[a-zA-Z0-9-_\\.]+$").matcher(userLogin);
        return userLogin.length() >= 3 && matcher.find();
    }

    public static void copyText(String text, FragmentActivity fragmentActivity) {
        ClipboardManager clipboard = (ClipboardManager) fragmentActivity.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(fragmentActivity, fragmentActivity.getString(R.string.other_text_is_copied), Toast.LENGTH_SHORT).show();
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

    public static void hideBottomBar(FragmentActivity fragmentActivity) {
        final BottomNavigationView bottomNavigationView = fragmentActivity.findViewById(R.id.bottom_navigation_view);
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);
    }

    public static void showBottomBar(FragmentActivity fragmentActivity) {
        final BottomNavigationView bottomNavigationView = fragmentActivity.findViewById(R.id.bottom_navigation_view);
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.VISIBLE);
    }

    public static boolean nameCorrect(String name) {
        return !name.isEmpty() && Pattern.compile("^[a-zA-zа-яА-Я]{1,30}$").matcher(name).find();
    }

    public static boolean phoneCorrect(String phNumber) {
        if (phNumber.isEmpty()) return true;
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phNumber, null);
            return phoneNumberUtil.isValidNumber(phoneNumber);
        } catch (NumberParseException ex) {
            return false;
        }
    }

    public static String formatPhone(long phone) {
        if (phone == 0)
            return "";

        String phNumber = "+" + String.valueOf(phone);
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(phNumber, null);
            return phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException ex) {
            return "";
        }
    }

    public static String formatPhone(String phone) {
        char[] chars = phone.toCharArray();
        for (char ch : chars) {
            if (!Character.isDigit(ch))
                phone = phone.replace(String.valueOf(ch), "");
        }
        return formatPhone(Long.parseLong(phone));
    }

    public static String getETHorBTCpubKeyFromText(String text) {
        Matcher matcher = Pattern.compile("^(^0x[a-fA-F0-9]{40,44}$)|(^[13][a-zA-Z0-9]{25,34}$)$").matcher(text);

        if (!matcher.find()) return null;

        int foundCount = 0;
        while (matcher.find()) {
            foundCount++;
            if (foundCount > 1)
                return null;
        }

        return matcher.group(1);
    }

    public static boolean verifyBTCpubKey(String key) {
        Matcher matcher = Pattern.compile("^[13][a-zA-Z0-9]{25,34}$").matcher(key);
        return !key.isEmpty() && matcher.find();
    }

    public static boolean verifyETHpubKey(String key) {
        Matcher matcher = Pattern.compile("^0x[a-fA-F0-9]{40,44}$").matcher(key);
        return !key.isEmpty() && matcher.find();
    }


    // BTC return 1
    // ETH return 2
    // matches not found return 0
    public static int identifyTypeOfPubKey(String key) {
        if (key.isEmpty()) return 0;

        Matcher matcher = Pattern.compile("^[13][a-zA-Z0-9]{25,34}$").matcher(key);
        if (matcher.find())
            return 1;

        matcher = Pattern.compile("^0x[a-fA-F0-9]{40,44}$").matcher(key);
        if (matcher.find())
            return 2;

        return 0;
    }

    public static void loadPhoto(String url, ImageView view){
        Picasso.get().load(url)
                .resize(300, 0)
                .centerCrop()
                .placeholder(R.drawable.profile_photo_none)
                .error(R.drawable.profile_photo_none)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(view, new Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("UTILS loadPhoto() ", "URL : " + url + " " + e.getMessage());
                        Picasso.get().load(url)
                                .error(R.drawable.profile_photo_none)
                                .resize(300, 0)
                                .centerCrop()
                                .into(view);
                    }
                });
    }
}
