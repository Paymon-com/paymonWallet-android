package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;
import static ru.paymon.android.view.FragmentRegistrationLogin.KEY_LOGIN_REGISTRATION;

public class FragmentRegistrationPassword extends Fragment {
    public static final String KEY_PASSWORD_REGISTRATION = "password_registration";

    private static FragmentRegistrationPassword instance;
    private String login;
    private TextView hintError;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;


    public static synchronized FragmentRegistrationPassword newInstance() {
        instance = new FragmentRegistrationPassword();
        return instance;
    }

    public static synchronized FragmentRegistrationPassword getInstance() {
        if (instance == null)
            instance = new FragmentRegistrationPassword();
        return instance;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle loginBundle = getArguments();
        if (loginBundle != null) {
            if (loginBundle.containsKey(KEY_LOGIN_REGISTRATION))
                login = loginBundle.getString(KEY_LOGIN_REGISTRATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_registration));

        setHasOptionsMenu(true);
        getActivity().invalidateOptionsMenu();

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            Utils.hideKeyboard(v);
            getActivity().getSupportFragmentManager().popBackStack();
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_password, container, false);

        view.setBackgroundResource(R.drawable.background);

        passwordEditText = view.findViewById(R.id.registration_password_edit_text);
        repeatPasswordEditText = view.findViewById(R.id.registration_repeat_password_edit_text);
        hintError = view.findViewById(R.id.password_hint_error_text_view);

        passwordEditText.requestFocus();

        repeatPasswordEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                showFragmentRegistrationEmail();
            }
            return true;
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString();

                if (text.length() < 8)
                    hintError.setText(R.string.reg_check_password_length);
                else
                    hintError.setText("");
            }
        });

        repeatPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                if (!text.equals(passwordEditText.getText().toString())) {
                    hintError.setText(R.string.error_incorrect_double_password);
                } else {
                    hintError.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registration_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                showFragmentRegistrationEmail();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFragmentRegistrationEmail() {
        if (passwordEditText.getText().toString().length() < 8 || !repeatPasswordEditText.getText().toString().equals(passwordEditText.getText().toString()))
            return;

        final Bundle passwordBundle = new Bundle();
        passwordBundle.putString(KEY_LOGIN_REGISTRATION, login);
        passwordBundle.putString(KEY_PASSWORD_REGISTRATION, passwordEditText.getText().toString());

        final FragmentRegistrationEmail fragmentRegistrationEmail = FragmentRegistrationEmail.newInstance();
        fragmentRegistrationEmail.setArguments(passwordBundle);
        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentRegistrationEmail, null);
    }
}
