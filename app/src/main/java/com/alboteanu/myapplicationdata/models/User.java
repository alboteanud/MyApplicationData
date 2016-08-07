package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    private HashMap<String, Object> map;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        HashMap<String, Object> map = new HashMap<>();
        map.put("joined", ServerValue.TIMESTAMP);
        this.map = map;
    }

}
// [END blog_user_class]
