package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_OTHER;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;

public class FixedFields {
    public String name;
    public String phone;
    public String email;
    public String other;
    public String date;

    public FixedFields() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public FixedFields(String namef, String phonef, String emailf, String others1f, String returnf) {
        this.name = namef;
        this.phone = phonef;
        this.email = emailf;
        this.other = others1f;
        this.date = returnf;
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
