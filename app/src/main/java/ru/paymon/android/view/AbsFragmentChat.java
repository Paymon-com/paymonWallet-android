package ru.paymon.android.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import java.util.concurrent.Executor;

import androidx.navigation.Navigation;
import androidx.recyclerview.selection.SelectionTracker;
import ru.paymon.android.ApplicationLoader;
import ru.paymon.android.GroupsManager;
import ru.paymon.android.MessagesManager;
import ru.paymon.android.R;
import ru.paymon.android.UsersManager;
import ru.paymon.android.net.RPC;
import ru.paymon.android.test.ChatMessageDao;
import ru.paymon.android.utils.ImagePicker;
import ru.paymon.android.utils.Utils;
import ru.paymon.android.viewmodels.ChatViewModel;

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
    public LinearLayout toolbarContainer;
    public View toolbarView;
    public View toolbarViewSelected;
    public TextView chatTitleTextView;
    public CircularImageView toolbarAvatar;
    public ImageView backToolbar;
    public TextView participantsCountTextView;
    public TextView selectedItemCount;
    public TextView delete;
    public TextView copy;
    public SelectionTracker selectionTracker;
    public ChatViewModel chatViewModel;
    public ChatMessageDao dao = ApplicationLoader.db.chatMessageDao();

    public int chatID;

    public class MainThreadExecutor implements Executor {

        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            handler.post(runnable);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageInput = (EmojiEditText) view.findViewById(R.id.input_edit_text);
        messagesRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recview);
        sendButton = (Button) view.findViewById(R.id.sendButton);
        emoticonsButton = (ImageView) view.findViewById(R.id.smilesButton);
        includeAttachment = (ConstraintLayout) view.findViewById(R.id.fragment_chat_attachment_include);
        buttonAttachment = (ImageButton) view.findViewById(R.id.attach_button);
        buttonDocumentAttachment = (ImageButton) view.findViewById(R.id.document_chat_attachment);
        buttonImageAttachment = (ImageButton) view.findViewById(R.id.image_chat_attachment);
        buttonVideoAttachment = (ImageButton) view.findViewById(R.id.video_chat_attachment);
        toolbarContainer = (LinearLayout) view.findViewById(R.id.toolbar_container);

        final Bundle bundle = new Bundle();
        bundle.putInt(CHAT_ID_KEY, chatID);
        if (this instanceof FragmentChat) {
            toolbarView = getLayoutInflater().inflate(R.layout.toolbar_chat, null);
            chatTitleTextView = (TextView) toolbarView.findViewById(R.id.toolbar_title);
            toolbarAvatar = (CircularImageView) toolbarView.findViewById(R.id.toolbar_avatar);
            backToolbar = (ImageView) toolbarView.findViewById(R.id.toolbar_back_btn);
            final RPC.UserObject user = UsersManager.getInstance().users.get(chatID);
            if (user != null) {
                chatTitleTextView.setText(Utils.formatUserName(user));
                if (!user.photoURL.url.isEmpty())
                    Utils.loadPhoto(user.photoURL.url, toolbarAvatar);
            }
            toolbarView.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentFriendProfile, bundle));
        } else if (this instanceof FragmentGroupChat) {
            toolbarView = getLayoutInflater().inflate(R.layout.toolbar_chat_group, null);
            participantsCountTextView = (TextView) toolbarView.findViewById(R.id.participants_count);
            chatTitleTextView = (TextView) toolbarView.findViewById(R.id.toolbar_title);
            toolbarAvatar = (CircularImageView) toolbarView.findViewById(R.id.chat_group_avatar);
            backToolbar = (ImageView) toolbarView.findViewById(R.id.toolbar_back_btn);
            final RPC.Group group = GroupsManager.getInstance().groups.get(chatID);
            if (group != null) {
                chatTitleTextView.setText(group.title);
                participantsCountTextView.setText(String.format("%s: %d", getString(R.string.participants), ((FragmentGroupChat) this).groupUsers.size()));
                if (!group.photoURL.url.isEmpty())
                    Utils.loadPhoto(group.photoURL.url, toolbarAvatar);
            }
            toolbarView.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.fragmentGroupSettings, bundle));
        }

        toolbarViewSelected = getLayoutInflater().inflate(R.layout.toolbar_chat_selected, null);
        selectedItemCount = toolbarViewSelected.findViewById(R.id.toolbar_chat_selected_count);
        delete = toolbarViewSelected.findViewById(R.id.toolbar_chat_selected_delete);
        delete = toolbarViewSelected.findViewById(R.id.toolbar_chat_selected_copy);
        toolbarViewSelected.setVisibility(View.GONE);

        toolbarContainer.addView(toolbarView);
        toolbarContainer.addView(toolbarViewSelected);
        backToolbar.setOnClickListener(v -> Navigation.findNavController(getActivity(), R.id.nav_host_fragment).popBackStack());

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
        linearLayoutManager.setReverseLayout(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Utils.hideBottomBar(getActivity());
        MessagesManager.getInstance().currentChatID = chatID;
    }

    @Override
    public void onPause() {
        super.onPause();
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
