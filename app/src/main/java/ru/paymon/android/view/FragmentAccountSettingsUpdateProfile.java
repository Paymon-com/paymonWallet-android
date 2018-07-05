package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

import ru.paymon.android.R;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.utils.Utils;

public class FragmentAccountSettingsUpdateProfile extends Fragment {
    private static FragmentAccountSettingsUpdateProfile instance;

    public static synchronized FragmentAccountSettingsUpdateProfile newInstance(){
        instance = new FragmentAccountSettingsUpdateProfile();
        return instance;
    }

    public static synchronized FragmentAccountSettingsUpdateProfile getInstance(){
        if (instance == null)
            instance = new FragmentAccountSettingsUpdateProfile();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
