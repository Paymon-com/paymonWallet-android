package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
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
import ru.paymon.android.components.DialogProgress;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentRecoveryPasswordEmail extends Fragment {
    public static final String PASSWORD_RECOVERY_LOGIN = "login";
    private EditText emailEditText;
    private TextView hintError;
    private DialogProgress dialogProgress;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery_password_email, container, false);
        view.setBackgroundResource(R.drawable.background);
        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        acceptToolbar.setOnClickListener(v -> showFragmentRecoveryPasswordCode());

        emailEditText = view.findViewById(R.id.fragment_password_recovery_email_edit_text);
        hintError = view.findViewById(R.id.fragment_password_recovery_email_error_hint_text_view);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        emailEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_DONE) {
                showFragmentRecoveryPasswordCode();
            }
            return true;
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty()) {
                    hintError.setText("");
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    public void showFragmentRecoveryPasswordCode() {
        if (emailEditText.getText().toString().isEmpty()) {
            hintError.setText(R.string.sign_up_email_error);
            emailEditText.requestFocus();
            return;
        }

        Utils.netQueue.postRunnable(() -> {
            RPC.PM_restorePasswordRequestCode passwordRecoveryCodeRequest = new RPC.PM_restorePasswordRequestCode();
            passwordRecoveryCodeRequest.login = emailEditText.getText().toString();

            ApplicationLoader.applicationHandler.post(dialogProgress::show);

            final long requestID = NetworkManager.getInstance().sendRequest(passwordRecoveryCodeRequest, (response, error) -> {
                if (error != null || response instanceof RPC.PM_boolFalse) {
                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                        hintError.setText(R.string.recovery_password_error);
                    });
                    return;
                }

                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialogProgress != null && dialogProgress.isShowing())
                        dialogProgress.dismiss();

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                            .setMessage(getString(R.string.recovery_password_sent))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.other_ok), (dialogInterface, i) -> {
                                Bundle bundle = new Bundle();
                                bundle.putString(PASSWORD_RECOVERY_LOGIN, emailEditText.getText().toString());
                                Navigation.findNavController(getActivity(),R.id.nav_host_fragment).navigate(R.id.fragmentRecoveryPasswordCode, bundle);
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                });
            });

            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
        });
    }
}
