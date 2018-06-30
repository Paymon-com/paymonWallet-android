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
import static ru.paymon.android.view.FragmentRecoveryPasswordEmail.PASSWORD_RECOVERY_LOGIN;

public class FragmentRecoveryPasswordCode extends Fragment {
    public static final String PASSWORD_RECOVERY_CODE = "code";
    private static FragmentRecoveryPasswordCode instance;

    private EditText codeEditText;
    private TextView hintError;
    private String login;
    private DialogProgress dialogProgress;

    public static synchronized FragmentRecoveryPasswordCode newInstance() {
        instance = new FragmentRecoveryPasswordCode();
        return instance;
    }

    public static synchronized FragmentRecoveryPasswordCode getInstance() {
        if (instance == null)
            instance = new FragmentRecoveryPasswordCode();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(PASSWORD_RECOVERY_LOGIN))
                login = bundle.getString(PASSWORD_RECOVERY_LOGIN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery_password_code, container, false);
        view.setBackgroundResource(R.drawable.background);

        hintError = view.findViewById(R.id.fragment_password_recovery_code_hint_error_text_view);
        codeEditText = view.findViewById(R.id.fragment_password_recovery_code_edit_text);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        codeEditText.requestFocus();
        codeEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE)
                showFragmentRecoveryPasswordCode();
            return true;
        });

        codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!editable.toString().isEmpty()) {
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
        inflater.inflate(R.menu.registration_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                showFragmentRecoveryPasswordCode();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFragmentRecoveryPasswordCode() {
        if (codeEditText.getText().toString().isEmpty()) {
            hintError.setText(R.string.string_code_is_empty);
            codeEditText.requestFocus();
            return;
        }

        RPC.PM_verifyPasswordRecoveryCode verifyPasswordRecoveryCode = new RPC.PM_verifyPasswordRecoveryCode();
        verifyPasswordRecoveryCode.login = login;
        verifyPasswordRecoveryCode.code = Integer.parseInt(codeEditText.getText().toString()); //TODO: запрет ввода букв

        dialogProgress.show();

        final long requestID = NetworkManager.getInstance().sendRequest(verifyPasswordRecoveryCode, (response, error) -> {
            if (error != null || (response != null && response instanceof RPC.PM_boolFalse)) {
                if (dialogProgress != null && dialogProgress.isShowing())
                    dialogProgress.cancel();
                ApplicationLoader.applicationHandler.post(() -> hintError.setText(R.string.password_recovery_failed));
                return;
            }

            if (dialogProgress != null && dialogProgress.isShowing()) dialogProgress.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                    .setMessage(getString(R.string.confirmation_code_verified))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                        Fragment fragment = FragmentRecoveryPasswordNew.newInstance();
                        Bundle bundle = new Bundle();
                        bundle.putString(PASSWORD_RECOVERY_LOGIN, login);
                        bundle.putString(PASSWORD_RECOVERY_CODE, codeEditText.getText().toString());
                        fragment.setArguments(bundle);
                        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragment, null);
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false));
    }

}
