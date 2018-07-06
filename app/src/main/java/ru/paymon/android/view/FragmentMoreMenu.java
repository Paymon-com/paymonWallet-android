package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.components.CircleImageView;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.Config.TAG;

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

        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_more));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_more_menu, container, false);

        final NavigationView moreMenu = view.findViewById(R.id.fragment_more_menu_navigation_view);
        final View headerView = moreMenu.getHeaderView(0);
        final TextView name = headerView.findViewById(R.id.more_menu_header_profile_name_text_view);
        final CircleImageView avatar = headerView.findViewById(R.id.more_menu_header_profile_avatar_image_view);

        moreMenu.setNavigationItemSelectedListener(this);

        headerView.setOnClickListener(view1 -> {
            //TODO Вызвать вью профиля
            Log.d(TAG, "onCreateView: " + "to profile");
        });

        name.setText(Utils.formatUserName(User.currentUser));

        //TODO: установка фото
        avatar.setImageResource(R.drawable.ic_yandex);

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
                Log.d(TAG, "onCreateView: " + "to profit");

                break;
            case R.id.more_menu_invite:
                Log.d(TAG, "onCreateView: " + "to invite");

                break;
            case R.id.more_menu_faq:
                Log.d(TAG, "onCreateView: " + "to faq");

        }

        return true;
    }
}
