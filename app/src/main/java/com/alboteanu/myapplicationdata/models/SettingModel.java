package com.alboteanu.myapplicationdata.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CUSTOM_MESSAGE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_TITLE;


@IgnoreExtraProperties
public class SettingModel implements Serializable {

    public String title;
    public String custom_message;

    public SettingModel() {
        // Default constructor required for calls to DataSnapshot.getValue(Contact.class)
    }



    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(FIREBASE_LOCATION_TITLE, title);
        result.put(FIREBASE_LOCATION_CUSTOM_MESSAGE, custom_message);
        return result;
    }
}
