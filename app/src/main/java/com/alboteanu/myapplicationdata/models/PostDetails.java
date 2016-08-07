package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dan on 06/08/2016.
 */
public class PostDetails {
    public String text1;
    public String text2;

    public PostDetails() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public PostDetails(String text1, String text2) {
        this.text1 = text1;
        this.text2 = text2;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("text4", text1);
        result.put("text2", text2);

        return result;
    }
}
