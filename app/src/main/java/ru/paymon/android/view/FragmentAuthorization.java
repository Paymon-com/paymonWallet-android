package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.firebase.FcmService;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentRegistrationLogin.KEY_LOGIN_REGISTRATION;
import static ru.paymon.android.view.FragmentRegistrationPassword.KEY_PASSWORD_REGISTRATION;


public class FragmentAuthorization extends Fragment {
    private EditText loginView;
    private EditText passView;
    private DialogProgress dialog;
    private String login;
    private String password;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey(KEY_LOGIN_REGISTRATION))
                login = bundle.getString(KEY_LOGIN_REGISTRATION);
            if (bundle.containsKey(KEY_PASSWORD_REGISTRATION))
                password = bundle.getString(KEY_PASSWORD_REGISTRATION);
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authorization, container, false);
        view.setBackgroundResource(R.drawable.background);
        View toolbar = view.findViewById(R.id.toolbar);
        ImageButton backToolbar = (ImageButton) toolbar.findViewById(R.id.toolbar_back_btn);
        ImageButton loginToolbar = (ImageButton) toolbar.findViewById(R.id.toolbar_next_btn);
        loginView = (EditText) view.findViewById(R.id.login_authorization);
        passView = (EditText) view.findViewById(R.id.password_authorization);
        TextView recoveryPassword = (TextView) view.findViewById(R.id.forgot_password);


        loginView.setText(login);
        passView.setText(password);

        loginToolbar.setOnClickListener(view12 -> {
            if (NetworkManager.getInstance().isConnected()) {
                final String login = loginView.getText().toString().trim();
                final String password = passView.getText().toString().trim();

                if (login.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.reg_check_login), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.reg_check_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                auth(login, password);
            } else {
                Toast.makeText(getContext(), R.string.check_connected_to_server, Toast.LENGTH_SHORT).show();
            }
        });


        backToolbar.setOnClickListener(view1 -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

        dialog = new DialogProgress(getContext());
        dialog.setCancelable(true);

        recoveryPassword.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentRecoveryPasswordEmail));

        passView.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_ENTER:
                        String login = loginView.getText().toString().trim();
                        String password = passView.getText().toString().trim();

                        if (login.equals("")) {
                            Toast.makeText(getContext(), getString(R.string.reg_check_login), Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        if (password.equals("")) {
                            Toast.makeText(getContext(), getString(R.string.reg_check_password), Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        Utils.hideKeyboard(v);
                        auth(login, password);
                        return true;
                    default:
                        break;
                }
            }
            return false;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void auth(final String login, final String password) {
        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialog::show);

            RPC.PM_auth request = new RPC.PM_auth();
            request.login = login;
            request.password = password;
            request.fcmToken = FcmService.token;

            final long msgID = NetworkManager.getInstance().sendRequest(request, (response, error) -> {
                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                });

                if (response == null) return;
                User.currentUser = (RPC.PM_userSelf) response;
                User.saveConfig();
                User.loadConfig();
                NetworkManager.getInstance().setAuthorized(true);
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.mainActivity);
            });

            ApplicationLoader.applicationHandler.post(() -> dialog.setOnDismissListener((dialogInterface) -> NetworkManager.getInstance().cancelRequest(msgID, false)));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
