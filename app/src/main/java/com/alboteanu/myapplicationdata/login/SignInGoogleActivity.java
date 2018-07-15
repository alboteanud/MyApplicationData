package com.alboteanu.myapplicationdata.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.MainActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Constants;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static com.alboteanu.myapplicationdata.login.CreatePasswordActivity.goToMainActivity;

public class SignInGoogleActivity extends BaseActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInGoogleActivity";
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        // Check auth on Activity start
        if (firebaseAuth.getCurrentUser() != null) {
//            onAuthSuccess(firebaseAuth.getCurrentUser());
            goToMainActivity(SignInGoogleActivity.this);
            return;
        }
        setContentView(R.layout.activity_google_login);

        findViewById(R.id.google_sign_in_button).setOnClickListener(this);
        findViewById(R.id.email_sign_in).setOnClickListener(this);

    }

    private void signInGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "Google sign in failed", e);
                hideProgressDialog();
                Toast.makeText(SignInGoogleActivity.this, "Sign in error",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            hideProgressDialog();
            Toast.makeText(SignInGoogleActivity.this, "Sign in error",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle: " + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential: success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
//                                    updateUI(user);
                            CreatePasswordActivity.writeNewUser(user.getEmail());
                            goToMainActivity(SignInGoogleActivity.this);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                                    updateUI(null);
//                            onAuthFail(task.getException().getMessage());
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
            signInGoogle();
        } else if (id == R.id.email_sign_in) {
            Log.w(TAG, "onClick() on R.id.email_sign_in");
            startActivity(new Intent(SignInGoogleActivity.this, SignInEmailActivity.class));
        }
    }



}
