package com.alboteanu.myapplicationdata.others;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

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
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN_RETUR;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN_DATES;

public class Utils {

    public static String getSavedTitle(Context context){
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

    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    public static void composeSMS(String[] phonesArray, Context context) {
        StringBuilder stringBuilder = new StringBuilder("smsto: ");
        for (String aPhonesArray : phonesArray) {
            if(aPhonesArray !=null) {
                stringBuilder.append(aPhonesArray);
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

    public static void composeEmail(Context context, String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only emailText apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static String calendarToString(Calendar calendar) {
        String dateString = null;
        if(calendar != null) {
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

    public static int getColorFromString(String string) {
        int[] RGB = {0,0,0};
        int l = string.length();
        String sub_string_0 = string.substring(0, (int) Math.ceil((double) l / 3));                 // responsable for Red
        int l_0 = sub_string_0.length();
        String sub_string_1 = string.substring(l_0,  l_0 + (int) Math.ceil((double) (l - l_0)/2));  // responsable for Green
        String sub_string_2 = string.substring(l_0 + sub_string_1.length(), string.length());       // responsable for Blue

        String[] sub_string = new String[]{
                sub_string_0,
                sub_string_1,
                sub_string_2
        };
        for(int i = 0; i < sub_string.length; i++) {
            if(sub_string[i].length()==0)
                sub_string[i] = " ";
            for (char c : sub_string[i].toCharArray()) {
                int c_val = Character.getNumericValue(c) - Character.getNumericValue('a');          // for 'a' -> 0     for 'z' -> 25
                if(c_val < 0)                                                                       //  spaces, numbers ...
                    c_val= new Random().nextInt(25);
                RGB[i] = RGB[i] + c_val;
            }
        }

        int letters_number = Character.getNumericValue('z') - Character.getNumericValue('a');       //  z - a    35 - 10

        // normalizing
        int R = 255 * RGB[0]/sub_string[0].length()/letters_number;
        int G = 255 * RGB[1]/sub_string[1].length()/letters_number;
        int B = 255 * RGB[2]/sub_string[2].length()/letters_number;

        return Color.rgb(R, G, B);
    }

    public static String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map == null)
                            return;
                        Collection<String> values = map.values();
                        if(!values.isEmpty()){
                            allEmailsArray = values.toArray(new String[0]);
//                            menu.findItem(R.id.action_send_email_to_all).setVisible(true);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    List<String> phoneList = new ArrayList<>();
    public void getExpired() {
        final long currentTime = System.currentTimeMillis();
        Utils.getUserNode().child(FIREBASE_LOCATION_RETURN_DATES).orderByChild(FIREBASE_LOCATION_RETURN_RETUR).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
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

    }
