package com.alboteanu.myapplicationdata.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.MainActivity;
import com.alboteanu.myapplicationdata.models.User;
import com.alboteanu.myapplicationdata.others.Constants;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Map;

import static com.alboteanu.myapplicationdata.R.id.create_account_button;

public class CreatePasswordActivity extends BaseActivity {
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_password);
        mPasswordField = findViewById(R.id.field_password);
        findViewById(create_account_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String password = mPasswordField.getText().toString();
                if (validatePassword(password)) {
                    Utils.saveEmail(CreatePasswordActivity.this, getIntent().getStringExtra("email"));
                    createAccount(getIntent().getStringExtra("email"), password);
                }
            }
        });
    }



    public static void writeNewUser(String email) {
        User user = new User(email, Utils.calendarToString(Calendar.getInstance()));
        Map<String, Object> userMap = user.toMap();
        getUserNode().child(Constants.FIREBASE_USER).updateChildren(userMap);
    }

    private boolean validatePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.required));
            return false;
        } else
            mPasswordField.setError(null);
        return true;
    }

    private void createAccount(@NonNull final String email, @NonNull final String password) {
        showProgressDialog();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgressDialog();
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();

                            writeNewUser(user.getEmail());


                            goToMainActivity(CreatePasswordActivity.this);
                        } else {
                            String message = task.getException().getMessage();
                            Toast.makeText(CreatePasswordActivity.this, message,
                                    Toast.LENGTH_LONG).show();
                        }
                    }


                });
    }

    public static void goToMainActivity(Context context) {
        Intent intentToMainActivity = new Intent(context, MainActivity.class);
        intentToMainActivity.setAction(Constants.ACTION_UPDATE_LOCAL_CONTACTS);
        intentToMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intentToMainActivity);
    }

    private ProgressDialog mProgressDialog;
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


}
