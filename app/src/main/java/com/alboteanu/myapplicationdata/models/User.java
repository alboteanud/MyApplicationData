package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User {
    private String timeJoined;
    public String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String timeJoined) {
        this.timeJoined = timeJoined;
        this.email = email;
    }

    @NonNull
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("joined", timeJoined);
        result.put("email", email);

        return result;
    }


}
