package com.alboteanu.myapplicationdata.models;

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
    public String name;
    public String phone;
    public String email;
    public String note;
//    public long return_date_millis = -1;
//    public Calendar calendar;
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
