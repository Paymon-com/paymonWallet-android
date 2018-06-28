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
import android.widget.EditText;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;


import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;
import static ru.paymon.android.utils.Utils.emailCorrect;
import static ru.paymon.android.view.FragmentRegistrationLogin.KEY_LOGIN_REGISTRATION;
import static ru.paymon.android.view.FragmentRegistrationPassword.KEY_PASSWORD_REGISTRATION;

public class FragmentRegistrationEmail extends Fragment {
    private static FragmentRegistrationEmail instance;

    private String login;
    private String password;
    private EditText emailEditText;
    private EditText promocodeEditText;
    private TextView hintError;

    public static synchronized FragmentRegistrationEmail newInstance() {
        instance = new FragmentRegistrationEmail();
        return instance;
    }

    public static synchronized FragmentRegistrationEmail getInstance() {
        if (instance == null)
            instance = new FragmentRegistrationEmail();
        return instance;
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_registration));

        setHasOptionsMenu(true);

        final Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            Utils.hideKeyboard(v);
            getActivity().getSupportFragmentManager().popBackStack();
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle passwordBundle = getArguments();
        if (passwordBundle != null) {
            if (passwordBundle.containsKey(KEY_LOGIN_REGISTRATION))
                login = passwordBundle.getString(KEY_LOGIN_REGISTRATION);
            if (passwordBundle.containsKey(KEY_PASSWORD_REGISTRATION))
                password = passwordBundle.getString(KEY_PASSWORD_REGISTRATION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_email, container, false);
        view.setBackgroundResource(R.drawable.background);
        DialogProgress dialogProgress;
        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(false);

        emailEditText = view.findViewById(R.id.registration_email_edit_text);
        promocodeEditText = view.findViewById(R.id.registration_promocode_edit_text);
        TextView agreementTextView = view.findViewById(R.id.registration_email_agreement_text_view);
        TextView privacyPolicyTextView = view.findViewById(R.id.registration_email_privacy_policy_text_view);
        hintError = view.findViewById(R.id.registration_email_hint_error_text_view);

        agreementTextView.setOnClickListener(v -> {
            Utils.hideKeyboard(v);
            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentAgreement.newInstance(), null);
        });

        privacyPolicyTextView.setOnClickListener(v -> {
            Utils.hideKeyboard(v);
            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentPrivacyPolicy.newInstance(), null);
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!emailCorrect(emailEditText.getText().toString())) {
                    hintError.setText(R.string.reg_check_email);
                } else {
                    hintError.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registration_email_menu, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                registration();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void registration(){
        if(!emailCorrect(emailEditText.getText().toString())) return;

        //TODO:registration
    }
}
