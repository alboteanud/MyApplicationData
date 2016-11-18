/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alboteanu.myapplicationdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.R.attr.defaultValue;

public class EmailPasswordActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "EmailPassword";

//    private TextView mStatusTextView;
    private EditText mEmailField;
    private EditText mPasswordField;

    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);

        // Views
//        mStatusTextView = (TextView) findViewById(R.id.status);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);

        // Buttons
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(R.id.create_account_button).setOnClickListener(this);
        findViewById(R.id.create_account_text).setOnClickListener(this);
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.have_account_text).setOnClickListener(this);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        String savedEmail = sharedPref.getString(getString(R.string.saved_email), "");
        if(!savedEmail.equals("")){
            mEmailField.setText(savedEmail);
            mPasswordField.requestFocus();
        }

        // [START auth_state_listener]
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    sendUserToMainActivity();
                    finish();
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                updateUI(user);
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void createAccount(String email, String password) {

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if(task.isSuccessful()) {
                            Utils.writeNewUser(task.getResult().getUser().getEmail());
                        }else {
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void signOut() {
        mAuth.signOut();
        updateUI(null);
    }

    private boolean validateForm() {
      if(!validateEmail() || !validatePassword())
          return false;

        return true;
    }

    private boolean validateEmail() {
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getString(R.string.required));
            return false;
        } else if ( !Utils.isValidEmail(email) ){
            mEmailField.setError(getString(R.string.invalid_email));
            return false;
        } else {
            mEmailField.setError(null);
            return true;
        }
    }

 private boolean validatePassword() {
     String password = mPasswordField.getText().toString();
     if (TextUtils.isEmpty(password)) {
         mPasswordField.setError(getString(R.string.required));
         return false;
     } else {
         mPasswordField.setError(null);
         return true;
     }
    }



    private void updateUI(FirebaseUser user) {
        hideProgressDialog();
        if (user == null) {
            //            mStatusTextView.setText(R.string.log_in_text);

//            findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
//            findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);

        } else {
            //            mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail()));

//            findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
//            findViewById(R.id.email_password_fields).setVisibility(View.GONE);

            findViewById(R.id.create_account_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.create_account_text).setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        switch (v.getId()) {
            case R.id.create_account_text:
                updateUI(null);
                findViewById(R.id.create_account_button).setVisibility(View.VISIBLE);
                findViewById(R.id.have_account_text).setVisibility(View.VISIBLE);
                findViewById(R.id.email_sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.create_account_text).setVisibility(View.GONE);
                break;
            case R.id.create_account_button:
                if (validateForm()) {
                    saveEmail(email);
                    createAccount(email, password);
                }
                break;
            case R.id.email_sign_in_button:
                if (validateForm()) {
                    saveEmail(email);
                    signIn(email, password);
                }
                break;
            case R.id.forgot_password:
                if(validateEmail()){
                    mAuth.sendPasswordResetEmail(email);
                    Snackbar.make(v, getString(R.string.need_email), Snackbar.LENGTH_LONG).show();
                }else{

                }
                break;
            case R.id.have_account_text:
                updateUI(null);
                findViewById(R.id.create_account_button).setVisibility(View.GONE);
                findViewById(R.id.have_account_text).setVisibility(View.GONE);
                findViewById(R.id.email_sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.create_account_text).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void saveEmail(String emailToSave){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.saved_email), emailToSave);
        editor.apply();
    }


}
