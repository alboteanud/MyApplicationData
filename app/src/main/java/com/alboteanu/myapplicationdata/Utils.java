package com.alboteanu.myapplicationdata;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import static com.alboteanu.myapplicationdata.BaseActivity.getDatabase;

/**
 * Created by albot on 20.09.2016.
 */
public class Utils {

    public static void setSavedTheme(Context context){
        //scimbare Themes
        switch (getSavedTheme(context)) {
            case 0:    //Theme Light
                context.setTheme(R.style.AppTheme_NoActionBar);
                break;
            case 1:   //Theme Dark
                context.setTheme(R.style.AppThemeDark_NoActionBar);
                break;
        }
    }

    public static int getSavedTheme(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.themes_list);
        int storedVal = Integer.parseInt(sharedPrefs.getString(key, "0"));
        return storedVal;
    }

 public static String getSavedTitle(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.title_text);
        String storedVal = sharedPrefs.getString(key, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        return storedVal;
    }

 public static boolean getListStateView(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.simple_list_switch);
        Boolean storedVal = sharedPrefs.getBoolean(key, true);
        return storedVal;
    }

    

    public static DatabaseReference getUserNode(){
        return getDatabase().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    static String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }
}
