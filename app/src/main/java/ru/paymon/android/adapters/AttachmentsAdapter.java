package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

import ru.paymon.android.R;
import ru.paymon.android.models.attachments.AttachmentItem;
import ru.paymon.android.models.attachments.AttachmentMessages;

public class AttachmentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private LinkedList<AttachmentItem>  attachmentItems;

    public AttachmentsAdapter(LinkedList<AttachmentItem> attachmentsItems){
        this.attachmentItems = attachmentsItems;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder vh = null;

        AttachmentItem.AttachmentTypes attachmentType = AttachmentItem.AttachmentTypes.values()[viewType];
        switch (attachmentType){
            case MESSAGE:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_attachment_message, viewGroup, false);
                vh = new MessageAttachmentViewHolder(view);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AttachmentItem attachmentItem = getItem(position);

        if(holder instanceof MessageAttachmentViewHolder){
            AttachmentMessages attachmentMessages = (AttachmentMessages) attachmentItem;
            MessageAttachmentViewHolder messageAttachmentViewHolder = (MessageAttachmentViewHolder) holder;
            messageAttachmentViewHolder.count.setText(attachmentMessages.messages.size() + "");
            messageAttachmentViewHolder.close.setOnClickListener(view -> {
                attachmentItems.remove(attachmentItem);
                notifyDataSetChanged();
            });
        }
    }

    private AttachmentItem getItem(int position){
        return attachmentItems.get(position);
    }

    @Override
    public int getItemCount() {
        return attachmentItems.size();
    }

    public static class MessageAttachmentViewHolder extends RecyclerView.ViewHolder {
        public TextView count;
        public ImageView close;

        public MessageAttachmentViewHolder(View itemView) {
            super(itemView);
            count = itemView.findViewById(R.id.text_view_message_attachment_count);
            close = itemView.findViewById(R.id.image_view_close);
        }
    }
}
