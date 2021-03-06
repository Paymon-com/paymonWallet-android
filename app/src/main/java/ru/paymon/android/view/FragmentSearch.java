//package ru.paymon.android.view;
//
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.SparseArray;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.TabHost;
//
//import java.util.LinkedList;
//
//import androidx.navigation.Navigation;
//import ru.paymon.android.GroupsManager;
//import ru.paymon.android.MessagesManager;
//import ru.paymon.android.R;
//import ru.paymon.android.UsersManager;
//import ru.paymon.android.adapters.ChatsSearchAdapter;
//import ru.paymon.android.adapters.MessagesSearchAdapter;
//import ru.paymon.android.models.ChatsSearchItem;
//import ru.paymon.android.models.MessageItem;
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.RecyclerItemClickListener;
//import ru.paymon.android.utils.Utils;
//
//import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;
//
//public class FragmentSearch extends Fragment {
//    private static FragmentSearch instance;
//    private TabHost tabHost;
//    private String currentTabTag;
//    private EditText editText;
//    private RecyclerView recyclerViewChats;
//    private RecyclerView recyclerViewMessages;
//
//    public static synchronized FragmentSearch newInstance() {
//        instance = new FragmentSearch();
//        return instance;
//    }
//
//    public static synchronized FragmentSearch getInstance() {
//        if (instance == null)
//            instance = new FragmentSearch();
//        return instance;
//    }
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_search, container, false);
//        recyclerViewChats = (RecyclerView) view.findViewById(R.id.recViewReg);
//        recyclerViewMessages = (RecyclerView) view.findViewById(R.id.recViewUnreg);
//        editText = view.findViewById(R.id.edit_text_search);
//
//        ImageView backToolbar = (ImageView) view.findViewById(R.id.toolbar_back_btn);
//
//        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());
//
//        LinkedList<ChatsSearchItem> listChats = new LinkedList<>();
//        SparseArray<RPC.UserObject> users = UsersManager.getInstance().users;
//        SparseArray<RPC.Group> groups = GroupsManager.getInstance().groups;
//        for (int i = 0; i < users.size(); i++) {
//            RPC.UserObject user = users.get(users.keyAt(i));
//            listChats.add(new ChatsSearchItem(user.id, Utils.formatUserName(user), user.photoURL, false));
//        }
//        for (int i = 0; i < groups.size(); i++) {
//            RPC.Group group = groups.get(groups.keyAt(i));
//            listChats.add(new ChatsSearchItem(group.id, group.title, group.photoURL, true));
//        }
//        recyclerViewChats.setHasFixedSize(true);
//        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        LinkedList<MessageItem> listMessages = new LinkedList<>();
////        SparseArray<LinkedList<RPC.Message>> chatsDialog = MessagesManager.getInstance().dialogsMessages;
////        SparseArray<LinkedList<RPC.Message>> groupChats = MessagesManager.getInstance().groupsMessages;
//
//        for (int i = 0; i < chatsDialog.size(); i++) {
//            LinkedList<RPC.Message> getChatByChatID = chatsDialog.get(chatsDialog.keyAt(i));
//            int chatID = getChatByChatID.getFirst().from_id;
//            RPC.UserObject user = UsersManager.getInstance().users.get(chatID);
//            if(user != null) {
//                for (int j = 0; j < getChatByChatID.size(); j++) {
//                    RPC.Message message = getChatByChatID.get(j);
//                    listMessages.add(new MessageItem(message.id, Utils.formatUserName(user), message.text, user.photoURL));
//                }
//            }
//        }
//
////        for (int i = 0; i < groupChats.size(); i++) {
////            LinkedList<RPC.Message> groupChat = groupChats.get(groupChats.keyAt(i));
////            int chatID = groupChat.get(0).from_id;
////            RPC.Group group = GroupsManager.getInstance().groups.get(chatID);
////            for (int j = 0; j < groupChat.size(); j++) {
////                RPC.Message message = groupChat.get(j);
////                listMessages.add(new MessagesSearchItem(message.gid, group.title, message.text, group.photoURL));
////            }
////        }
//
//        recyclerViewMessages.setHasFixedSize(true);
//        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        editText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//                if (currentTabTag.equals("Сообщения")) {
//                    LinkedList<MessageItem> sortedMessagesList = new LinkedList<>();
//
//                    String text = editable.toString();
//
//                    if (text.isEmpty()){
//                        recyclerViewMessages.setAdapter(null);
//                        return;
//                    }
//
//                    for (MessageItem message : listMessages) {
//                        if (message.message.toLowerCase().contains(text.toLowerCase())) {
//                            sortedMessagesList.add(message);
//                        }
//                    }
//
//                    MessagesSearchAdapter messagesSearchAdapter = new MessagesSearchAdapter(sortedMessagesList);
//                    recyclerViewMessages.setAdapter(messagesSearchAdapter);
//
//
//                } else if (currentTabTag.equals("Чаты")) {
//                    LinkedList<ChatsSearchItem> sortedChatsList = new LinkedList<>();
//
//                    String text = editable.toString();
//
//                    if (text.isEmpty()){
//                        recyclerViewChats.setAdapter(null);
//                        return;
//                    }
//
//                    for (ChatsSearchItem getChatByChatID : listChats) {
//                        if (getChatByChatID.name.toLowerCase().contains(text.toLowerCase())) {
//                            sortedChatsList.add(getChatByChatID);
//                        }
//                    }
//
//                    ChatsSearchAdapter chatsSearchAdapter = new ChatsSearchAdapter(sortedChatsList);
//                    recyclerViewChats.setAdapter(chatsSearchAdapter);
//                }
//
//            }
//        });
//
//        recyclerViewChats.setHasFixedSize(true);
//        recyclerViewMessages.setHasFixedSize(true);
//        recyclerViewChats.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
//
//
//        tabHost = view.findViewById(R.id.tabHost);
//
//        tabHost.setOnTabChangedListener((tag) -> {
//            currentTabTag = tag;
////            getActivity().invalidateOptionsMenu();
//            editText.setText("");
//            recyclerViewChats.setAdapter(null);
//            recyclerViewMessages.setAdapter(null);
//        });
//
//        recyclerViewChats.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewChats, new RecyclerItemClickListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
//                final Bundle bundle = new Bundle();
//                int chatID = (int) recyclerViewChats.getAdapter().getItemId(position);
//                bundle.putInt(CHAT_ID_KEY, chatID);
//                Navigation.findNavController(getActivity(), R.id.nav_host_fragment).navigate(R.id.fragmentSearch, bundle);
//            }
//
//            @Override
//            public void onLongItemClick(View view, int position) {
//
//            }
//        }));
//
//        tabHost.setup();
//
//        TabHost.TabSpec tabSpec = tabHost.newTabSpec("Сообщения");
//        tabSpec.setContent(R.id.linearLayout2);
//        String messages = getString(R.string.notif_messages);
//        tabSpec.setIndicator(messages);
//        tabHost.addTab(tabSpec);
//
//        tabSpec = tabHost.newTabSpec("Чаты");
//        tabSpec.setContent(R.id.linearLayout);
//        String getChats = getString(R.string.title_chats);
//        tabSpec.setIndicator(getChats);
//        tabHost.addTab(tabSpec);
//        tabHost.setCurrentTab(0);
//
//        return view;
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        if (!editText.getText().toString().equals("")){
//            editText.setText("");
//            recyclerViewChats.setAdapter(null);
//            recyclerViewMessages.setAdapter(null);
//        }
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//}
