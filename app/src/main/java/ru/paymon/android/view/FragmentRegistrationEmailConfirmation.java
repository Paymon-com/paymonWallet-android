package ru.paymon.android.view;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Timer;
import java.util.TimerTask;

import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.R.id.next;
import static ru.paymon.android.utils.Utils.emailCorrect;
import static ru.paymon.android.utils.Utils.setActionBarWithTitle;

public class FragmentRegistrationEmailConfirmation extends Fragment {
    private static FragmentRegistrationEmailConfirmation instance;
    private boolean isSendingAvailable = true;

    private EditText email;
    private TextView hintError;
    private TextView time;
    private CountDownTimer timer;

    public static synchronized FragmentRegistrationEmailConfirmation newInstance() {
        instance = new FragmentRegistrationEmailConfirmation();
        return instance;
    }

    public static synchronized FragmentRegistrationEmailConfirmation getInstance() {
        if (instance == null)
            instance = new FragmentRegistrationEmailConfirmation();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_email_confirmation, container, false);
        view.setBackgroundResource(R.drawable.background);

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_confirmation));

        email = view.findViewById(R.id.email_confirmation_edit_text);
        hintError = view.findViewById(R.id.email_confirmation_hint_error);
        time = view.findViewById(R.id.email_confirmation_timer);


        email.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                confirmRegistration();
            }
            return true;
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!emailCorrect(email.getText().toString())) {
                    hintError.setText(R.string.reg_check_email);
                } else {
                    hintError.setText("");
                }
            }
        });

        timer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long l) {
                time.setText("Время до след отправки : " + android.text.format.DateFormat.format("mm:ss", l).toString());
            }

            @Override
            public void onFinish() {
                isSendingAvailable = true;
                time.setText("");
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.registration_email_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case next:
                confirmRegistration();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void confirmRegistration() {
        if (!emailCorrect(email.getText().toString())) return;

        if(isSendingAvailable) {
            isSendingAvailable = false;
            Log.d(Config.TAG, "отправка");
            //TODO: отправляем на серв
            timer.start();
        }
    }


}
