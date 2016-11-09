package com.alboteanu.myapplicationdata;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import static android.R.attr.x;
import static com.alboteanu.myapplicationdata.BaseActivity.getDatabase;

/**
 * Created by albot on 20.09.2016.
 */
public class Utils {


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
        String key = context.getString(R.string.display_title_text_key);
        String storedVal = sharedPrefs.getString(key, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        return storedVal;
    }

    public static String getSavedTextMessage(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.custom_text_key);
        String storedVal = sharedPrefs.getString(key, context.getString(R.string.my_custom_text));
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

    public static void composeSMS(String[] phonesArray, Context context) {
        StringBuilder stringBuilder = new StringBuilder("smsto: ");
        for (int i = 0; i < phonesArray.length; i++) {
            stringBuilder.append(phonesArray[i]);
            stringBuilder.append(", ");
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(stringBuilder.toString())); // only sms apps should handle this
        intent.putExtra("sms_body", getSavedTextMessage(context));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

}
