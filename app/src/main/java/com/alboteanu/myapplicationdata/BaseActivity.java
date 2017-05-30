package com.alboteanu.myapplicationdata;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.models.User;
import com.alboteanu.myapplicationdata.others.Constants;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.screens.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Utils.calendarToString;

public class BaseActivity extends AppCompatActivity {
    private static final String ACTION_UPDATE_LOCAL_CONTACTS = "action_update_local";
    private static FirebaseDatabase mDatabase;
    protected FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    private static void writeNewUser(String email) {
        User user = new User(email, calendarToString(Calendar.getInstance()));
        Map<String, Object> userMap = user.toMap();
        Utils.getUserNode().child(Constants.FIREBASE_USER).updateChildren(userMap);
    }

    public static String getActionUpdateLocalContacts() {
        return ACTION_UPDATE_LOCAL_CONTACTS;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    protected void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    protected void saveEmail(@Nullable String email) {
        String savedEmail = getSavedEmail();
        if(email != null && !email.equals(savedEmail)){
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", email);
            editor.apply();
        }
    }

    @Nullable
    protected String getSavedEmail() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return sharedPref.getString("email", null);
    }

    protected void onAuthFail(String message) {
        Toast.makeText(BaseActivity.this, message,
                Toast.LENGTH_SHORT).show();
//        Snackbar.make(getWindow().getDecorView(), message, Snackbar.LENGTH_LONG).show();
    }


    protected void onAuthSuccess(@NonNull FirebaseUser user) {
        writeNewUser(user.getEmail());
        Intent intentToMainActivity = new Intent(this, MainActivity.class);
        intentToMainActivity.setAction(getActionUpdateLocalContacts());
        intentToMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intentToMainActivity);
        finish();
    }

}
