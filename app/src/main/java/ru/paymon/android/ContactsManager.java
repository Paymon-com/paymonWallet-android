//package ru.paymon.android;
//
//import android.content.ContentResolver;
//import android.content.Context;
//import android.database.Cursor;
//import android.provider.ContactsContract;
//import android.text.TextUtils;
//import android.util.Log;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//
//import ru.paymon.android.models.Contact;
//import ru.paymon.android.net.NetworkManager;
//import ru.paymon.android.net.RPC;
//import ru.paymon.android.utils.Utils;
//import ru.paymon.android.view.DialogProgress;
//
//public class ContactsManager {
//    private static final String CONTACT_ID = ContactsContract.Contacts._ID;
//    private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
//    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
//    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
//    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
//    private static volatile ContactsManager instance = null;
//    private ArrayList<Contact> allContacts;
//    public ArrayList<Contact> registeredContacts;
//    public ArrayList<Contact> unregisteredContacts;
//
//    public static synchronized ContactsManager newInstance(DialogProgress dialogProgress) {
//        instance = new ContactsManager(dialogProgress);
//        return instance;
//    }
//
//    public static synchronized ContactsManager getInstance(DialogProgress dialogProgress) {
//        if (instance == null)
//            instance = new ContactsManager(dialogProgress);
//        return instance;
//    }
//
//
//    public ContactsManager(DialogProgress dialogProgress) {
//        allContacts = getGroups();
//        registeredContacts = getRegistered(dialogProgress);
//        unregisteredContacts = getUnregistered();
//    }
//
//    private ArrayList<Contact> getGroups() {
//        ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
//
//        Cursor pCur = cr.query(
//                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID},
//                null,
//                null,
//                null
//        );
//        if (pCur != null) {
//            if (pCur.getCount() > 0) {
//                HashMap<Integer, ArrayList<String>> phones = new HashMap<>();
//
//                while (pCur.moveToNext()) {
//                    Integer contactId = pCur.getInt(pCur.getColumnIndex(PHONE_CONTACT_ID));
//                    ArrayList<String> curPhones = new ArrayList<>();
//                    if (phones.containsKey(contactId)) {
//                        curPhones = phones.get(contactId);
//                    }
//                    curPhones.add(pCur.getString(0));
//                    phones.put(contactId, curPhones);
//                }
//
//                Cursor cur = cr.query(
//                        ContactsContract.Contacts.CONTENT_URI,
//                        new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER},
//                        HAS_PHONE_NUMBER + " > 0",
//                        null,
//                        DISPLAY_NAME + " ASC");
//                if (cur != null) {
//                    if (cur.getCount() > 0) {
//                        ArrayList<Contact> contacts = new ArrayList<>();
//                        while (cur.moveToNext()) {
//                            int id = cur.getInt(cur.getColumnIndex(CONTACT_ID));
//                            if (phones.containsKey(id)) {
//                                contacts.add(new Contact(id, cur.getString(cur.getColumnIndex(DISPLAY_NAME)), TextUtils.join(",", phones.get(id).toArray())));
//                            }
//                        }
//                        return contacts;
//                    }
//                    cur.close();
//                }
//            }
//            pCur.close();
//        }
//        return null;
//    }
//
//    private ArrayList<Contact> getRegistered(DialogProgress dialogProgress) {
//        ArrayList<Contact> registeredContacts = new ArrayList<>();
//
//        Utils.netQueue.postRunnable(() -> {
//            ApplicationLoader.applicationHandler.post(dialogProgress::show);
//
//            String phones = null;
//            StringBuilder stringBuilder = new StringBuilder();
//
//            for (Contact contact : allContacts) {
//                String phone = contact.phone;
//                phone = Utils.formatPhone(phone);
//                stringBuilder.append(phone).append(";");
//            }
//
//            phones = stringBuilder.toString();
//
//
//            RPC.PM_getUsersByPhoneNumber getUsersByPhoneNumberRequest = new RPC.PM_getUsersByPhoneNumber();
//            getUsersByPhoneNumberRequest.phones = phones;
//
//            long requestID = NetworkManager.getInstance().sendRequest(getUsersByPhoneNumberRequest, ((response, error) -> {
//                if (error != null || response == null) {
//                    ApplicationLoader.applicationHandler.post(() -> {
//                        if (dialogProgress != null && dialogProgress.isShowing())
//                            dialogProgress.cancel();
//                    });
//                    return;
//                }
//
//                if (response instanceof RPC.PM_users) {
//                    RPC.PM_users registeredUsers = (RPC.PM_users) response;
//
//                    for (RPC.UserObject user : registeredUsers.users) {
//                        registeredContacts.add(new Contact(user));
//                    }
//                }
//
//                ApplicationLoader.applicationHandler.post(() -> {
//                    if (dialogProgress != null && dialogProgress.isShowing())
//                        dialogProgress.dismiss();
//                });
//            }));
//
//            ApplicationLoader.applicationHandler.post(() ->
//                    dialogProgress.setOnDismissListener((dialog) -> NetworkManager.getInstance().cancelRequest(requestID, false)));
//        });
//        return registeredContacts;
//    }
//
//    private ArrayList<Contact> getUnregistered() {
//        ArrayList<Contact> unregisteredContacts = getGroups();
//
//        for (Contact registeredContact : registeredContacts) {
//            for (Contact unregisteredContact : unregisteredContacts)
//                if (registeredContact.phone.equals(unregisteredContact.phone))
//                    unregisteredContacts.remove(registeredContact);
//        }
//
//        return unregisteredContacts;
//    }
//}
