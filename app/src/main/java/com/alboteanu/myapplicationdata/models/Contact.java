package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_OTHER;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;


@IgnoreExtraProperties
public class Contact {
    public String name;
    public String phone;
    public String email;
    public String other;
    public final Map<String, Long> retur = new HashMap<>();

    public Contact() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

/*    public Contact(String name, String phone, String email, String other) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.other = other;
    }*/

    public Contact(String name) {
        this.name = name;
    }

    @NonNull
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_NAME, name);
        result.put(FIREBASE_LOCATION_PHONE, phone);
        result.put(FIREBASE_LOCATION_EMAIL, email);
        result.put(FIREBASE_LOCATION_OTHER, other);
        result.put(FIREBASE_LOCATION_RETURN, retur);

        return result;
    }
}
