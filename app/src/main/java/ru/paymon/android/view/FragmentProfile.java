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

import androidx.navigation.Navigation;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircularImageView;
import ru.paymon.android.utils.Utils;

public class FragmentProfile extends Fragment {

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
        TextView login = (TextView) view.findViewById(R.id.login_profile_text_view);
        TextView publicAddressBTC = (TextView) view.findViewById(R.id.fragment_profile_public_btc_address_text_view);
        TextView publicAddressETH = (TextView) view.findViewById(R.id.fragment_profile_public_eth_address_text_view);
        TextView publicAddressPMNT = (TextView) view.findViewById(R.id.fragment_profile_public_pmnt_address_text_view);
        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);

        backToolbar.setOnClickListener(view1 -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
        updateProfile.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentProfileEdit));

        name.setText(Utils.formatUserName(User.currentUser));
        login.setText(String.format("@%s", User.currentUser.login));
        publicAddressBTC.setText(User.currentUser.btcAddress);
        publicAddressETH.setText(User.currentUser.ethAddress);
        publicAddressPMNT.setText(User.currentUser.pmntAddress);

        if (publicAddressBTC.getText().toString().isEmpty())
            publicAddressBTC.setText(R.string.user_profile_not_specified);

        if (publicAddressETH.getText().toString().isEmpty())
            publicAddressETH.setText(R.string.user_profile_not_specified);

        if (publicAddressPMNT.getText().toString().isEmpty())
            publicAddressPMNT.setText(R.string.user_profile_not_specified);

        if (!User.currentUser.photoURL.url.isEmpty())
            Utils.loadPhoto(User.currentUser.photoURL.url, avatar);

        if (User.currentUser.email != null && !User.currentUser.email.equals(""))
            email.setText(User.currentUser.email);
        else
            email.setText(R.string.user_profile_not_specified);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
    }
}
