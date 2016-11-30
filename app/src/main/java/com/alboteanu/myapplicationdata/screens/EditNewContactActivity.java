package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.Utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.others.Utils.getUid;


public class EditNewContactActivity extends BaseDetailsActivity
        implements View.OnClickListener {
    CheckBox checkBox_6M;
    EditText nameText, phoneText, emailText, otherText;
    private TextView returnText;
    String mContactKey;
    private Calendar calendar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();

    }


    @Override
    public void onDateSelected(Calendar cal) {
        super.onDateSelected(cal);
        checkBox_6M.setChecked(false);
        returnText.setText(Utils.calendarToString(cal));
        calendar = cal;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        menu.findItem(R.id.action_delete_contact).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            if (saveAndSend()) {
                Intent intent = new Intent(this, QuickContactActivity.class);
                intent.putExtra(EXTRA_CONTACT_KEY, mContactKey);
                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initViews() {
        nameText = ((EditText) findViewById(R.id.name));
        phoneText = ((EditText) findViewById(R.id.phone));
        emailText = ((EditText) findViewById(R.id.email));
        otherText = ((EditText) findViewById(R.id.other));
        returnText = ((TextView) findViewById(R.id.return_date_textView));
        returnText.setOnClickListener(this);
        findViewById(R.id.clepsidra_icon).setOnClickListener(this);
        checkBox_6M = (CheckBox) findViewById(R.id.checkBox6Luni);
        checkBox_6M.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    returnText.setText(Utils.calendarToString(calendar));

                } else {
                    returnText.setText(null);
                    calendar = null;
                }
            }
        });
    }


    private boolean saveAndSend() {
        final String name = nameText.getText().toString();
        String phone = phoneText.getText().toString();
        String email = emailText.getText().toString();
        String other = otherText.getText().toString();

        if (name.isEmpty()) {
            nameText.setError(getString(R.string.required));
            return false;
        }

        if (phone.isEmpty())
            phone = null;

        if (email.isEmpty())
            email = null;
        else if (!Utils.isValidEmail(email)) {
            emailText.setError(getString(R.string.invalid_email));
            return false;
        }

        if (other.isEmpty())
            other = null;


        Contact contact = new Contact(name, phone, email, other);
        mContactKey = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_S).push().getKey();  //generate new key

        Map<String, Object> updates = new HashMap<>();

        Contact contactShort = new Contact(name);
        if(calendar != null){
            contact.retur.put(getUid(), calendar.getTimeInMillis());
            contactShort.retur.put(getUid(), calendar.getTimeInMillis());
        }
        Map<String, Object> contactMap = contactShort.toMap();
        Map<String, Object> contactDetMap = contact.toMap();
        updates.put(FIREBASE_LOCATION_CONTACT_S + "/" + mContactKey, contactMap);
        updates.put(FIREBASE_LOCATION_CONTACTS + "/" + mContactKey, contactDetMap);
        updates.put(FIREBASE_LOCATION_EMAIL + "/" + mContactKey, email);
        Utils.getUserNode().updateChildren(updates);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clepsidra_icon:
                showDatePickerDialog();
                break;
            case R.id.return_date_textView:
                showDatePickerDialog();
                break;
        }
    }
}
