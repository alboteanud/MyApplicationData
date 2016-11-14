package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;

@IgnoreExtraProperties
public class Contact {
    public String name;
    public String phone;
    public long date;

    public Contact() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public Contact(String name, String phone, long date) {
        this.name = name;
        this.phone = phone;
        this.date = date;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_NAME, name);
        if(!phone.isEmpty())
            result.put(FIREBASE_LOCATION_PHONE, phone);
        if(date!=0)
            result.put(FIREBASE_LOCATION_RETURN_DATE, date);

        return result;
    }
}
