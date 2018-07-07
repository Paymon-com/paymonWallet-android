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

import static ru.paymon.android.R.id.send;

public class FragmentRecoveryPasswordEmail extends Fragment {
    public static final String PASSWORD_RECOVERY_LOGIN = "login";
    private static FragmentRecoveryPasswordEmail instance;

    private EditText emailEditText;
    private TextView hintError;
    private DialogProgress dialogProgress;

    public static synchronized FragmentRecoveryPasswordEmail newInstance() {
        instance = new FragmentRecoveryPasswordEmail();
        return instance;
    }

    public static synchronized FragmentRecoveryPasswordEmail getInstance() {
        if (instance == null)
            instance = new FragmentRecoveryPasswordEmail();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery_password_email, container, false);
        view.setBackgroundResource(R.drawable.background);

        emailEditText = view.findViewById(R.id.fragment_password_recovery_email_edit_text);
        hintError = view.findViewById(R.id.fragment_password_recovery_email_error_hint_text_view);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        emailEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                showFragmentRecoveryPasswordCode();
            }
            return true;
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
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
        inflater.inflate(R.menu.password_recovery_send_email, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case send:
                showFragmentRecoveryPasswordCode();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFragmentRecoveryPasswordCode() {
        if (emailEditText.getText().toString().isEmpty()) {
            hintError.setText(R.string.string_is_empty);
            emailEditText.requestFocus();
            return;
        }

        RPC.PM_sendPasswordRecoveryCode passwordRecoveryCodeRequest = new RPC.PM_sendPasswordRecoveryCode();
        passwordRecoveryCodeRequest.login = emailEditText.getText().toString();

        dialogProgress.show();

        final long requestID = NetworkManager.getInstance().sendRequest(passwordRecoveryCodeRequest, (response, error) -> {
            if (error != null || (response != null && response instanceof RPC.PM_boolFalse)) {
                if (dialogProgress != null && dialogProgress.isShowing())
                    dialogProgress.cancel();
                ApplicationLoader.applicationHandler.post(() -> hintError.setText(R.string.password_recovery_failed));
                return;
            }

            if (dialogProgress != null && dialogProgress.isShowing()) dialogProgress.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.confirmation_code_was_sent))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                        Fragment fragment = FragmentRecoveryPasswordCode.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(PASSWORD_RECOVERY_LOGIN, emailEditText.getText().toString());
                        fragment.setArguments(bundle);
                        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragment, null);
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false));
    }
}