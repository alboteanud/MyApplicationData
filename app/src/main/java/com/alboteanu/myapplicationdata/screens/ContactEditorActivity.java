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
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.models.Contact;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.R.layout.contact;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;


public class ContactEditorActivity extends BaseDetailsActivity
        implements View.OnClickListener {
    CheckBox checkBox_6M;
    EditText nameText, phoneText, emailText, otherText;
    public long newDate;
    private TextView returnText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        if(contactKey != null)
            getContactFromFirebaseAndUpdateUI();
    }

    public void updateUI(Contact contact){
        if(contact != null) {
            nameText.setText(contact.name);
            phoneText.setText(contact.phone);
            emailText.setText(contact.email);
            otherText.setText(contact.other);
            if(contact.date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(contact.date);
                returnText.setText(Utils.calendarToString(calendar));
            }
        }
        nameText.requestFocus();
    }

     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        if (contactKey == null) {
            menu.findItem(R.id.action_delete_contact).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_contact) {
            createDeleteDialogAlert(contactKey);
            return true;
        }
        if (id == R.id.action_save) {
            if (saveAndSend()) {
                goToQuickContactActivity();
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
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    returnText.setText(Utils.calendarToString(calendar));
                    newDate = calendar.getTimeInMillis();
                } else {
                    returnText.setText(null);
                    newDate = 0;
                }
            }
        });
    }

    private void goToQuickContactActivity() {
        Intent intent = new Intent(this, QuickContactActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, contactKey);
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

        if(phone.isEmpty())
            phone = null;

        if (email.isEmpty())
            email = null;
        else if ( !Utils.isValidEmail(email) ) {
            emailText.setError(getString(R.string.invalid_email));
            return false;
        }

        if(other.isEmpty())
            other = null;

        if (contactKey == null)
            contactKey = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_S).push().getKey();  //generate new key

        Map<String, Object> updates = new HashMap<>();

        Contact contactShort = new Contact(name, newDate);
        Contact contact = new Contact(name, phone, email, other, newDate);

        Map<String, Object> contactMap = contactShort.toMap();
        Map<String, Object> contactDetMap = contact.toMap();
        updates.put(FIREBASE_LOCATION_CONTACT_S + "/" + contactKey, contactMap);
        updates.put(FIREBASE_LOCATION_CONTACTS + "/" + contactKey, contactDetMap);
        updates.put(FIREBASE_LOCATION_EMAIL + "/" + contactKey, email);
//        Contact dateToReturn = new Contact(this.name, phoneS, this.date);  //data si tel
//        Map<String, Object> returnMap = contactShort.toMap();
        Utils.getUserNode().updateChildren(updates);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.clepsidra_icon:
                showDatePickerDialog();
                break;
            case R.id.return_date_textView:
                showDatePickerDialog();
                break;
        }
    }
}
