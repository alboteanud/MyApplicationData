package com.alboteanu.myapplicationdata.models;

import com.alboteanu.myapplicationdata.R;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.R.attr.data;
import static com.alboteanu.myapplicationdata.R.id.returnD;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public String id;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String id, String username, String email) {
        this.username = username;
        this.email = email;
        this.id = id;

    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("joined", getTime());
        result.put("email", email);
        result.put("username", username);

        return result;
    }

    private String getTime() {

        Calendar calendar = Calendar.getInstance();
        Date dateNow = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
        String dateString = simpleDateFormat.format(dateNow);
        return dateString;
    }

}
