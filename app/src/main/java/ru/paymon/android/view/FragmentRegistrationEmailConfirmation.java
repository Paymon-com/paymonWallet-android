package ru.paymon.android.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.DialogProgress;
import ru.paymon.android.firebase.FcmService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.Config.TAG;
import static ru.paymon.android.utils.Utils.emailCorrect;

public class FragmentRegistrationEmailConfirmation extends Fragment implements NotificationManager.IListener {
    private boolean isSendingAvailable = true;
    private EditText email;
    private TextView hintError;
    private TextView time;
    private CountDownTimer timer;
    private DialogProgress dialogProgress;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registration_email_confirmation, container, false);
        view.setBackgroundResource(R.drawable.background);

        ImageButton backToolbar = (ImageButton) view.findViewById(R.id.toolbar_back_btn);
        ImageButton acceptToolbar = (ImageButton) view.findViewById(R.id.toolbar_next_btn);

        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        acceptToolbar.setOnClickListener(v -> confirmRegistration());

        //Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_confirmation));

        email = view.findViewById(R.id.email_confirmation_edit_text);
        hintError = view.findViewById(R.id.email_confirmation_hint_error);
        time = view.findViewById(R.id.email_confirmation_timer);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

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

        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long l) {
                time.setText(getString(R.string.time_to_next_dispatch) + ": " + android.text.format.DateFormat.format("mm:ss", l).toString());
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
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        email.setText(User.currentUser.email);
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.userAuthorized);
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }

    public void confirmRegistration() {
        if (!emailCorrect(email.getText().toString())) return;

        if (isSendingAvailable) {
            isSendingAvailable = false;
            NetworkManager.getInstance().reconnect();
            timer.start();
        }
    }


    @Override
    public void didReceivedNotification(NotificationManager.NotificationEvent event, Object... args) {
        if (event == NotificationManager.NotificationEvent.userAuthorized) {
            if (User.currentUser.confirmed) {
                NavOptions navOptions = new NavOptions.Builder().setClearTask(true).build();
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.mainActivity, null, navOptions);
            } else {
                ApplicationLoader.applicationHandler.post(dialogProgress::show);

                RPC.PM_resendEmail checkEmailRequest = new RPC.PM_resendEmail();
                checkEmailRequest.newEmail = email.getText().toString();

                final long requestID = NetworkManager.getInstance().sendRequest(checkEmailRequest, (r, e) -> {
                    if (e != null) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            if (dialogProgress != null && dialogProgress.isShowing())
                                dialogProgress.cancel();
                        });
                        return;
                    }

                    if (r instanceof RPC.PM_boolFalse) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            hintError.setText("");
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                                    .setMessage(getString(R.string.confirmation_code_was_sent))
                                    .setCancelable(true)
                                    .setPositiveButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {});
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        });
                    }

                    if (r instanceof RPC.PM_boolTrue) {
                        ApplicationLoader.applicationHandler.post(() -> {
                            hintError.setText("");
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                    .setMessage(getString(R.string.confirmation_code_was_sent))
                                    .setCancelable(true)
                                    .setPositiveButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {});
                            AlertDialog alertDialog = builder.create();
                            alertDialog.show();
                        });
                    }

                    ApplicationLoader.applicationHandler.post(() -> {
                        if (dialogProgress != null && dialogProgress.isShowing())
                            dialogProgress.cancel();
                    });
                });

                ApplicationLoader.applicationHandler.post(() -> {
                    dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false));
                    timer.start();
                });
            }
        }
    }
}
