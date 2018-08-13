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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

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
    private DialogProgress dialogProgress;

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

        setHasOptionsMenu(true);
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

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(view1 -> getActivity().getSupportFragmentManager().popBackStack());

        acceptToolbar.setOnClickListener(view12 -> registration());

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

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
                if (!emailCorrect(emailEditText.getText().toString()))
                    hintError.setText(R.string.reg_check_email);
                else
                    hintError.setText("");
            }
        });

        return view;
    }

    private void registration() {
        if (!emailCorrect(emailEditText.getText().toString())) return;

        Utils.hideKeyboard(this.getView());

        Utils.netQueue.postRunnable(() -> {
            final RPC.PM_register packet = new RPC.PM_register();
            packet.login = login;
            packet.password = password;
            packet.walletKey = "0000000000000000000000000000000000";
            packet.walletBytes = new byte[32];
            packet.email = emailEditText.getText().toString().trim();
            packet.inviteCode = promocodeEditText.getText().toString().trim().toLowerCase();

            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            final long registration = NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                if (error != null && error.code == RPC.ERROR_REGISTER_USER_EXISTS) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        hintError.setText(R.string.registration_email_used);
                    });
                    return;
                }

                if (response == null) return;

                User.currentUser = (RPC.PM_userFull) response;
                User.saveConfig();

                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.dismiss();
                });
//            NotificationManager.getInstance().postNotificationName(NotificationManager.userRegistered);
//            Intent mainActivityIntent = new Intent(ApplicationLoader.applicationContext, MainActivity.class);
//            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//            mainActivityIntent.putExtra(REGISTRATION_FLAG, true);
//            startActivity(mainActivityIntent);
            });

            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(registration, false)));
        });
    }
}
