package ru.paymon.android.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;

import ru.paymon.android.R;
import ru.paymon.android.models.Contact;
import ru.paymon.android.utils.Utils;

public class ContactsInviteRegisteredAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public ArrayList<Contact> registeredContacts;

    public ContactsInviteRegisteredAdapter(ArrayList<Contact> registeredContacts) {
        this.registeredContacts = registeredContacts;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_contacts_invite_registered, parent, false);
        RecyclerView.ViewHolder vh = new ContactsInviteRegisteredViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Contact contact = registeredContacts.get(position);
        ContactsInviteRegisteredViewHolder registeredViewHolder = ( ContactsInviteRegisteredAdapter.ContactsInviteRegisteredViewHolder) holder;
        registeredViewHolder.name.setText(contact.name);
        registeredViewHolder.phoneNumber.setText(contact.phone);
        if (!contact.photo.url.isEmpty())
            Utils.loadPhoto(contact.photo.url, registeredViewHolder.avatar);
        registeredViewHolder.checkBox.setOnCheckedChangeListener((cbutton, newValue) -> contact.isChecked = newValue);
    }

    @Override
    public int getItemCount() {
        return registeredContacts.size();
    }

    private class ContactsInviteRegisteredViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private TextView phoneNumber;
        private CheckBox checkBox;
        private CircularImageView avatar;

        private ContactsInviteRegisteredViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.name_contact_invite_registered);
            phoneNumber = (TextView) itemView.findViewById(R.id.phone_contact_invite_registered);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_contact_invite_registered);
            avatar = (CircularImageView) itemView.findViewById(R.id.image_contact_invite_registered);
        }
    }


}
