package ru.paymon.android.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.paymon.android.components.CircularImageView;

import androidx.navigation.Navigation;
import ru.paymon.android.R;
import ru.paymon.android.User;
import ru.paymon.android.utils.Utils;

public class FragmentMoreMenu extends Fragment implements NavigationView.OnNavigationItemSelectedListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_more_menu, container, false);
        final NavigationView moreMenu = (NavigationView) view.findViewById(R.id.fragment_more_menu_navigation_view);
        final View headerView = (View) moreMenu.getHeaderView(0);
        final TextView name = (TextView) headerView.findViewById(R.id.fragment_more_menu_header_profile_name_text_view);
        final CircularImageView avatar = (CircularImageView) headerView.findViewById(R.id.fragment_more_menu_header_profile_avatar_image_view);

        moreMenu.setNavigationItemSelectedListener(this);
        headerView.setOnClickListener(view1 -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentProfile));
        name.setText(Utils.formatUserName(User.currentUser));
        if (!User.currentUser.photoURL.url.isEmpty())
            Utils.loadPhoto(User.currentUser.photoURL.url, avatar);

        return view;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();
        switch (itemId) {
            case R.id.more_menu_profit:
                Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage("com.example.raing.profitbeta");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?gid=com.vkontakte.android")));//TODO:Вместо вконтактика вставить ссылку на релизнутый Profit в гугл приложении
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?gid=com.vkontakte.android")));//TODO:Вместо вконтактика вставить ссылку на релизнутый Profit в гугл приложении
                    }
                }
                break;
            case R.id.more_menu_invite:
//                if (ContextCompat.checkSelfPermission(ApplicationLoader.applicationContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//                    Utils.replaceFragmentWithAnimationSlideFade(getActivity().getSupportFragmentManager(), FragmentContactsInvite.newInstance(), null);
//                } else {
//                    ((MainActivity) getActivity()).requestAppPermissions(new String[]{Manifest.permission.READ_CONTACTS},
//                            R.string.msg_permissions_required, READ_CONTACTS_PERMISSION);
//                }
                break;
            case R.id.more_menu_faq:
                break;
            case R.id.more_menu_settings:
                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.settingsActivity);
                break;
            case R.id.bottom_menu_games:
                break;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.showBottomBar(getActivity());
    }
}
