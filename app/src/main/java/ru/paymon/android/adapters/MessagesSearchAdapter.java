//package ru.paymon.android.adapters;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import com.mikhaellopez.circularimageview.CircularImageView;
//
//import java.util.LinkedList;
//
//import ru.paymon.android.R;
//import ru.paymon.android.models.MessageItem;
//import ru.paymon.android.utils.Utils;
//
//public class MessagesSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//    private LinkedList<MessageItem> messagesSearchItems;
//
//    public MessagesSearchAdapter(LinkedList<MessageItem> messagesSearchItems){
//        this.messagesSearchItems = messagesSearchItems;
//    }
//
//    @NonNull
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_messages_search_item, parent, false);
//        MessagesSearchItemViewHolder holder = new MessagesSearchItemViewHolder(view);
//        return holder;
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        MessageItem messageItem = messagesSearchItems.get(position);
//        MessagesSearchAdapter.MessagesSearchItemViewHolder messagesSearchItemViewHolder = (MessagesSearchAdapter.MessagesSearchItemViewHolder) holder;
//        if (!messageItem.photo.url.isEmpty())
//            Utils.loadPhoto(messageItem.photo.url, messagesSearchItemViewHolder.photo);
//        messagesSearchItemViewHolder.name.setText(messageItem.name);
//        messagesSearchItemViewHolder.message.setText(messageItem.message);
//
//    }
//
//    @Override
//    public int getItemCount() {
//        return messagesSearchItems.size();
//    }
//
//    private class MessagesSearchItemViewHolder extends RecyclerView.ViewHolder{
//        public CircularImageView photo;
//        public TextView name;
//        public TextView message;
//
//        public MessagesSearchItemViewHolder(View itemView) {
//            super(itemView);
//            photo = (CircularImageView) itemView.findViewById(R.id.message_search_photo);
//            name = (TextView) itemView.findViewById(R.id.message_search_name);
//            message = (TextView) itemView.findViewById(R.id.message_search);
//        }
//    }
//}
