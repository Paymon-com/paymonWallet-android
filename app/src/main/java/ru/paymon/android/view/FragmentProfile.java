package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentProfile extends Fragment {
    private static FragmentProfile instance;

    public static synchronized FragmentProfile newInstance() {
        instance = new FragmentProfile();
        return instance;
    }

    public static synchronized FragmentProfile getInstance() {
        if (instance == null)
            instance = new FragmentProfile();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ImageView updateProfile = (ImageView) view.findViewById(R.id.profile_update_button);
        CircleImageView avatar = (CircleImageView) view.findViewById(R.id.profile_avatar_image_view);
        TextView name = (TextView) view.findViewById(R.id.name_profile_text_view);
        TextView email = (TextView) view.findViewById(R.id.profile_email_text_view);
        TextView phone = (TextView) view.findViewById(R.id.profile_phone_text_view);
        TextView city = (TextView) view.findViewById(R.id.profile_city_text_view);
        TextView birthday = (TextView) view.findViewById(R.id.profile_bday_text_view);
        TextView country = (TextView) view.findViewById(R.id.profile_country_text_view);

        updateProfile.setOnClickListener(v ->
                Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), FragmentAccountSettingsUpdateProfile.newInstance(), null));

        name.setText(Utils.formatUserName(User.currentUser));

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.user_id = User.currentUser.id;
        photo.id = User.currentUser.photoID;
        avatar.setPhoto(photo);

        email.setText(User.currentUser.email);
        final String phoneNumber = String.valueOf(User.currentUser.phoneNumber);
        if (!phoneNumber.isEmpty() && !phoneNumber.equals("0"))
            phone.setText(String.valueOf(User.currentUser.phoneNumber));
        else
            phone.setText("Не указан");//TODO:string
        city.setText(User.currentUser.city);
        birthday.setText(User.currentUser.birthdate);
        country.setText(User.currentUser.country);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), "Профиль"); //TODO: string
        Utils.setArrowBackInToolbar(getActivity());
    }
}
