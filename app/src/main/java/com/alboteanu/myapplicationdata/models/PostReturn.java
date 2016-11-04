package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by albot on 04.11.2016.
 */
public class PostReturn extends PostFixed {
    public long date;
    public String phone;

    public PostReturn() {
    }

    public PostReturn(long date, String phone) {
        this.date = date;
        this.phone = phone;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("phone", phone);
        return result;
    }
}
