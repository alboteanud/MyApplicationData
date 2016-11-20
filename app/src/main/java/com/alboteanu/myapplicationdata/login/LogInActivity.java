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

package com.alboteanu.myapplicationdata.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.alboteanu.myapplicationdata.R.id.email_sign_in_button;

public class LogInActivity extends BaseActivity implements View.OnClickListener {
    EditText mEmailField, mPasswordField;
    TextView create_account_text, forgot_pass_text;
    Button sign_in_button;
    String savedEmail;
    public FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emailpassword);
        initViewsAndSetListeners();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {  //signed out

                } else { // logged in
                        sendUserToMainActivity();
                        finish();
                }
                // updateUI
            }
        };

    }

    private void initViewsAndSetListeners(){
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        sign_in_button = (Button) findViewById(R.id.email_sign_in_button);
        create_account_text = (TextView) findViewById(R.id.create_account_text);
        forgot_pass_text = (TextView) findViewById(R.id.forgot_password);
        forgot_pass_text.setOnClickListener(this);
        sign_in_button.setOnClickListener(this);
        create_account_text.setOnClickListener(this);
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
                Intent intent = new Intent(this, CreateEmailActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                break;
            case email_sign_in_button:
                if (validateEmail() && validatePassword()) {
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        savedEmail = getSavedEmail();
        mEmailField.setText(savedEmail);
        if (savedEmail != null)
            mPasswordField.requestFocus();
        Log.d("LogInActivity resumed", "savedEmail  " + getSavedEmail());
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideProgressDialog();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


}
