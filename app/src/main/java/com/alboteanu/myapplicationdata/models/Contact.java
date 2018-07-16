package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_OTHER;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONE;


@IgnoreExtraProperties
public class Contact implements Serializable {
    public String name, phone, email, note;
    public long date = -1;

    public Contact() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public Contact(String name) {
        this.name = name;
//        this.phone = phone;
//        this.email = email;
//        this.note = note;
    }

 public Contact(String name, String phone, String email, String note, long date) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.note = note;
        this.date = date;
    }


    @NonNull
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_NAME, name);
        result.put(FIREBASE_LOCATION_PHONE, phone);
        result.put(FIREBASE_LOCATION_EMAIL, email);
        result.put(FIREBASE_LOCATION_OTHER, note);
        result.put(FIREBASE_LOCATION_DATE, date);
        return result;
    }
}
