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
import com.alboteanu.myapplicationdata.models.DateToReturn;
import com.alboteanu.myapplicationdata.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static com.alboteanu.myapplicationdata.BaseActivity.getDatabase;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN_DATES;

public class Utils {

    @Nullable
    public static String getSavedTitle(@NonNull Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.display_title_text_key);
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String defaultTitle = usernameFromEmail(userEmail);


        return sharedPrefs.getString(key, defaultTitle);
    }

    @Nullable
    private static String getSavedTextMessage(@NonNull Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.custom_text_key);
        return sharedPrefs.getString(key, context.getString(R.string.messaage_text));
    }

    public static DatabaseReference getUserNode() {
        return getDatabase().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    static String usernameFromEmail(@NonNull String email) {
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

    public static void writeNewUser(String email) { //now
        User user = new User(email, calendarToString(Calendar.getInstance()));
        Map<String, Object> userMap = user.toMap();
        Utils.getUserNode().child("-user").updateChildren(userMap);

    }

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

    /*    private String[] allPhonesArray;
    private void getAllPhones() {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map == null)
                            return;
                        Collection<String> values = map.values();
                        if(!values.isEmpty()){
                            allPhonesArray = values.toArray(new String[0]);
//                            menu.findItem(R.id.action_send_sms_to_all).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }*/

    String[] allEmailsArray;

    private void getAllEmails() {
        Utils.getUserNode().child(FIREBASE_LOCATION_EMAIL)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map == null)
                            return;
                        Collection<String> values = map.values();
                        if (!values.isEmpty()) {
                            allEmailsArray = values.toArray(new String[0]);
//                            menu.findItem(R.id.action_send_email_to_all).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    @NonNull
    final
    List<String> phoneList = new ArrayList<>();

    public void getExpired() {
        final long currentTime = System.currentTimeMillis();
        Utils.getUserNode().child(FIREBASE_LOCATION_RETURN_DATES).orderByChild(FIREBASE_LOCATION_RETURN).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                DateToReturn dateToReturn = dataSnapshot.getValue(DateToReturn.class);
                if (dateToReturn != null) {
                    if (dateToReturn.date != 0 && currentTime > dateToReturn.date) {
                        phoneList.add(dateToReturn.phone);
//                        menu.findItem(R.id.action_sms_to_expired).setTitle(String.valueOf(phoneList.size())).setVisible(true);
                    }

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public static void saveDefaultTitle(@NonNull Context context) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String key = context.getString(R.string.display_title_text_key);
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String defaultTitle = usernameFromEmail(userEmail);

        sharedPrefs.edit().putString(key, defaultTitle).apply();
    }
}
