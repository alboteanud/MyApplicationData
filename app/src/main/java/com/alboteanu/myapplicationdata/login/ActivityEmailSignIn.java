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
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import static com.alboteanu.myapplicationdata.R.id.button_sign_in;
import static com.alboteanu.myapplicationdata.R.id.create_account_text;

public class ActivityEmailSignIn extends BaseActivity implements View.OnClickListener {
    private static final String FORGOT_PASSWORD = "password";
    EditText mEmailField, mPasswordField;
    TextView forgotPassword;
    boolean isForgotPass;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_sign_in);
        mEmailField = (EditText) findViewById(R.id.field_email);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        forgotPassword = (TextView) findViewById(R.id.forgot_password);
        mEmailField.setText(getSavedEmail());
        if (!mEmailField.getText().toString().isEmpty())
            mPasswordField.requestFocus();
        forgotPassword.setOnClickListener(this);
        findViewById(R.id.button_sign_in).setOnClickListener(this);
        findViewById(create_account_text).setOnClickListener(this);
        mPasswordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    String email = mEmailField.getText().toString();
                    if (validateEmail() && validatePassword()) {
                        saveEmail(email);
                        emailSignIn(email, mPasswordField.getText().toString());
                        handled = true;
                    }

                }
                return handled;
            }
        });

        if(savedInstanceState !=null){
            isForgotPass = savedInstanceState.getBoolean(FORGOT_PASSWORD);
            if(isForgotPass)
                forgotPassword.setVisibility(View.VISIBLE);

        }
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
    public void onClick(@NonNull View v) {
        String email = mEmailField.getText().toString();
        switch (v.getId()) {
            case create_account_text:
                Intent intent = new Intent(this, CreateEmailActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                break;
            case button_sign_in:
                if (validateEmail() && validatePassword()) {
                    saveEmail(email);
                    emailSignIn(email, mPasswordField.getText().toString());
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
    public void onAuthFail(String message) {
        super.onAuthFail(message);
        mPasswordField.setError(null);
        isForgotPass = true;
        forgotPassword.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if(isForgotPass)
            outState.putBoolean(FORGOT_PASSWORD, isForgotPass);
        super.onSaveInstanceState(outState);
    }

    public void emailSignIn(@NonNull String email, @NonNull String password) {
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            onAuthFail(task.getException().getMessage());
                        }

                    }
                });
    }


}
