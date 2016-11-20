package com.alboteanu.myapplicationdata.login;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import static android.R.attr.password;
import static com.alboteanu.myapplicationdata.R.id.create_account_button;

public class CreatePasswordActivity extends BaseActivity {
    Button create_account_button;
    EditText mPasswordField;
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
        mPasswordField = (EditText) findViewById(R.id.field_password);
        email = getIntent().getStringExtra("email");
        create_account_button = (Button) findViewById(R.id.create_account_button);
        create_account_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validatePassword()) {
                    saveEmail(email);
                    createAccount(email, password);
                }
            }
        });
    }

    private boolean validatePassword() {
        password = mPasswordField.getText().toString();
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
                        if (task.isSuccessful()) {
                            Utils.writeNewUser( task.getResult().getUser().getEmail());
                            signIn(email, password);
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(CreatePasswordActivity.this, message,
                                    Toast.LENGTH_LONG).show();
                        }
                        hideProgressDialog();
                    }


                });
    }

}
