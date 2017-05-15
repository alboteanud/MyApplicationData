package com.alboteanu.myapplicationdata.others;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.alboteanu.myapplicationdata.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import static com.alboteanu.myapplicationdata.BaseActivity.getDatabase;

public class Utils {

    private static final String LOG_PREFIX = "app_";
    private static final int LOG_PREFIX_LENGTH = LOG_PREFIX.length();
    private static final int MAX_LOG_TAG_LENGTH = 23;

    public static String makeLogTag(String str) {
        if (str.length() > MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH) {
            return LOG_PREFIX + str.substring(0, MAX_LOG_TAG_LENGTH - LOG_PREFIX_LENGTH - 1);
        }

        return LOG_PREFIX + str;
    }

    /**
     * Don't use this when obfuscating class names!
     */
    public static String makeLogTag(Class cls) {
        return makeLogTag(cls.getSimpleName());
    }

    @Nullable
    public static String getSavedTitle(@NonNull Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.display_title_text_key);
        return sharedPrefs.getString(key, getUsername());
    }

    @Nullable
    public static String getSavedTextMessage(@NonNull Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.custom_message_text_key);
        return sharedPrefs.getString(key, context.getString(R.string.custom_message));
    }

    public static DatabaseReference getUserNode() {
        return getDatabase().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    public static String getUsername() {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    public static boolean isValidEmail(@Nullable CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static void composeSMS(@NonNull String[] phonesArray, @NonNull Context context) {
        StringBuilder stringBuilder = new StringBuilder("smsto: ");
        for (int i = 0; i < phonesArray.length; i++) {
            String phoneNumber = phonesArray[i];
            if (phoneNumber != null) {
                stringBuilder.append(phoneNumber);
                if (i != phonesArray.length - 1)
                    stringBuilder.append(", ");
            }

        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(stringBuilder.toString())); // only sms apps should handle this
        intent.putExtra("sms_body", getSavedTextMessage(context));
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static void composeEmail(@NonNull Context context, String[] addresses) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only emailText apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    @Nullable
    public static String calendarToString(@Nullable Calendar calendar) {
        String dateString = null;
        if (calendar != null) {
            Date date = calendar.getTime();
            dateString = DateFormat.getDateInstance().format(date);
        }
        return dateString;
    }
//    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));



    public static int getColorFromString(@NonNull String string) {
        int[] RGB = {0, 0, 0};
        int l = string.length();
        String sub_string_0 = string.substring(0, (int) Math.ceil((double) l / 3));                 // responsable for Red
        int l_0 = sub_string_0.length();
        String sub_string_1 = string.substring(l_0, l_0 + (int) Math.ceil((double) (l - l_0) / 2));  // responsable for Green
        String sub_string_2 = string.substring(l_0 + sub_string_1.length(), string.length());       // responsable for Blue

        String[] sub_string = new String[]{
                sub_string_0,
                sub_string_1,
                sub_string_2
        };
        for (int i = 0; i < sub_string.length; i++) {
            if (sub_string[i].length() == 0)
                sub_string[i] = " ";
            for (char c : sub_string[i].toCharArray()) {
                int c_val = Character.getNumericValue(c) - Character.getNumericValue('a');          // for 'a' -> 0     for 'z' -> 25
                if (c_val < 0)                                                                       //  spaces, numbers ...
                    c_val = new Random().nextInt(25);
                RGB[i] = RGB[i] + c_val;
            }
        }

        int letters_number = Character.getNumericValue('z') - Character.getNumericValue('a');       //  z - a    35 - 10

        // normalizing
        int R = 255 * RGB[0] / sub_string[0].length() / letters_number;
        int G = 255 * RGB[1] / sub_string[1].length() / letters_number;
        int B = 255 * RGB[2] / sub_string[2].length() / letters_number;

        return Color.rgb(R, G, B);
    }

    public static void updateLocalDataBase() {
        Log.d("tag", "updateLocalDataBase()");
        Utils.getUserNode().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }


    public static void clearPreferences(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
//        String key = getString(R.string.display_title_text_key);
//        SharedPreferences.Editor editor = sharedPrefs.edit();
//        editor.remove(key);
//        editor.apply();
    }

}