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

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;

public class FragmentRecoveryPasswordCode extends Fragment {
    private static FragmentRecoveryPasswordCode instance;

    private EditText codeEditText;
    private TextView hintError;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery_password_code, container, false);
        view.setBackgroundResource(R.drawable.background);

        hintError = view.findViewById(R.id.fragment_password_recovery_code_hint_error_text_view);

        codeEditText = view.findViewById(R.id.fragment_password_recovery_code_edit_text);
        codeEditText.requestFocus();
        codeEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                showFragmentRecoveryPasswordCode();
            }
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
        Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(),  FragmentRecoveryPasswordNew.newInstance(), null);

    }

}
