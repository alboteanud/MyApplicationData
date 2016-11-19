/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static android.R.attr.defaultValue;

public class EmailPasswordActivity extends BaseActivity implements View.OnClickListener {
    EditText mEmailField, mPasswordField;
    TextView create_account_text, forgot_pass_text, have_account_text;
    Button sign_in_button, create_account_button;
    FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences sharedPref;
    String savedEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        savedEmail = sharedPref.getString(getString(R.string.saved_email), null);
        initViewsAndSetListeners();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    sendUserToMainActivity();
                    finish();
                } else {
//                  signed_out
                }
                // updateUI
            }
        };
    }

    private void initViewsAndSetListeners(){
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        sign_in_button = (Button) findViewById(R.id.email_sign_in_button);
        create_account_button = (Button) findViewById(R.id.create_account_button);
        create_account_text = (TextView) findViewById(R.id.create_account_text);
        forgot_pass_text = (TextView) findViewById(R.id.forgot_password);
        have_account_text = (TextView) findViewById(R.id.have_account_text);

        mEmailField.setText(savedEmail);
        if (savedEmail!=null)
            mPasswordField.requestFocus();

        forgot_pass_text.setOnClickListener(this);
        have_account_text.setOnClickListener(this);
        sign_in_button.setOnClickListener(this);
        create_account_button.setOnClickListener(this);
        create_account_text.setOnClickListener(this);
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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Utils.writeNewUser(task.getResult().getUser().getEmail());
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(EmailPasswordActivity.this, message,
                                    Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }


                });
    }

    private void signIn(String email, String password) {
        if (!validateEmail() || !validatePassword())
            return;
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            String message = task.getException().getMessage();
                            Toast.makeText(EmailPasswordActivity.this, message,
                                    Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    private boolean validateEmail() {
        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getString(R.string.required));
            return false;
        } else if (!Utils.isValidEmail(email)) {
            mEmailField.setError(getString(R.string.invalid_email));
            return false;
        } else
            mEmailField.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.required));
            return false;
        } else
            mPasswordField.setError(null);
        return true;
    }

    @Override
    public void onClick(View v) {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();
        switch (v.getId()) {
            case R.id.create_account_text:
                prepareUItoCreateAccount();
                break;
            case R.id.create_account_button:
                if (validateEmail() && validatePassword()) {
                    if(email != savedEmail)
                        saveEmail(email);
                    createAccount(email, password);
                }
                break;
            case R.id.email_sign_in_button:
                if (validateEmail() && validatePassword()) {
                    if(email != savedEmail)
                        saveEmail(email);
                    signIn(email, password);
                }
                break;
            case R.id.forgot_password:
                if (validateEmail()) {
                    mAuth.sendPasswordResetEmail(email);
                    Snackbar.make(v, getString(R.string.need_email, email), Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.have_account_text:
                prepareUItoSignIn();
                break;
        }
    }

    private void prepareUItoSignIn() {
        create_account_button.setVisibility(View.GONE);
        have_account_text.setVisibility(View.GONE);
        sign_in_button.setVisibility(View.VISIBLE);
        create_account_text.setVisibility(View.VISIBLE);
    }

    private void prepareUItoCreateAccount() {
        create_account_button.setVisibility(View.VISIBLE);
        have_account_text.setVisibility(View.VISIBLE);
        sign_in_button.setVisibility(View.GONE);
        create_account_text.setVisibility(View.GONE);
    }

    private void saveEmail(String emailToSave) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.saved_email), emailToSave);
            editor.apply();
    }
}
