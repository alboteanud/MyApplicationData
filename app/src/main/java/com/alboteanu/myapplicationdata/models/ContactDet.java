package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_F_DATE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_F_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_F_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_F_OTHER_1;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE_F;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_OTHERS1;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;


@IgnoreExtraProperties
public class ContactDet {
    public String name;
    public String phone;
    public String email;
    public String others1;
    public String namef;
    public String phonef;
    public String emailf;
    public String others1f;
    public String datef;
    public long date;


    public ContactDet() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }

    public ContactDet(String name, String phone, String email, String others1, long date, String namef, String phonef, String emailf, String others1f, String datef) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.others1 = others1;
        this.date = date;
        this.namef = namef;
        this.phonef = phonef;
        this.emailf = emailf;
        this.others1f = others1f;
        this.datef = datef;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_NAME, name);
        if(!namef.isEmpty())
            result.put(FIREBASE_LOCATION_F_NAME, namef);
        if(!phone.isEmpty())
            result.put(FIREBASE_LOCATION_PHONE, phone);
        if(!email.isEmpty())
            result.put(FIREBASE_LOCATION_EMAIL, email);
        if(!others1.isEmpty())
            result.put(FIREBASE_LOCATION_OTHERS1, others1);
        if(date!=0)
            result.put(FIREBASE_LOCATION_RETURN_DATE, date);
        if(!phonef.isEmpty())
            result.put(FIREBASE_LOCATION_PHONE_F, phonef);
        if(!emailf.isEmpty())
            result.put(FIREBASE_LOCATION_F_EMAIL, emailf);
        if(!others1f.isEmpty())
            result.put(FIREBASE_LOCATION_F_OTHER_1, others1f);
        if(!datef.isEmpty())
            result.put(FIREBASE_LOCATION_F_DATE, datef);

        return result;
    }
}
