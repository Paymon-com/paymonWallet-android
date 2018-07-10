package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import ru.paymon.android.R;
import ru.paymon.android.utils.Utils;


public class FragmentPermissions extends Fragment {
    private static FragmentPermissions instance;

    public static synchronized FragmentPermissions newInstance() {
        instance = new FragmentPermissions();
        return instance;
    }

    public static synchronized FragmentPermissions getInstance() {
        if (instance == null)
            instance = new FragmentPermissions();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.hideActionBar(getActivity());
        Utils.hideBottomBar(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_permissions, container, false);
        view.setBackgroundResource(R.drawable.background);

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
}
