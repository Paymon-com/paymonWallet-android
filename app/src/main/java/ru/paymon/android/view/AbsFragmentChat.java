package ru.paymon.android.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.NotificationManager;
import ru.paymon.android.utils.ImagePicker;
import ru.paymon.android.utils.Utils;

public abstract class AbsFragmentChat extends Fragment {
    public static final String CHAT_ID_KEY = "CHAT_ID_KEY";
    public static final String CHAT_GROUP_USERS = "CHAT_USERS";
    public final int PICK_IMAGE_ID = 100;
    public final int PICK_VIDEO_ID = 99;
    public final int PICK_DOCUMENT_ID = 98;

    public RecyclerView messagesRecyclerView;
    public EmojiEditText messageInput;
    public Button sendButton;
    public ImageView emoticonsButton;
    public ConstraintLayout includeAttachment;
    public ImageButton buttonAttachment;
    public ImageButton buttonDocumentAttachment;
    public ImageButton buttonImageAttachment;
    public ImageButton buttonVideoAttachment;

    public int chatID;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        buttonDocumentAttachment.setOnClickListener(view13 -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/myFolder/");
            intent.setDataAndType(uri, "*/*");
            startActivityForResult(Intent.createChooser(intent, "Open folder"), PICK_DOCUMENT_ID);
            includeAttachment.setVisibility(View.GONE);
        });

        buttonImageAttachment.setOnClickListener(view12 -> {
            Intent chooseImageIntent = ImagePicker.getPickImageIntent(ApplicationLoader.applicationContext, "Выберите");//TODO:string
            startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            includeAttachment.setVisibility(View.GONE);
        });

        buttonVideoAttachment.setOnClickListener(view14 -> {
            Intent chooseVideoIntent = new Intent(Intent.ACTION_PICK);
            chooseVideoIntent.setType("video/*");
            startActivityForResult(chooseVideoIntent, PICK_VIDEO_ID);
            includeAttachment.setVisibility(View.GONE);
        });

        buttonAttachment.setOnClickListener((view1) -> {
            if (includeAttachment.getVisibility() == View.GONE) {
                includeAttachment.setVisibility(View.VISIBLE);
            } else {
                includeAttachment.setVisibility(View.GONE);
            }
        });

        final EmojiPopup emojiPopup = EmojiPopup.Builder.fromRootView(emoticonsButton).build(messageInput);
        emoticonsButton.setOnClickListener((view1) -> emojiPopup.toggle());

        messagesRecyclerView.setHasFixedSize(true);
        messagesRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        return new View(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManager.getInstance().addObserver(this, NotificationManager.NotificationEvent.chatAddMessages);
        Utils.hideBottomBar(getActivity());
        MessagesManager.getInstance().currentChatID = chatID;
    }

    @Override
    public void onPause() {
        super.onPause();
        NotificationManager.getInstance().removeObserver(this, NotificationManager.NotificationEvent.chatAddMessages);
        MessagesManager.getInstance().currentChatID = 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_IMAGE_ID:
                    Bitmap bitmap = ImagePicker.getImageFromResult(ApplicationLoader.applicationContext, requestCode, resultCode, data);
                    //TODO:Работа с картинками
                    break;
                case PICK_DOCUMENT_ID:
                    //TODO:Работа с документами
                    break;
                case PICK_VIDEO_ID:
                    //TODO:Работа с видео
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
