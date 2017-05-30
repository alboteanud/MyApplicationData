package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    private String timeLastVisit;
    private String email;

    public User(String email, String timeLastVisit) {
        this.timeLastVisit = timeLastVisit;
        this.email = email;
    }

    @NonNull
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("visited", timeLastVisit);
        result.put("email", email);

        return result;
    }


}
