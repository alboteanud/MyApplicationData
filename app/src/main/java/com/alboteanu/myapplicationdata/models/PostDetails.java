package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class PostDetails {
    public String text2;
    public String text4;
    public String text6;
    public String text8;

    public PostDetails() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public PostDetails(String text2, String text4, String text6, String text8) {
        this.text2 = text2;
        this.text4 = text4;
        this.text6 = text6;
        this.text8 = text8;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("text2", text2);
        result.put("text4", text4);
        result.put("text6", text6);
        result.put("text8", text8);

        return result;
    }
}
