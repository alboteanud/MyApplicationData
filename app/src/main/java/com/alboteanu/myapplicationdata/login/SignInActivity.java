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
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.screens.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.alboteanu.myapplicationdata.R.id.create_account_text;
import static com.alboteanu.myapplicationdata.R.id.email_sign_in_button;

public class SignInActivity extends BaseActivity implements View.OnClickListener {
    EditText mEmailField, mPasswordField;
    FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    sendUserToMainActivity();
                }
                // updateUI
            }
        };

        setContentView(R.layout.activity_emailpassword);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        mEmailField.setText(getSavedEmail());
        if (!mEmailField.getText().toString().isEmpty())
            mPasswordField.requestFocus();
        findViewById(R.id.forgot_password).setOnClickListener(this);
        findViewById(R.id.email_sign_in_button).setOnClickListener(this);
        findViewById(create_account_text).setOnClickListener(this);

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
        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError(getString(R.string.required));
            return false;
        } else
            mPasswordField.setError(null);
        return true;
    }

    @Override
    public void onClick(View v) {
        String email = mEmailField.getText().toString();
        switch (v.getId()) {
            case create_account_text:
                Intent intent = new Intent(this, CreateEmailActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                break;
            case email_sign_in_button:
                if (validateEmail() && validatePassword()) {
                    saveEmail(email);
                    signIn(email, mPasswordField.getText().toString());
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



}
