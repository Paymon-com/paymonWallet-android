package ru.paymon.android;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import ru.paymon.android.models.Contact;

public class ContactsManager {
    private static final String CONTACT_ID = ContactsContract.Contacts._ID;
    private static final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private static final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private static final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private static volatile ContactsManager Instance = null;

    public static ContactsManager getInstance() {
        ContactsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ContactsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ContactsManager();
                }
            }
        }
        return localInstance;
    }

    public void dispose() {
        Instance = null;
    }

    public ArrayList<Contact> getAll() {
        ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID},
                null,
                null,
                null
        );
        if (pCur != null) {
            if (pCur.getCount() > 0) {
                HashMap<Integer, ArrayList<String>> phones = new HashMap<>();

                while (pCur.moveToNext()) {
                    Integer contactId = pCur.getInt(pCur.getColumnIndex(PHONE_CONTACT_ID));
                    ArrayList<String> curPhones = new ArrayList<>();
                    if (phones.containsKey(contactId)) {
                        curPhones = phones.get(contactId);
                    }
                    curPhones.add(pCur.getString(0));
                    phones.put(contactId, curPhones);
                }

                Cursor cur = cr.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER},
                        HAS_PHONE_NUMBER + " > 0",
                        null,
                        DISPLAY_NAME + " ASC");
                if (cur != null) {
                    if (cur.getCount() > 0) {
                        ArrayList<Contact> contacts = new ArrayList<>();
                        while (cur.moveToNext()) {
                            int id = cur.getInt(cur.getColumnIndex(CONTACT_ID));
                            if (phones.containsKey(id)) {
                                contacts.add(new Contact(id, cur.getString(cur.getColumnIndex(DISPLAY_NAME)), TextUtils.join(",", phones.get(id).toArray())));
                            }
                        }
                        return contacts;
                    }
                    cur.close();
                }
            }
            pCur.close();
        }
        return null;
    }

    public ArrayList<Contact> getRegistered() {
        ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID},
                null,
                null,
                null
        );
        if (pCur != null) {
            if (pCur.getCount() > 0) {
                HashMap<Integer, ArrayList<String>> phones = new HashMap<>();

                while (pCur.moveToNext()) {
                    Integer contactId = pCur.getInt(pCur.getColumnIndex(PHONE_CONTACT_ID));
                    ArrayList<String> curPhones = new ArrayList<>();
                    if (phones.containsKey(contactId)) {
                        curPhones = phones.get(contactId);
                    }
                    curPhones.add(pCur.getString(0));
                    phones.put(contactId, curPhones);
                }

                Cursor cur = cr.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER},
                        HAS_PHONE_NUMBER + " > 0",
                        null,
                        DISPLAY_NAME + " ASC");
                if (cur != null) {
                    if (cur.getCount() > 0) {
                        ArrayList<Contact> contacts = new ArrayList<>();
                        while (cur.moveToNext()) {
                            int id = cur.getInt(cur.getColumnIndex(CONTACT_ID));
                            if (phones.containsKey(id)) {
                                contacts.add(new Contact(id, cur.getString(cur.getColumnIndex(DISPLAY_NAME)), TextUtils.join(",", phones.get(id).toArray())));
                            }
                        }
                        return contacts;
                    }
                    cur.close();
                }
            }
            pCur.close();
        }
        return null;
    }

    public ArrayList<Contact> getUnregistered() {
        ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();

        Cursor pCur = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{PHONE_NUMBER, PHONE_CONTACT_ID},
                null,
                null,
                null
        );
        if (pCur != null) {
            if (pCur.getCount() > 0) {
                HashMap<Integer, ArrayList<String>> phones = new HashMap<>();

                while (pCur.moveToNext()) {
                    Integer contactId = pCur.getInt(pCur.getColumnIndex(PHONE_CONTACT_ID));
                    ArrayList<String> curPhones = new ArrayList<>();
                    if (phones.containsKey(contactId)) {
                        curPhones = phones.get(contactId);
                    }
                    curPhones.add(pCur.getString(0));
                    phones.put(contactId, curPhones);
                }

                Cursor cur = cr.query(
                        ContactsContract.Contacts.CONTENT_URI,
                        new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER},
                        HAS_PHONE_NUMBER + " > 0",
                        null,
                        DISPLAY_NAME + " ASC");
                if (cur != null) {
                    if (cur.getCount() > 0) {
                        ArrayList<Contact> contacts = new ArrayList<>();
                        while (cur.moveToNext()) {
                            int id = cur.getInt(cur.getColumnIndex(CONTACT_ID));
                            if (phones.containsKey(id)) {
                                contacts.add(new Contact(id, cur.getString(cur.getColumnIndex(DISPLAY_NAME)), TextUtils.join(",", phones.get(id).toArray())));
                            }
                        }
                        return contacts;
                    }
                    cur.close();
                }
            }
            pCur.close();
        }
        return null;
    }
}
