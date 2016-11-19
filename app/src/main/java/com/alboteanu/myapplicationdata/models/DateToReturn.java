package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;


public class DateToReturn {
    public long date;
    public String phone;

    public DateToReturn() {
    }

    public DateToReturn(long date, String phone) {
        this.date = date;
        this.phone = phone;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_RETURN_DATE, date);
        result.put(FIREBASE_LOCATION_PHONE, phone);
        return result;
    }
}