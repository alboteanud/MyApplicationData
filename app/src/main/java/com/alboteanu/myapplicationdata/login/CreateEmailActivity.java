package com.alboteanu.myapplicationdata.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;

class CreateEmailActivity extends BaseActivity {
    EditText emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account_email);
        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailField.getText().toString();
                if(validateEmail(email)){
                    Intent intent = new Intent(CreateEmailActivity.this, CreatePasswordActivity.class);
                    intent.putExtra("email", email);
                    startActivity(intent);
                }
            }
        });
        emailField = (EditText) findViewById(R.id.field_email);
        emailField.setText(getIntent().getStringExtra("email"));
        emailField.setSelection(emailField.getText().length());
    }

    boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.required));
            return false;
        } else if (!Utils.isValidEmail(emailField.getText().toString())) {
            emailField.setError(getString(R.string.invalid_email));
            return false;
        } else
            emailField.setError(null);
        return true;
    }

}
