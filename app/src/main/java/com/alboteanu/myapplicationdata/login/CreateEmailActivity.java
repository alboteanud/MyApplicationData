package com.alboteanu.myapplicationdata.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.Utils;

public class CreateEmailActivity extends BaseActivity {
    String email;
    EditText mEmailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_email);
        email = getIntent().getStringExtra("email");
        mEmailField = (EditText) findViewById(R.id.field_email);
        mEmailField.setText(email);
        mEmailField.setSelection(email.length());
        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateEmail()){
                    email = mEmailField.getText().toString();
                    Intent intent = new Intent(CreateEmailActivity.this, CreatePasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean validateEmail() {
        email = mEmailField.getText().toString();
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




}
