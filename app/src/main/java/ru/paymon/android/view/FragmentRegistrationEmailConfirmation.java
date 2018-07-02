package ru.paymon.android.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.MainActivity;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;
import static ru.paymon.android.utils.Utils.emailCorrect;
import static ru.paymon.android.utils.Utils.setActionBarWithTitle;

public class FragmentRegistrationEmailConfirmation extends Fragment {
    private static FragmentRegistrationEmailConfirmation instance;
    private boolean isSendingAvailable = true;

    private EditText email;
    private TextView hintError;
    private TextView time;
    private CountDownTimer timer;
    private DialogProgress dialogProgress;

    public static synchronized FragmentRegistrationEmailConfirmation newInstance() {
        instance = new FragmentRegistrationEmailConfirmation();
        return instance;
    }

    public static synchronized FragmentRegistrationEmailConfirmation getInstance() {
        if (instance == null)
            instance = new FragmentRegistrationEmailConfirmation();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_email_confirmation, container, false);
        view.setBackgroundResource(R.drawable.background);

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_confirmation));

        email = view.findViewById(R.id.email_confirmation_edit_text);
        hintError = view.findViewById(R.id.email_confirmation_hint_error);
        time = view.findViewById(R.id.email_confirmation_timer);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        email.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                confirmRegistration();
            }
            return true;
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!emailCorrect(email.getText().toString())) {
                    hintError.setText(R.string.reg_check_email);
                } else {
                    hintError.setText("");
                }
            }
        });

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
                time.setText("Время до след отправки : " + android.text.format.DateFormat.format("mm:ss", l).toString());
            }

            @Override
            public void onFinish() {
                isSendingAvailable = true;
                time.setText("");
            }
        };

        email.setText(User.currentUser.email);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registration_email_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                confirmRegistration();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirmRegistration() {
        if (!emailCorrect(email.getText().toString())) return;

        if (isSendingAvailable) {
            isSendingAvailable = false;

            RPC.PM_checkEmailConfirmation checkEmailRequest = new RPC.PM_checkEmailConfirmation();
            checkEmailRequest.login = User.currentUser.login;
            checkEmailRequest.newEmail = email.getText().toString();

            dialogProgress.show();

            final long requestID = NetworkManager.getInstance().sendRequest(checkEmailRequest, (response, error) -> {
                if (error != null) {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.cancel();
                    switch (error.code) { //TODO: add text to string file
                        case 1:
                            ApplicationLoader.applicationHandler.post(() -> hintError.setText("Логин или емейл пустой"));
                            break;
                        case 2:
                            ApplicationLoader.applicationHandler.post(() -> hintError.setText("Ошибка в емейле"));
                            break;
                        case 3:
                            ApplicationLoader.applicationHandler.post(() -> hintError.setText(R.string.registration_email_used));
                            break;
                    }
                    return;
                }

                if (response != null) {
                    if (response instanceof RPC.PM_boolFalse) {
                        ApplicationLoader.applicationHandler.post(() -> hintError.setText(""));
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                .setMessage(getString(R.string.confirmation_code_was_sent))
                                .setCancelable(false);
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }

                    if (response instanceof RPC.PM_boolTrue) {
                        User.currentUser.confirmed = true;
                        User.saveConfig();

                        Intent mainActivityIntent = new Intent(ApplicationLoader.applicationContext, MainActivity.class);
                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mainActivityIntent);
                    }
                }

                if (dialogProgress != null && dialogProgress.isShowing()) dialogProgress.dismiss();
            });

            dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false));
            timer.start();
        }
    }


}
