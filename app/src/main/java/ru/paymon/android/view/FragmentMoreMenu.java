package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentMoreMenu extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    private static FragmentMoreMenu instance;

    public static synchronized FragmentChat newInstance() {
        return new FragmentChat();
    }

    public static synchronized FragmentMoreMenu getInstance() {
        if (instance == null)
            instance = new FragmentMoreMenu();
        return instance;
    }

    @Override
    public void onResume() {
        super.onResume();

        Utils.setActionBarWithTitle(getActivity(), ApplicationLoader.applicationContext.getString(R.string.title_more));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_more_menu, container, false);

        final NavigationView moreMenu = (NavigationView) view.findViewById(R.id.fragment_more_menu_navigation_view);
        final View headerView = (View) moreMenu.getHeaderView(0);
        final TextView name = (TextView) headerView.findViewById(R.id.fragment_more_menu_header_profile_name_text_view);
        final CircleImageView avatar = (CircleImageView) headerView.findViewById(R.id.fragment_more_menu_header_profile_avatar_image_view);

        moreMenu.setNavigationItemSelectedListener(this);

        headerView.setOnClickListener(view1 ->
                Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentProfile.newInstance(), null));

        name.setText(Utils.formatUserName(User.currentUser));

        RPC.PM_photo photo = new RPC.PM_photo();
        photo.user_id = User.currentUser.id;
        photo.id = User.currentUser.photoID;
        avatar.setPhoto(photo);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.more_menu_profit:
                break;
            case R.id.more_menu_invite:
                break;
            case R.id.more_menu_faq:
                break;
            case R.id.more_menu_settings:
                Utils.replaceFragmentWithAnimationFade(getActivity().getSupportFragmentManager(), FragmentSettings.newInstance(), null);
            break;
        }

        return true;
    }
}
