//package ru.paymon.android.view;
//
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TabHost;
//
//import ru.paymon.android.ContactsManager;
//import ru.paymon.android.R;
//import ru.paymon.android.adapters.ContactsInviteRegisteredAdapter;
//import ru.paymon.android.adapters.ContactsInviteUnregisteredAdapter;
//import ru.paymon.android.models.Contact;
//import ru.paymon.android.utils.Utils;
//
//public class FragmentContactsInvite extends Fragment {
//    private static final String REG_TAB_TAG = "registered";
//    private static final String UNREG_TAB_TAG = "unregistered";
//    private static FragmentContactsInvite instance;
//    private String currentTabTag;
//    private ContactsInviteRegisteredAdapter contactsInviteRegisteredAdapter;
//    private ContactsInviteUnregisteredAdapter contactsInviteUnregisteredAdapter;
//    private DialogProgress dialogProgress;
//
//    public static synchronized FragmentContactsInvite newInstance() {
//        instance = new FragmentContactsInvite();
//        return instance;
//    }
//
//    public static synchronized FragmentContactsInvite getInstance() {
//        if (instance == null)
//            instance = new FragmentContactsInvite();
//        return instance;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        ContactsManager.newInstance(dialogProgress);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_contacts_invite, container, false);
//
//        TabHost tabHost = view.findViewById(R.id.tabHost);
//        RecyclerView recyclerViewReg = (RecyclerView) view.findViewById(R.id.recViewReg);
//        RecyclerView recyclerViewUnreg = (RecyclerView) view.findViewById(R.id.recViewUnreg);
//
//        dialogProgress = new DialogProgress(getActivity());
//        dialogProgress.setCancelable(true);
//
//        recyclerViewReg.setHasFixedSize(true);
//        recyclerViewReg.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        recyclerViewUnreg.setHasFixedSize(true);
//        recyclerViewUnreg.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        contactsInviteRegisteredAdapter = new ContactsInviteRegisteredAdapter(ContactsManager.getInstance(dialogProgress).registeredContacts);
//        recyclerViewReg.setAdapter(contactsInviteRegisteredAdapter);
//
//        contactsInviteUnregisteredAdapter = new ContactsInviteUnregisteredAdapter(ContactsManager.getInstance(dialogProgress).unregisteredContacts);
//        recyclerViewUnreg.setAdapter(contactsInviteUnregisteredAdapter);
//
//        tabHost.setup();
//
//        TabHost.TabSpec tabSpec = tabHost.newTabSpec(REG_TAB_TAG);
//        tabSpec.setContent(R.id.linearLayout);
//        tabSpec.setIndicator(getString(R.string.invite_registered));
//        tabHost.addTab(tabSpec);
//
//        tabSpec = tabHost.newTabSpec(UNREG_TAB_TAG);
//        tabSpec.setContent(R.id.linearLayout2);
//        tabSpec.setIndicator(getString(R.string.invite_unregistered));
//        tabHost.addTab(tabSpec);
//
//        tabHost.setOnTabChangedListener((tag) -> {
//            currentTabTag = tag;
//            getActivity().invalidateOptionsMenu();
//        });
//
//        tabHost.setCurrentTab(1);
//        tabHost.setCurrentTabByTag(REG_TAB_TAG);
//        return view;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        Utils.hideBottomBar(getActivity());
//        //Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_contacts));
//        //Utils.setArrowBackInToolbar(getActivity());
//        setHasOptionsMenu(true);
//    }
//
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        selectMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.inviteReg:
//                inviteReg();
//                break;
//            case R.id.inviteUnreg:
//                inviteUnreg();
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public void onPrepareOptionsMenu(Menu menu) {
//        selectMenu(menu);
//        super.onPrepareOptionsMenu(menu);
//    }
//
//    private void selectMenu(Menu menu) {
//        if (currentTabTag == null) return;
//        menu.clear();
//        MenuInflater inflater = getActivity().getMenuInflater();
//
//        if (currentTabTag.equals(REG_TAB_TAG)) {
//            inflater.inflate(R.menu.contacts_reg_invite_menu, menu);
//        } else if (currentTabTag.equals(UNREG_TAB_TAG)) {
//            inflater.inflate(R.menu.contacts_unreg_invite_menu, menu);
//        }
//    }
//
//    private void inviteReg() {
//        for (Contact contact: contactsInviteRegisteredAdapter.registeredContacts) {
//            if(contact.isChecked){
//                //TODO:send invite request
//            }
//        }
//    }
//
//    private void inviteUnreg() {
//        for (Contact contact: contactsInviteUnregisteredAdapter.unregisteredContacts) {
//            if(contact.isChecked){
//                //TODO:send invite request
//            }
//        }
//    }
//}
