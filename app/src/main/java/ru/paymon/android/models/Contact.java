package ru.paymon.android.models;

public class Contact {
    private String _name = "";
    private String _phone = "";
    private int _my_id;

    public void setName(String name) {
        _name = name;
    }

    public void setPhone(String phone) {
        _phone = phone;
    }

    public void setMyId(int my_id) {
        _my_id = my_id;
    }
}

