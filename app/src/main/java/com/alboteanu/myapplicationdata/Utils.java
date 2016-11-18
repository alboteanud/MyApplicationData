package com.alboteanu.myapplicationdata;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.alboteanu.myapplicationdata.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import static com.alboteanu.myapplicationdata.BaseActivity.getDatabase;

/**
 * Created by albot on 20.09.2016.
 */
class Utils {


 static String getSavedTitle(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.display_title_text_key);
        String storedVal = sharedPrefs.getString(key, FirebaseAuth.getInstance().getCurrentUser().getEmail());
        return storedVal;
    }

    private static String getSavedTextMessage(Context context){
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.custom_text_key);
        String storedVal = sharedPrefs.getString(key, context.getString(R.string.messaage_text));
        return storedVal;
    }


    static DatabaseReference getUserNode(){
        return getDatabase().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    static String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    static void composeSMS(String[] phonesArray, Context context) {
        StringBuilder stringBuilder = new StringBuilder("smsto: ");
        for (String aPhonesArray : phonesArray) {
            stringBuilder.append(aPhonesArray);
            stringBuilder.append(", ");
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(stringBuilder.toString())); // only sms apps should handle this
        intent.putExtra("sms_body", getSavedTextMessage(context));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    static void composeEmail(ListActivity listActivity, String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only emailText apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(listActivity.getPackageManager()) != null) {
            listActivity.startActivity(intent);
        }
    }

    public static String calendarToString(Calendar calendar) {
        Date date = calendar.getTime();
        String dateString = DateFormat.getDateInstance().format(date);
        return dateString;
    }
//    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));

    static void writeNewUser(String email) {
        //        String username = Utils.usernameFromEmail(user.getEmail());
        Calendar calendar = Calendar.getInstance();  //now
        User user = new User(email, calendarToString(calendar));
        Map<String, Object> userMap = user.toMap();
        Utils.getUserNode().child("-user").updateChildren(userMap);

    }
}
