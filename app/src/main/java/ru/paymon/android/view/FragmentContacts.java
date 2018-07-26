package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.adapters.ChatsAdapter;
import ru.paymon.android.adapters.ContactsAdapter;
//import ru.paymon.android.models.ContactsItem;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.RecyclerItemClickListener;
import ru.paymon.android.utils.Utils;

import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;


public class FragmentContacts extends Fragment {
    private static FragmentContacts instance;

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

        LinkedList<RPC.UserObject> listContacts = new LinkedList<>();
        SparseArray<RPC.UserObject> contacts = UsersManager.getInstance().userContacts;
        for (int i = 0; i < contacts.size(); i++) {
            RPC.UserObject contact = contacts.get(contacts.keyAt(i));
            listContacts.add(contact);
        }

        LinkedList<RPC.UserObject> listContactsGlobal = new LinkedList<>();
        SparseArray<RPC.UserObject> contactsGlobal = UsersManager.getInstance().userContacts;
        for (int i = 0; i < contactsGlobal.size(); i++) {
            RPC.UserObject contact = contactsGlobal.get(contactsGlobal.keyAt(i));
            listContactsGlobal.add(contact);
        }

        recyclerViewContacts.setHasFixedSize(true);
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(getContext()));

        ContactsAdapter contactsAdapter = new ContactsAdapter(listContacts, listContactsGlobal);
        recyclerViewContacts.setAdapter(contactsAdapter);

        EditText editText = view.findViewById(R.id.edit_text_contacts);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                LinkedList<RPC.UserObject> sortedContacts = new LinkedList<>();
                LinkedList<RPC.UserObject> sortedContactsGlobal = new LinkedList<>();//TODO: получить запросиком от сервака

                String text = editable.toString();

                if (text.trim().isEmpty()) recyclerViewContacts.setAdapter(contactsAdapter);

                for (RPC.UserObject user : listContacts) {
                    if (user.last_name.toLowerCase().contains(text.toLowerCase()) || user.first_name.toLowerCase().contains(text.toLowerCase()) || user.login.toLowerCase().contains(text.toLowerCase())) {
                        sortedContacts.add(user);
                    }
                }

                for (RPC.UserObject user : listContactsGlobal) {
                    if (user.last_name.toLowerCase().contains(text.toLowerCase()) || user.first_name.toLowerCase().contains(text.toLowerCase()) || user.login.toLowerCase().contains(text.toLowerCase())) {
                        sortedContactsGlobal.add(user);
                    }
                }
                ContactsAdapter chatsSearchAdapter = new ContactsAdapter(sortedContacts, sortedContactsGlobal);
                recyclerViewContacts.setAdapter(chatsSearchAdapter);
            }
        });

        recyclerViewContacts.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewContacts, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Bundle bundle = new Bundle();
                int userID = ((ContactsAdapter) (recyclerViewContacts.getAdapter())).getItem(position).id;
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
        Utils.setActionBarWithTitle(getActivity(), getString(R.string.title_contacts));
        Utils.showBottomBar(getActivity());
        setHasOptionsMenu(true);
    }
}
