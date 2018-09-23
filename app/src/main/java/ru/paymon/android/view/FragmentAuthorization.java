package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.Config;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;


public class FragmentAuthorization extends Fragment {
    private static FragmentAuthorization instance;
    private EditText loginView;
    private EditText passView;
    private DialogProgress dialog;


    public static synchronized FragmentAuthorization newInstance() {
        instance = new FragmentAuthorization();
        return instance;
    }

    public static synchronized FragmentAuthorization getInstance() {
        if (instance == null)
            instance = new FragmentAuthorization();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Utils.stageQueue.postRunnable(() ->
//                NetworkManager.getInstance().reconnect()
//        );
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authorization, container, false);
        view.setBackgroundResource(R.drawable.background);
        View toolbar = view.findViewById(R.id.toolbar);
        ImageButton backToolbar = (ImageButton) toolbar.findViewById(R.id.toolbar_back_btn);
        ImageButton loginToolbar = (ImageButton) toolbar.findViewById(R.id.toolbar_next_btn);

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
                Toast.makeText(getActivity().getApplicationContext(), R.string.check_connected_to_server, Toast.LENGTH_SHORT).show();
            }
        });

        backToolbar.setOnClickListener(view1 -> getActivity().getSupportFragmentManager().popBackStack());

        dialog = new DialogProgress(getContext());
        dialog.setCancelable(true);

        loginView = (EditText) view.findViewById(R.id.login_authorization);
        passView = (EditText) view.findViewById(R.id.password_authorization);
        TextView recoveryPassword = (TextView) view.findViewById(R.id.forgot_password);

        getActivity().invalidateOptionsMenu();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setHasOptionsMenu(true);

        recoveryPassword.setOnClickListener((v) -> {
            final FragmentRecoveryPasswordEmail fragmentRecoveryPasswordEmail = FragmentRecoveryPasswordEmail.newInstance();
            Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), fragmentRecoveryPasswordEmail, null);
        });

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
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }

    private void auth(final String login, final String password) {
        Utils.netQueue.postRunnable(() -> {
            ApplicationLoader.applicationHandler.post(dialog::show);

            RPC.PM_auth request = new RPC.PM_auth();
            request.login = login;
            request.password = password;

            final long msgID = NetworkManager.getInstance().sendRequest(request, (response, error) -> {
                ApplicationLoader.applicationHandler.post(() -> {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                });

                if (response == null) return;
                RPC.PM_userFull user = (RPC.PM_userFull) response;
                User.currentUser = user;
                User.saveConfig();
                User.loadConfig();
                Log.d(Config.TAG, "Login successful: " + user.login + " TOKEN:" + Utils.bytesToHexString(User.currentUser.token));
                NetworkManager.getInstance().setAuthorized(true);
            });

            ApplicationLoader.applicationHandler.post(() ->
                    dialog.setOnDismissListener((dialogInterface) -> NetworkManager.getInstance().cancelRequest(msgID, false)));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
