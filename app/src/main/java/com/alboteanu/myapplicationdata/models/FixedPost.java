package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class FixedPost {
    public String text1;
    public String text3;
    public String text5;

    public FixedPost() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public FixedPost(String text1, String text3, String text5) {
        this.text1 = text1;
        this.text3 = text3;
        this.text5 = text5;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("text1", text1);
        result.put("text3", text3);
        result.put("text5", text5);

        return result;
    }
}
