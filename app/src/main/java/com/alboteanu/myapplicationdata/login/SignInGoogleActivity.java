package com.alboteanu.myapplicationdata.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInGoogleActivity extends BaseActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInGoogleActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
            return;
        }
        setContentView(R.layout.activity_google_login);



        // Button listeners
        findViewById(R.id.google_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_sign_in).setOnClickListener(this);

    }

    private void googleSignIn() {
        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                assert account != null;
                firebaseAuthWithGoogle(account);
            } else {
                hideProgressDialog();
                // Google Sign In failed, update UI appropriately
                Toast.makeText(SignInGoogleActivity.this, "Sign in error",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, result.getStatus() + "  " + data.toString());
            }
        }
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential: success");
                            FirebaseUser user = mAuth.getCurrentUser();
//                                    updateUI(user);
                            onAuthSuccess(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                                    updateUI(null);
                            onAuthFail(task.getException().getMessage());
                        }

                        // ...
                    }
                });
    }



    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        if (id == R.id.google_sign_in_button) {
            Log.w(TAG, "onClick() on R.id.google_sign_in_button");
            googleSignIn();
        } else if (id == R.id.email_sign_in) {
            Log.w(TAG, "onClick() on R.id.email_sign_in");
            startActivity(new Intent(SignInGoogleActivity.this, SignInEmailActivity.class));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mGoogleApiClient, getIndexApiAction());
        mGoogleApiClient.disconnect();
    }


    @NonNull
    private Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName(getString(R.string.app_name)) // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.alboteanu.clientsFree"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        AppIndex.AppIndexApi.start(mGoogleApiClient, getIndexApiAction());
    }
}
