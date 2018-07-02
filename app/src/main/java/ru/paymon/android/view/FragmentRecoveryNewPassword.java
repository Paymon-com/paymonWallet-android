package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;
import static ru.paymon.android.view.FragmentRecoveryPasswordCode.PASSWORD_RECOVERY_CODE;
import static ru.paymon.android.view.FragmentRecoveryPasswordEmail.PASSWORD_RECOVERY_LOGIN;

public class FragmentRecoveryNewPassword extends Fragment {
    private static FragmentRecoveryNewPassword instance;

    private EditText newPassword;
    private EditText repeatNewPassword;
    private DialogProgress dialogProgress;
    private TextView hintError;
    private String login;
    private String code;


    public static synchronized FragmentRecoveryNewPassword newInstance() {
        instance = new FragmentRecoveryNewPassword();
        return instance;
    }

    public static synchronized FragmentRecoveryNewPassword getInstance() {
        if (instance == null)
            instance = new FragmentRecoveryNewPassword();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(PASSWORD_RECOVERY_LOGIN))
                login = bundle.getString(PASSWORD_RECOVERY_LOGIN);
            if (bundle.containsKey(PASSWORD_RECOVERY_CODE))
                code = bundle.getString(PASSWORD_RECOVERY_CODE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery_password_new, container, false);
        view.setBackgroundResource(R.drawable.background);

        newPassword = view.findViewById(R.id.new_password);
        repeatNewPassword = view.findViewById(R.id.repeat_new_password);
        hintError = view.findViewById(R.id.fragment_new_password_error_hint);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        repeatNewPassword.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT) {
                recoveryPassword();
            }
            return true;
        });

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    hintError.setText("");
                }

                if (editable.toString().length() < 8) {
                    hintError.setText(R.string.reg_check_password_length);
                    return;
                } else {
                    hintError.setText("");
                }
            }
        });

        repeatNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().equals(newPassword.getText().toString())) {
                    hintError.setText(R.string.error_incorrect_double_password);
                } else {
                    hintError.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_password_recovery));
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registration_email_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                recoveryPassword();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void recoveryPassword() {
        if (newPassword.getText().toString().length() < 8 || !repeatNewPassword.getText().toString().equals(newPassword.getText().toString()))
            return;

        RPC.PM_restorePassword restorePassword = new RPC.PM_restorePassword();
        restorePassword.login = login;
        restorePassword.code = Integer.parseInt(code);
        restorePassword.password = newPassword.getText().toString();

        dialogProgress.show();

        final long requestID = NetworkManager.getInstance().sendRequest(restorePassword, (response, error) -> {
            if (error != null || (response != null && response instanceof RPC.PM_boolFalse)) {
                if (dialogProgress != null && dialogProgress.isShowing())
                    dialogProgress.cancel();
                ApplicationLoader.applicationHandler.post(() -> hintError.setText(R.string.password_recovery_failed));
                return;
            }

            if (dialogProgress != null && dialogProgress.isShowing()) dialogProgress.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.password_was_changed))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentStart.newInstance(), null)
                    );
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false));
    }
}
