package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import ru.paymon.android.R;
import ru.paymon.android.User;

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
        CircularImageView avatar = (CircularImageView) view.findViewById(R.id.profile_avatar_image_view);
        TextView name = (TextView) view.findViewById(R.id.name_profile_text_view);
        TextView email = (TextView) view.findViewById(R.id.profile_email_text_view);
        TextView phone = (TextView) view.findViewById(R.id.profile_phone_text_view);
        TextView city = (TextView) view.findViewById(R.id.profile_city_text_view);
        TextView birthday = (TextView) view.findViewById(R.id.profile_bday_text_view);
        TextView country = (TextView) view.findViewById(R.id.profile_country_text_view);
        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view1 -> getActivity().getSupportFragmentManager().popBackStack());

        updateProfile.setOnClickListener(v ->
                Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), FragmentProfileEdit.newInstance(), null));

        name.setText(Utils.formatUserName(User.currentUser));

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.user_id = User.currentUser.id;
        photo.id = User.currentUser.photoID;
//        avatar.setPhoto(photo);

        if (!User.currentUser.email.isEmpty())
            email.setText(User.currentUser.email);
        else
            email.setText(R.string.not_specified);
        if (User.currentUser.phoneNumber != 0)
            phone.setText(Utils.formatPhone(User.currentUser.phoneNumber));
        else
            phone.setText(R.string.not_specified);
        if (!User.currentUser.city.isEmpty())
            city.setText(User.currentUser.city);
        else
            city.setText(R.string.not_specified);
        if (!User.currentUser.birthdate.isEmpty())
            birthday.setText(User.currentUser.birthdate);
        else
            birthday.setText(R.string.not_specified);
        if (!User.currentUser.country.isEmpty())
            country.setText(User.currentUser.country);
        else
            country.setText(R.string.not_specified);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //Utils.setActionBarWithTitle(getActivity(), "Профиль"); //TODO: string
        //Utils.setArrowBackInToolbar(getActivity());
        Utils.hideBottomBar(getActivity());
    }
}
