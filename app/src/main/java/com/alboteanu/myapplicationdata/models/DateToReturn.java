package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;


public class DateToReturn {
    public long date;
    public String phone;

    public DateToReturn() {
    }

    public DateToReturn(long date, String phone) {
        this.date = date;
        this.phone = phone;
    }

    @NonNull
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_RETURN, date);
        result.put(FIREBASE_LOCATION_PHONE, phone);
        return result;
    }
}
