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

import static ru.paymon.android.view.FragmentRecoveryPasswordEmail.PASSWORD_RECOVERY_LOGIN;

public class FragmentRecoveryPasswordCode extends Fragment {
    public static final String PASSWORD_RECOVERY_CODE = "code";
    private EditText codeEditText;
    private TextView hintError;
    private String login;
    private DialogProgress dialogProgress;

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

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(view1 -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        acceptToolbar.setOnClickListener(view12 -> showFragmentRecoveryPasswordCode());

        hintError = view.findViewById(R.id.fragment_password_recovery_code_hint_error_text_view);
        codeEditText = view.findViewById(R.id.fragment_password_recovery_code_edit_text);

        dialogProgress = new DialogProgress(getContext());
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
        setHasOptionsMenu(true);
    }

    public void showFragmentRecoveryPasswordCode() {
        if (codeEditText.getText().toString().isEmpty()) {
            hintError.setText(R.string.string_code_is_empty);
            codeEditText.requestFocus();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString(PASSWORD_RECOVERY_LOGIN, login);
        bundle.putString(PASSWORD_RECOVERY_CODE, codeEditText.getText().toString());
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentRecoveryNewPassword, bundle);
    }
}
