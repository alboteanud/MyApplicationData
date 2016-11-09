package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class DetailedContact {
    public String name;
    public String phone;
    public String email;
    public String others1;
    public String nameF;
    public String phoneF;
    public String emailF;
    public String others1F;
    public String dateF;
    public long date;


    public DetailedContact() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public DetailedContact(String name, String phone, String email, String others1, long date, String nameF, String phoneF, String emailF, String others1F, String dateF) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.others1 = others1;
        this.date = date;
        this.nameF = nameF;
        this.phoneF = phoneF;
        this.emailF = emailF;
        this.others1F = others1F;
        this.dateF = dateF;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("phone", phone);
        result.put("email", email);
        result.put("others1", others1);
        result.put("date", date);
        result.put("nameF", nameF);
        result.put("phoneF", phoneF);
        result.put("emailF", emailF);
        result.put("others1F", others1F);
        result.put("dateF", dateF);

        return result;
    }
}
