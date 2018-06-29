package ru.paymon.android.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
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
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.nav_exit;
import static ru.paymon.android.R.id.next;
import static ru.paymon.android.R.id.send;

public class FragmentRecoveryPasswordNew extends Fragment {
    private static FragmentRecoveryPasswordNew instance;

    private EditText newPassword;
    private EditText repeatNewPassword;

    private TextView hintError;

    public static synchronized FragmentRecoveryPasswordNew newInstance(){
        instance = new FragmentRecoveryPasswordNew();
        return instance;
    }

    public static synchronized FragmentRecoveryPasswordNew getInstance(){
        if (instance == null)
            instance = new FragmentRecoveryPasswordNew();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery_password_new, container, false);
        view.setBackgroundResource(R.drawable.background);

        newPassword = view.findViewById(R.id.new_password);
        repeatNewPassword = view.findViewById(R.id.repeat_new_password);

        hintError = view.findViewById(R.id.fragment_new_password_error_hint);

        repeatNewPassword.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT){
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
                if (editable.toString().isEmpty()){
                    hintError.setText("");
                }

                if (editable.toString().length() < 8){
                    hintError.setText(R.string.reg_check_password_length);
                    return;
                } else{
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
                if (!editable.toString().equals(newPassword.getText().toString())){
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Важное сообщение!")
                .setMessage("Пароль должен быть обновлен")
                .setCancelable(false)
                .setNegativeButton("ОК, иду", (dialogInterface, i) ->
                        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(),  FragmentStart.newInstance(), null)
                );
        AlertDialog alert = builder.create();
        alert.show();
    }
}
