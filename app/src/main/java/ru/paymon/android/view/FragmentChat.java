package ru.paymon.android.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StorageStrategy;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.adapters.MessagesAdapter;
import ru.paymon.android.net.NetworkManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.pagedlib.MessageDiffUtilCallback;
import ru.paymon.android.selection.MessageItemKeyProvider;
import ru.paymon.android.selection.MessageItemLookup;
import ru.paymon.android.utils.Utils;

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

        messagesAdapter = new MessagesAdapter(new MessageDiffUtilCallback());
        messagesRecyclerView.setAdapter(messagesAdapter);

        selectionTracker = new SelectionTracker.Builder<RPC.Message>(
                "my-selection-id",
                messagesRecyclerView,
                new MessageItemKeyProvider(1, messagesAdapter.items),
                new MessageItemLookup(messagesRecyclerView),
                StorageStrategy.createParcelableStorage(RPC.Message.class)
        ).build();
        messagesAdapter.setSelectionTracker(selectionTracker);

        chatViewModel.getMessages(chatID, false).observe(this, pagedList -> {
            messagesAdapter.submitList(pagedList);

            ApplicationLoader.applicationHandler.postDelayed(() -> { //TODO: переехать на androidx paging там появился callback Для submitlist
                final List<RPC.Message> selectedMessages = Lists.newArrayList(selectionTracker.getSelection().iterator());
                selectionTracker.clearSelection();
                messagesAdapter.items.clear();
                messagesAdapter.items.addAll(messagesAdapter.getCurrentList());
                for (final RPC.Message message : selectedMessages) {
                    final long mid = message.id;
                    for (final RPC.Message msg : messagesAdapter.getCurrentList()) {
                        if (mid == msg.id)
                            selectionTracker.select(msg);
                    }
                }
            }, 200);

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
                        selectedItemCount.setText(getString(R.string.selected_messages_count) + ": " + selectionTracker.getSelection().size());
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

        clearChatSelected.setOnClickListener(v -> selectionTracker.clearSelection());

        delete.setOnClickListener(v -> {
            if (selectionTracker.hasSelection()) {
                final ArrayList<Long> checkedMessageIDs = new ArrayList<>();
                List<RPC.Message> selectedMessages = Lists.newArrayList(selectionTracker.getSelection().iterator());
                for (final RPC.Message message : selectedMessages) {
                    checkedMessageIDs.add(message.id);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AlertDialogCustom))
                        .setMessage(ApplicationLoader.applicationContext.getString(R.string.want_delete_message))
                        .setCancelable(false)
                        .setNegativeButton(getContext().getString(R.string.button_cancel), (dialogInterface, i) -> {
                        })
                        .setPositiveButton(getContext().getString(R.string.button_ok), (dialogInterface, i) -> {
                            RPC.PM_deleteDialogMessages request = new RPC.PM_deleteDialogMessages();
                            request.messageIDs.addAll(checkedMessageIDs);

                            NetworkManager.getInstance().sendRequest(request, (response, error) -> {
                                if (error != null || response == null || response instanceof RPC.PM_boolFalse) {
                                    ApplicationLoader.applicationHandler.post(() -> Toast.makeText(getContext(), R.string.unable_to_delete_messages, Toast.LENGTH_SHORT).show());
                                    return;
                                }

                                if (response instanceof RPC.PM_boolTrue) {
                                    MessagesManager.getInstance().deleteMessages(checkedMessageIDs);
                                }
                            });

                            selectionTracker.clearSelection();
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideKeyboard(getActivity().getWindow().getDecorView().getRootView());
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
