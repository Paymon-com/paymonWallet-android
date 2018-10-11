package ru.paymon.android.view;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import ru.paymon.android.adapters.MessagesAdapter;
import ru.paymon.android.pagedlib.MessageDiffUtilCallback;
import ru.paymon.android.selection.MessageItemKeyProvider;
import ru.paymon.android.selection.MessageItemLookup;
import ru.paymon.android.viewmodels.ChatViewModel;

public class FragmentChat extends AbsFragmentChat {
    private MessagesAdapter messagesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        chatViewModel = ViewModelProviders.of(this).get(ChatViewModel.class);

        messagesAdapter = new MessagesAdapter(new MessageDiffUtilCallback());
        chatViewModel.getMessages(chatID).observe(this, pagedList -> {
            Log.e("AAAsss", pagedList.size() + " ");
            messagesAdapter.submitList(pagedList);
            selectionTracker = new SelectionTracker.Builder<>(
                    "my-selection-id",
                    messagesRecyclerView,
                    new MessageItemKeyProvider(1, messagesAdapter.getCurrentList()),
                    new MessageItemLookup(messagesRecyclerView),
                    StorageStrategy.createLongStorage()
            ).build();
            messagesAdapter.setSelectionTracker(selectionTracker);
            selectionTracker.addObserver(new SelectionTracker.SelectionObserver() {
                @Override
                public void onItemStateChanged(@NonNull Object key, boolean selected) {
                    super.onItemStateChanged(key, selected);
                }

                @Override
                public void onSelectionRefresh() {
                    super.onSelectionRefresh();
                }

                @Override
                public void onSelectionChanged() {
                    super.onSelectionChanged();
                    if (selectionTracker.hasSelection()) {
                        toolbarView.setVisibility(View.GONE);
                        toolbarViewSelected.setVisibility(View.VISIBLE);
                        selectedItemCount.setText(String.format("Selected item count: %d", selectionTracker.getSelection().size()));//TODO:String
                    } else if (!selectionTracker.hasSelection()) {
                        toolbarView.setVisibility(View.VISIBLE);
                        toolbarViewSelected.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onSelectionRestored() {
                    super.onSelectionRestored();
                }
            });
        });

        messagesRecyclerView.setAdapter(messagesAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


//    private MessagesAdapter.IMessageClickListener iMessageClickListener = new MessagesAdapter.IMessageClickListener() {
//        @Override
//        public void longClick() {
//
//        }
//
//        @Override
//        public void forward(LinkedList<Long> checkedMessageIDs) {
//
//        }
//
//        @Override
//        public void delete(LinkedList<Long> checkedMessageIDs) {
////            for (long msgid : checkedMessageIDs) {
////                if (MessagesManager.getInstance().messages.get(msgid).from_id != User.currentUser.id) {
////                    Toast.makeText(getContext(), R.string.you_can_not_delete_someone_messages, Toast.LENGTH_SHORT).show();
////                    messagesAdapter.deselectAll();
////                    return;
////                }
////            }
////
////            final boolean[] checkPermission = {false};
////            final String[] text = {getString(R.string.delete_for_everyone)};
////            AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
////                    .setMultiChoiceItems(text, checkPermission, (dialogInterface, which, isChecked) -> checkPermission[which] = isChecked)
////                    .setTitle(ApplicationLoader.applicationContext.getString(R.string.want_delete_message))
////                    .setCancelable(false)
////                    .setNegativeButton(getContext().getString(R.string.button_cancel), (dialogInterface, i) -> {
////                    })
////                    .setPositiveButton(getContext().getString(R.string.button_ok), (dialogInterface, i) -> {
////                        if (checkPermission[0]) {
////                            Packet request;
////                            if (!isGroup) {
////                                request = new RPC.PM_deleteDialogMessages();
////                                ((RPC.PM_deleteDialogMessages) request).messageIDs.addAll(checkedMessageIDs);
////                            } else {
////                                request = new RPC.PM_deleteGroupMessages();
////                                ((RPC.PM_deleteGroupMessages) request).messageIDs.addAll(checkedMessageIDs);
////                            }
////
////                            NetworkManager.getInstance().sendRequest(request, (response, error) -> {
////                                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
////                                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), R.string.unable_to_delete_messages, Toast.LENGTH_SHORT).show());
////                                    return;
////                                }
////
////                                if (response instanceof RPC.PM_boolTrue) {
////                                    ApplicationLoader.applicationHandler.post(() -> {
////                                        for (Long msgID : checkedMessageIDs) {
////                                            RPC.Message msg = MessagesManager.getInstance().messages.get(msgID);
////                                            MessagesManager.getInstance().deleteMessage(msg);
////                                            messagesAdapter.messageIDs.remove(msgID);
////                                        }
////                                        messagesAdapter.notifyDataSetChanged();
////                                    });
////                                }
////                            });
////
////                            messagesAdapter.deselectAll();
////                        } else {
////                            //TODO:delete message for user
////                        }
////                    });
////            AlertDialog alertDialog = builder.create();
////            alertDialog.show();
//        }
//    };
}
