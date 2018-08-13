package ru.paymon.android.view;

import android.os.Bundle;
import android.os.UserManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.ContactsAdapter;
import ru.paymon.android.adapters.ContactsGlobalAdapter;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.RecyclerItemClickListener;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;

//import ru.paymon.android.models.ContactsItem;


public class FragmentContacts extends Fragment {
    private static FragmentContacts instance;
    private DialogProgress dialogProgress;
    private String sombody;

    public static synchronized FragmentContacts newInstance() {
        instance = new FragmentContacts();
        return instance;
    }

    public static synchronized FragmentContacts getInstance() {
        if (instance == null)
            instance = new FragmentContacts();
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        RecyclerView recyclerViewContacts = (RecyclerView) view.findViewById(R.id.recViewContacts);
        RecyclerView recyclerViewContactsGlobal = (RecyclerView) view.findViewById(R.id.recViewContactsGlobal);
        EditText editText = view.findViewById(R.id.edit_text_contacts_search);
        SearchView searchView = view.findViewById(R.id.edit_text_contacts_search2);

        recyclerViewContacts.setHasFixedSize(true);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerViewContactsGlobal.setHasFixedSize(true);
        recyclerViewContactsGlobal.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactsAdapter contactsAdapter = new ContactsAdapter();
        recyclerViewContacts.setAdapter(contactsAdapter);

        ContactsGlobalAdapter contactsGlobalAdapter = new ContactsGlobalAdapter();
        recyclerViewContactsGlobal.setAdapter(contactsGlobalAdapter);

        dialogProgress = new DialogProgress(getActivity());
        dialogProgress.setCancelable(true);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String text = editable.toString().toLowerCase();
                contactsAdapter.contactsItems.clear();
                for (int i = 0; i < UsersManager.getInstance().userContacts.size(); i++) {
                    RPC.UserObject user = UsersManager.getInstance().userContacts.get(UsersManager.getInstance().userContacts.keyAt(i));
                    if (user.first_name.toLowerCase().contains(text) || user.last_name.toLowerCase().contains(text) || user.login.toLowerCase().contains(text)) {
                        contactsAdapter.contactsItems.add(user);
                    }
                }
                contactsAdapter.notifyDataSetChanged();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            long requestID;
            long lastTimeSend;

            @Override
            public boolean onQueryTextSubmit(String searchText) {
                Utils.netQueue.postRunnable(() -> {
                    if(requestID != 0)
                        NetworkManager.getInstance().cancelRequest(requestID,false);

                    RPC.PM_searchContact packet = new RPC.PM_searchContact();
                    packet.query = searchText;
                    lastTimeSend = System.currentTimeMillis();
                    requestID = NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                        if (error != null || response == null)
                            ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_SHORT).show());//TODO string

                        if (response != null && response instanceof RPC.PM_users) {
                            RPC.PM_users received = (RPC.PM_users) response;
                            ArrayList<RPC.UserObject> users = received.users;
                            contactsGlobalAdapter.contactsGlobalItems.clear();
                            for (int i = 0; i < users.size(); i++) {
                                RPC.UserObject userGlobal = users.get(i);
                                if (userGlobal.first_name.toLowerCase().contains(searchText) || userGlobal.last_name.toLowerCase().contains(searchText) || userGlobal.login.toLowerCase().contains(searchText)) {
                                    contactsGlobalAdapter.contactsGlobalItems.add(userGlobal);
                                }
                            }
                            ApplicationLoader.applicationHandler.post(() -> contactsGlobalAdapter.notifyDataSetChanged());
                        }

                    });
                    ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
                });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if(searchText.isEmpty()){
                    contactsGlobalAdapter.contactsGlobalItems.clear();
                    contactsGlobalAdapter.notifyDataSetChanged();
                }
                if(System.currentTimeMillis() - lastTimeSend >= 1500){
                    Utils.netQueue.postRunnable(() -> {
                        if(requestID != 0)
                            NetworkManager.getInstance().cancelRequest(requestID,false);

                        RPC.PM_searchContact packet = new RPC.PM_searchContact();
                        packet.query = searchText;
                        lastTimeSend = System.currentTimeMillis();
                        requestID = NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                            if (error != null || response == null)
                                ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), "Ошибка", Toast.LENGTH_SHORT).show());//TODO string

                            if (response != null && response instanceof RPC.PM_users) {
                                RPC.PM_users received = (RPC.PM_users) response;
                                ArrayList<RPC.UserObject> users = received.users;
                                contactsGlobalAdapter.contactsGlobalItems.clear();
                                for (int i = 0; i < users.size(); i++) {
                                    RPC.UserObject userGlobal = users.get(i);
                                    if (userGlobal.first_name.toLowerCase().contains(searchText) || userGlobal.last_name.toLowerCase().contains(searchText) || userGlobal.login.toLowerCase().contains(searchText)) {
                                        contactsGlobalAdapter.contactsGlobalItems.add(userGlobal);
                                    }
                                }
                                ApplicationLoader.applicationHandler.post(() -> contactsGlobalAdapter.notifyDataSetChanged());
                            }

                        });
                        ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
                    });
                }

                return false;
            }
        });

        TabHost tabHost = view.findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Контакты");
        tabSpec.setContent(R.id.linearLayout);
        String contactTab = "Контакты";
        tabSpec.setIndicator(contactTab);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Глоабльный поиск");
        tabSpec.setContent(R.id.linearLayout2);
        String contactsGlobalTab = "Глоабльный поиск";
        tabSpec.setIndicator(contactsGlobalTab);
        tabHost.addTab(tabSpec);
        tabHost.setCurrentTab(0);


        recyclerViewContacts.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewContacts, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Bundle bundle = new Bundle();
                int userID = (int) recyclerViewContacts.getAdapter().getItemId(position);
                bundle.putInt(CHAT_ID_KEY, userID);
                final FragmentFriendProfile fragmentFriendProfile = FragmentFriendProfile.newInstance();
                fragmentFriendProfile.setArguments(bundle);
                final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentFriendProfile, null);
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideActionBar(getActivity());
//        //Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_contacts));
        Utils.showBottomBar(getActivity());
        setHasOptionsMenu(true);
    }

}
