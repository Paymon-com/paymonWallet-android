package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
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
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;
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

        CircleImageView avatar = (CircleImageView) view.findViewById(R.id.profile_update_photo);
        TextInputEditText firstName = (TextInputEditText) view.findViewById(R.id.profile_update_name);
        TextInputEditText lastName = (TextInputEditText) view.findViewById(R.id.profile_update_surname);
        TextInputEditText birthday = (TextInputEditText) view.findViewById(R.id.profile_update_bday);
        TextInputEditText phone = (TextInputEditText) view.findViewById(R.id.profile_update_phone_edit_text);
        TextInputEditText city = (TextInputEditText) view.findViewById(R.id.profile_update_city);
        TextInputEditText country = (TextInputEditText) view.findViewById(R.id.profile_update_country);
        TextInputEditText email = (TextInputEditText) view.findViewById(R.id.profile_update_email);
        CheckBox male = (CheckBox) view.findViewById(R.id.profile_update_male);
        CheckBox female = (CheckBox) view.findViewById(R.id.profile_update_female);
        Button saveButton = (Button) view.findViewById(R.id.profile_update_save_button);

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.user_id = User.currentUser.id;
        photo.id = User.currentUser.photoID;
        avatar.setPhoto(photo);

        firstName.setText(User.currentUser.first_name);
        lastName.setText(User.currentUser.last_name);
        email.setText(User.currentUser.email);
        final String phoneNumber = String.valueOf(User.currentUser.phoneNumber);
        if (!phoneNumber.isEmpty() && !phoneNumber.equals("0"))
            phone.setText(String.valueOf(User.currentUser.phoneNumber));
        else
            phone.setText("");
        city.setText(User.currentUser.city);
        birthday.setText(User.currentUser.birthdate);
        country.setText(User.currentUser.country);

        if(User.currentUser.gender == 1) {
            female.setChecked(false);
            male.setChecked(true);
        }else {
            male.setChecked(false);
            female.setChecked(true);
        }

        saveButton.setOnClickListener((view1) -> {
            //TODO:send update profile request
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_update_profile));
        Utils.setArrowBackInToolbar(getActivity());
    }
}
