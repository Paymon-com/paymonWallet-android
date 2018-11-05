package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentRecoveryPasswordCode.PASSWORD_RECOVERY_CODE;
import static ru.paymon.android.view.FragmentRecoveryPasswordEmail.PASSWORD_RECOVERY_LOGIN;

public class FragmentRecoveryNewPassword extends Fragment {
    private EditText newPassword;
    private EditText repeatNewPassword;
    private DialogProgress dialogProgress;
    private TextView hintError;
    private String login;
    private String code;

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

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        acceptToolbar.setOnClickListener(v -> recoveryPassword());

        newPassword = view.findViewById(R.id.new_password);
        repeatNewPassword = view.findViewById(R.id.repeat_new_password);
        hintError = view.findViewById(R.id.fragment_new_password_error_hint);

        dialogProgress = new DialogProgress(getContext());
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
    }

    public void recoveryPassword() {
        if (newPassword.getText().toString().length() < 8 || !repeatNewPassword.getText().toString().equals(newPassword.getText().toString()))
            return;

        Utils.netQueue.postRunnable(() -> {
            RPC.PM_restorePassword restorePassword = new RPC.PM_restorePassword();
            restorePassword.login = login;
            restorePassword.code = Integer.parseInt(code);
            restorePassword.password = newPassword.getText().toString();

            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            final long requestID = NetworkManager.getInstance().sendRequest(restorePassword, (response, error) -> {
                if (error != null || (response != null && response instanceof RPC.PM_boolFalse)) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        hintError.setText(R.string.password_recovery_failed);
                    });
                    return;
                }

                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setMessage(getString(R.string.password_was_changed))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.ok), (dialogInterface, i) ->
                                    Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentStart)
                            );
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                });
            });

            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        });
    }
}
