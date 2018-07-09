package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.ContactsManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.ContactsInviteRegisteredAdapter;
import ru.paymon.android.adapters.ContactsInviteUnregisteredAdapter;
import ru.paymon.android.utils.Utils;

public class FragmentContactsInvite extends Fragment {
    private static final String regTabTag = "registered";
    private static final String unregTabTag = "unregistered";
    private static FragmentContactsInvite instance;
    private TabHost tabHost;
    private String currentTabTag;

    public static synchronized FragmentContactsInvite newInstance() {
        instance = new FragmentContactsInvite();
        return instance;
    }

    public static synchronized FragmentContactsInvite getInstance() {
        if (instance == null)
            instance = new FragmentContactsInvite();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_invite, container, false);

        RecyclerView recyclerViewReg = (RecyclerView) view.findViewById(R.id.recViewReg);
        RecyclerView recyclerViewUnreg = (RecyclerView) view.findViewById(R.id.recViewUnreg);



        recyclerViewReg.setHasFixedSize(true);
        recyclerViewUnreg.setHasFixedSize(true);
        recyclerViewReg.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewUnreg.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactsInviteRegisteredAdapter contactsInviteRegisteredAdapter = new ContactsInviteRegisteredAdapter(ContactsManager.getInstance().getRegistered());
        ContactsInviteUnregisteredAdapter contactsInviteUnregisteredAdapter = new ContactsInviteUnregisteredAdapter(ContactsManager.getInstance().getUnregistered());

        recyclerViewReg.setAdapter(contactsInviteRegisteredAdapter);
        recyclerViewUnreg.setAdapter(contactsInviteUnregisteredAdapter);

        tabHost = view.findViewById(R.id.tabHost);

        tabHost.setOnTabChangedListener((tag) -> {
            currentTabTag = tag;
            getActivity().invalidateOptionsMenu();
        });

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec(unregTabTag);
        tabSpec.setContent(R.id.linearLayout2);
        String inviteUnregistered = getString(R.string.invite_unregistered);
        tabSpec.setIndicator(inviteUnregistered);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec(regTabTag);
        tabSpec.setContent(R.id.linearLayout);
        String inviteRegistered = getString(R.string.invite_registered);
        tabSpec.setIndicator(inviteRegistered);
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(0);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_contacts));
        Utils.setArrowBackInToolbar(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        selectMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        System.out.println(currentTabTag);
        switch (item.getItemId()) {
            case R.id.inviteReg:
                invite();
                break;
            case R.id.inviteUnreg:
                invite();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        selectMenu(menu);
        super.onPrepareOptionsMenu(menu);
    }

    private void selectMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getActivity().getMenuInflater();

        if (currentTabTag.equals(regTabTag)) {
            inflater.inflate(R.menu.contacts_reg_invite_menu, menu);
        } else if (currentTabTag.equals(unregTabTag)) {
            inflater.inflate(R.menu.contacts_unreg_invite_menu, menu);
        }
    }

    private void invite() {
        if (currentTabTag.equals(regTabTag)) {
            System.out.println("THIS IS REG");
        } else if (currentTabTag.equals(unregTabTag)) {
            System.out.println("THIS IS UNEG");
        }
    }
}
