package com.alboteanu.myapplicationdata.others;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;


public class Utils {

    @Nullable
    private static String getSavedTextMessage(@NonNull Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.custom_message_text_key);
        return sharedPrefs.getString(key, context.getString(R.string.pref_default_text_message));
    }

    public static boolean isValidEmail(@Nullable CharSequence target) {
        return target == null || !android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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

    public static int getLetterColor(String name) {
        String[] colors = {
                "#B71C1C",
                "#880E4F",
                "#4A148C",
                "#311B92",
                "#1A237E",
                "#0D47A1",
                "#1E88E5",
                "#01579B",
                "#006064",
                "#004D40",
                "#1B5E20",
                "#33691E",
                "#827717",
                "#E65100",
                "#BF360C",
                "#5D4037",
                "#8D6E63",
                "#424242",
                "#607D8B",
                "#E91E63",
                "#9C27B0",
                "#FF1744",
                "#7986CB",
                "#0097A7",
                "#43A047",
                "#9E9D24",
                "#FBC02D",
                "#A1887F",
                "#757575",
                "#78909C",
        };
        final long n = stringToNumber(name);
        final int colorNo = (int) (n % colors.length);
        return Color.parseColor(colors[colorNo]);
    }

    private static long stringToNumber(String s) {
        long result = 0;

        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            result += (int) ch;
        }

        return result;
    }


}