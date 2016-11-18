package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_OTHER;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;


@IgnoreExtraProperties
public class Contact implements Serializable {
    public String name;
    public String phone;
    public String email;
    public String other;
    public long date;


    public Contact() {
        // Default constructor required for calls to DataSnapshot.getValue(ContactS.class)
    }

    public Contact(String name, String phone, String email, String other, long date) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.other = other;
        this.date = date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_NAME, name);
        result.put(FIREBASE_LOCATION_PHONE, phone);
        result.put(FIREBASE_LOCATION_EMAIL, email);
        result.put(FIREBASE_LOCATION_OTHER, other);
        result.put(FIREBASE_LOCATION_RETURN_DATE, date);

        return result;
    }
}
