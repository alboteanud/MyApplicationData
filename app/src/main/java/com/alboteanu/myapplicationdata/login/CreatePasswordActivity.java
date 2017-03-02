package com.alboteanu.myapplicationdata.login;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import static com.alboteanu.myapplicationdata.R.id.create_account_button;

public class CreatePasswordActivity extends BaseActivity {
    EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        findViewById(create_account_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = mPasswordField.getText().toString();
                if (validatePassword(password)) {
                    saveEmail(getIntent().getStringExtra("email"));
                    createAccount(getIntent().getStringExtra("email"), password);
                }
            }
        });
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.required));
            return false;
        } else
            mPasswordField.setError(null);
        return true;
    }

    private void createAccount(final String email, final String password) {
        showProgressDialog();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(CreatePasswordActivity.this, message,
                                    Toast.LENGTH_LONG).show();
                        }
                    }


                });
    }


}
