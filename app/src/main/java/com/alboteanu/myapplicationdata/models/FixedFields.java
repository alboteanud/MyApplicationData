package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;

/**
 * Created by albot on 14.11.2016.
 */
public class FixedFields {
    public String namef;
    public String phonef;
    public String emailf;
    public String others1f;
    public String returnf;

    public FixedFields() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public FixedFields(String namef, String phonef, String emailf, String others1f, String returnf) {
        this.namef = namef;
        this.phonef = phonef;
        this.emailf = emailf;
        this.others1f = others1f;
        this.returnf = returnf;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_NAME, namef);
        result.put(FIREBASE_LOCATION_PHONE, phonef);
        result.put(FIREBASE_LOCATION_RETURN_DATE, emailf);
        result.put(FIREBASE_LOCATION_RETURN_DATE, others1f);
        result.put(FIREBASE_LOCATION_RETURN_DATE, returnf);
        return result;
    }
}
