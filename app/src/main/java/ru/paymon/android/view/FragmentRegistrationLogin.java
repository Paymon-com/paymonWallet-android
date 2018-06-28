package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;
import static ru.paymon.android.utils.Utils.loginCorrect;

public class FragmentRegistrationLogin extends Fragment {
    public static final String KEY_LOGIN_REGISTRATION = "login_registration";

    private static FragmentRegistrationLogin instance;
    private EditText loginEditText;
    private TextView hintError;


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
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_registration));

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setHasOptionsMenu(true);

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
        View view = inflater.inflate(R.layout.fragment_registration_login, container, false);
        view.setBackgroundResource(R.drawable.background);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registration_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                showFragmentRegistrationPassword();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFragmentRegistrationPassword() {
        if (!loginCorrect(loginEditText.getText().toString())) return;

        final Bundle loginBundle = new Bundle();
        loginBundle.putString(KEY_LOGIN_REGISTRATION, loginEditText.getText().toString());
        final FragmentRegistrationPassword fragmentRegistrationPassword = FragmentRegistrationPassword.newInstance();
        fragmentRegistrationPassword.setArguments(loginBundle);
        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentRegistrationPassword, null);
    }
}
