//package ru.paymon.android.adapters;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.v4.app.FragmentManager;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import ru.paymon.android.components.CircularImageView;
//
//import java.util.LinkedList;
//
//import ru.paymon.android.R;
//import ru.paymon.android.User;
//import ru.paymon.android.models.UserItem;
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.Utils;
//import ru.paymon.android.components.DialogProgress;
//import ru.paymon.android.view.FragmentFriendProfile;
//
//import static ru.paymon.android.view.FragmentChat.CHAT_ID_KEY;
//
//
//public class BlackListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private Context context;
//    private LinkedList<UserItem> list;
//    private boolean isCreator;
//    private int chatID;
//    private RPC.Group group;
//    private DialogProgress dialogProgress;
//
//    public BlackListAdapter(LinkedList<UserItem> list, int chatID, int creatorID, DialogProgress dialogProgress) {
//        this.list = list;
//        this.chatID = chatID;
//        this.dialogProgress = dialogProgress;
//        isCreator = creatorID == User.currentUser.id;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_group_settings, parent, false);
//        AlertDialogBlackListViewHolder holder = new AlertDialogBlackListViewHolder(view);
//        context = holder.itemView.getContext();
//        return new AlertDialogBlackListViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        UserItem userItem = list.get(position);
//        AlertDialogBlackListViewHolder alertDialogBlackListViewHolder = (AlertDialogBlackListViewHolder) holder;
//
//        alertDialogBlackListViewHolder.name.setText(userItem.name);
//
//        if (!userItem.photo.url.isEmpty())
//            Utils.loadPhoto(userItem.photo.url, alertDialogBlackListViewHolder.photo);
//
//        if (userItem.uid == User.currentUser.id)
//            alertDialogBlackListViewHolder.removeButton.setVisibility(View.GONE);
//
//        alertDialogBlackListViewHolder.removeButton.setOnClickListener((view) ->
//        {
//            //TODO:удаление
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    private class AlertDialogBlackListViewHolder extends RecyclerView.ViewHolder {
//        private TextView name;
//        private CircularImageView photo;
//        private ImageView removeButton;
//
//        private AlertDialogBlackListViewHolder(View itemView) {
//            super(itemView);
//            name = (TextView) itemView.findViewById(R.id.cell_group_participant_name);
//            photo = (CircularImageView) itemView.findViewById(R.id.cell_group_participant_photo);
//            removeButton = (ImageView) itemView.findViewById(R.id.cell_group_participant_remove);
//
//            removeButton.setImageResource(R.drawable.ic_close);
//
//            if (!isCreator)
//                removeButton.setVisibility(View.GONE);
//
//            View.OnClickListener listener = v -> {
//                int position = getLayoutPosition();
//                final int uid = list.get(position).uid;
//                final Bundle bundle = new Bundle();
//                bundle.putInt(CHAT_ID_KEY, uid);
//                final FragmentFriendProfile fragmentFriendProfile = new FragmentFriendProfile();
//                fragmentFriendProfile.setArguments(bundle);
//
//                final FragmentManager fragmentManager = ((AppCompatActivity)context).getSupportFragmentManager();
//                Utils.replaceFragmentWithAnimationFade(fragmentManager, fragmentFriendProfile, null); //TODO:NAV FIX
//            };
//            itemView.setOnClickListener(listener);
//            name.setOnClickListener(listener);
//            photo.setOnClickListener(listener);
//        }
//    }
//}
