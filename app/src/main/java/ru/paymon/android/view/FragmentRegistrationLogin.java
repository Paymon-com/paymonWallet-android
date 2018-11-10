package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import ru.paymon.android.R;

import static ru.paymon.android.utils.Utils.loginCorrect;

public class FragmentRegistrationLogin extends Fragment {
    public static final String KEY_LOGIN_REGISTRATION = "login_registration";
    private EditText loginEditText;
    private TextView hintError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_login, container, false);
        view.setBackgroundResource(R.drawable.background);

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        acceptToolbar.setOnClickListener(v -> showFragmentRegistrationPassword());

        loginEditText = view.findViewById(R.id.registration_login_edit_text);
        hintError = view.findViewById(R.id.registration_login_error_hint);
        loginEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                showFragmentRegistrationPassword();
            }
            return true;
        });

        loginEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!loginCorrect(loginEditText.getText().toString())) {
                    hintError.setText(R.string.sign_up_login_error);
                } else {
                    hintError.setText("");
                }
            }
        });

        return view;
    }

    public void showFragmentRegistrationPassword() {
        if (!loginCorrect(loginEditText.getText().toString())) return;

        final Bundle loginBundle = new Bundle();
        loginBundle.putString(KEY_LOGIN_REGISTRATION, loginEditText.getText().toString());
        Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentRegistrationPassword, loginBundle);
    }

    @Override
    public void onPause() {
        super.onPause();
//        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }
}
