package com.alboteanu.myapplicationdata;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.models.User;
import com.alboteanu.myapplicationdata.others.Constants;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.screens.MainActivity;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Utils.calendarToString;

public class BaseActivity extends AppCompatActivity {
    private static final String ACTION_UPDATE_LOCAL_CONTACTS = "action_update_local";
    private static FirebaseDatabase mDatabase;
    public GoogleApiClient mGoogleApiClient;
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
        initGoogleAuth();
    }

    private void initGoogleAuth() {

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestIdToken(getString(R.string.my_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this  /*FragmentActivity*/, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d("BaseActivity", "onConnectionFailed:" + connectionResult);
//                        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
                    }
                }  /*OnConnectionFailedListener*/)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(AppIndex.API)
 /*               .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "connectionFailed:  " + connectionResult.getErrorMessage());
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        if (bundle != null)
                            Log.d(TAG, "conected: " + bundle.toString());
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "connectionSuspended:  " + i);
                    }
                })*/
                .build();
    }

    protected void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(getString(R.string.loading));
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
        Toast.makeText(BaseActivity.this, "Authentication failed. " + message,
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

    public void googleSignOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
//                updateUI("signed Out");
            }
        });
    }

}
