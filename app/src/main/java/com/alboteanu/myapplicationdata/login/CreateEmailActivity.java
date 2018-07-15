package com.alboteanu.myapplicationdata.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;

public class CreateEmailActivity extends AppCompatActivity {
    private EditText emailField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_email);
        findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onNextPressed();
            }
        });
        emailField = findViewById(R.id.field_email);
        emailField.setText(getIntent().getStringExtra("email"));
        emailField.setSelection(emailField.getText().length());
        emailField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    onNextPressed();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void onNextPressed() {
        final String email = emailField.getText().toString();
        if(validateEmail(email)){
            Intent intent = new Intent(CreateEmailActivity.this, CreatePasswordActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        }
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            emailField.setError(getString(R.string.required));
            return false;
        } else if (Utils.isValidEmail(emailField.getText().toString())) {
            emailField.setError(getString(R.string.invalid_email));
            return false;
        } else
            emailField.setError(null);
        return true;
    }

}
