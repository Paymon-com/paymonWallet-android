package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.R;
import ru.paymon.android.adapters.ContactsGlobalAdapter;
import ru.paymon.android.components.CustomSearchView;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.utils.Utils;

public class FragmentContacts extends Fragment {
    private DialogProgress dialogProgress;
    private ContactsGlobalAdapter contactsGlobalAdapter;
    private ImageView contactsImage;
    private TextView contactsText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        RecyclerView recyclerViewContactsGlobal = (RecyclerView) view.findViewById(R.id.recViewContactsGlobal);
        CustomSearchView searchView = view.findViewById(R.id.edit_text_contacts_search2);

        contactsImage = (ImageView) view.findViewById(R.id.fragment_contacts_image_imageView);
        contactsText = (TextView) view.findViewById(R.id.fragment_contacts_text_textView);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerViewContactsGlobal.getContext(), (new LinearLayoutManager(getContext())).getOrientation());
        recyclerViewContactsGlobal.addItemDecoration(dividerItemDecoration);
        recyclerViewContactsGlobal.setHasFixedSize(true);
        recyclerViewContactsGlobal.setLayoutManager(new LinearLayoutManager(getContext()));

        contactsGlobalAdapter = new ContactsGlobalAdapter();
        recyclerViewContactsGlobal.setAdapter(contactsGlobalAdapter);

        dialogProgress = new DialogProgress(getContext());
        dialogProgress.setCancelable(true);

        ImageView clearButton = (ImageView) searchView.findViewById(R.id.search_close_btn);
        EditText searchText = (EditText) searchView.findViewById(R.id.search_src_text);
        clearButton.setOnClickListener(v -> {
            searchText.setText("");
            searchView.setQuery("", true);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            long requestID;
            long lastTimeSend;

            @Override
            public boolean onQueryTextSubmit(String searchText) {
                Utils.netQueue.postRunnable(() -> {
                    if (requestID != 0)
                        NetworkManager.getInstance().cancelRequest(requestID, false);

                    RPC.PM_searchContact packet = new RPC.PM_searchContact();
                    packet.query = searchText;
                    lastTimeSend = System.currentTimeMillis();
                    requestID = NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                        if (error != null || response == null)
                            ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), R.string.import_export_keys_dialog_failure_title, Toast.LENGTH_SHORT).show());//TODO string

                        if (response instanceof RPC.PM_users) {
                            RPC.PM_users received = (RPC.PM_users) response;
                            ArrayList<RPC.UserObject> users = received.users;
                            contactsGlobalAdapter.contactsGlobalItems.clear();
                            contactsGlobalAdapter.contactsGlobalItems.addAll(users);
                            ApplicationLoader.applicationHandler.post(() -> contactsGlobalAdapter.notifyDataSetChanged());
                        }

                    });
                    ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
                });


                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                if (searchText.isEmpty()) {
                    contactsGlobalAdapter.contactsGlobalItems.clear();
                    contactsGlobalAdapter.notifyDataSetChanged();
                    contactsImage.setVisibility(View.VISIBLE);
                    contactsText.setVisibility(View.VISIBLE);
                } else {
                    contactsImage.setVisibility(View.GONE);
                    contactsText.setVisibility(View.GONE);
                    if (System.currentTimeMillis() - lastTimeSend >= 1500) {
                        Utils.netQueue.postRunnable(() -> {
                            if (requestID != 0)
                                NetworkManager.getInstance().cancelRequest(requestID, false);

                            RPC.PM_searchContact packet = new RPC.PM_searchContact();
                            packet.query = searchText;
                            lastTimeSend = System.currentTimeMillis();
                            requestID = NetworkManager.getInstance().sendRequest(packet, (response, error) -> {
                                if (error != null || response == null)
                                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), R.string.import_export_keys_dialog_failure_title, Toast.LENGTH_SHORT).show());//TODO string

                                if (response instanceof RPC.PM_users) {
                                    RPC.PM_users received = (RPC.PM_users) response;
                                    ArrayList<RPC.UserObject> users = received.users;
                                    contactsGlobalAdapter.contactsGlobalItems.clear();
                                    contactsGlobalAdapter.contactsGlobalItems.addAll(users);
                                    ApplicationLoader.applicationHandler.post(() -> contactsGlobalAdapter.notifyDataSetChanged());
                                }

                            });
                            ApplicationLoader.applicationHandler.post(() -> dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
                        });
                    }
                }
                return false;
            }
        });

//        recyclerViewContactsGlobal.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewContactsGlobal, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                final Bundle bundle = new Bundle();
//                int userID = (int) recyclerViewContactsGlobal.getAdapter().getItemId(position);
//                bundle.putInt(CHAT_ID_KEY, userID);
//                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentFriendProfile, bundle);
//            }
//
//            @Override
//            public void onLongItemClick(View view, int position) {
//
//            }
//        }));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideActionBar(getActivity());
        Utils.showBottomBar(getActivity());
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
