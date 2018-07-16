package com.alboteanu.myapplicationdata.models;

import android.support.annotation.NonNull;

import com.alboteanu.myapplicationdata.others.Utils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;

@IgnoreExtraProperties
public class User {
    private String timeVisit;
    private String email;

    public User(){
        // required for datasnaphot.value
    }

    public User(FirebaseUser firebaseUser) {
        this.timeVisit = Utils.calendarToString(Calendar.getInstance());
        this.email = firebaseUser.getEmail();
    }

    @NonNull
    @Exclude
    public Map<String, Object> toMap(String label) {
        HashMap<String, Object> result = new HashMap<>();
        result.put(label, timeVisit);
        result.put(FIREBASE_LOCATION_EMAIL, email);
        return result;
    }

}
