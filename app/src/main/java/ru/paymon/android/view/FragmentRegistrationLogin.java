package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.utils.Utils.loginCorrect;

public class FragmentRegistrationLogin extends Fragment {
    public static final String KEY_LOGIN_REGISTRATION = "login_registration";

    private static FragmentRegistrationLogin instance;
    private EditText loginEditText;
    private TextView hintError;
    private FragmentActivity activity;


    public static synchronized FragmentRegistrationLogin newInstance() {
        instance = new FragmentRegistrationLogin();
        return instance;
    }

    public static synchronized FragmentRegistrationLogin getInstance() {
        if (instance == null)
            instance = new FragmentRegistrationLogin();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity = getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_login, container, false);
        view.setBackgroundResource(R.drawable.background);

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(view1 -> getActivity().getSupportFragmentManager().popBackStack());

        acceptToolbar.setOnClickListener(view12 -> showFragmentRegistrationPassword());

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
                    hintError.setText(R.string.reg_check_login_correct);
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
        final FragmentRegistrationPassword fragmentRegistrationPassword = FragmentRegistrationPassword.newInstance();
        fragmentRegistrationPassword.setArguments(loginBundle);
        Utils.replaceFragmentWithAnimationFade(activity.getSupportFragmentManager(), fragmentRegistrationPassword, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }
}
