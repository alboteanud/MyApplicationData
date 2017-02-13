package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.DatePickerFragment;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.models.Contact;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.R.id.phoneEditText;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_NOTE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAMES_DATES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONES_EMAILS;


public class EditActivity extends BaseDetailsActivity
        implements View.OnClickListener, DatePickerFragment.OnDateSelectedListener {
    EditText nameText, phoneText, emailText, editTextNote, dateText;
    private String key;
    @Nullable
    private Calendar calendar;
    private CheckBox checkBox;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        if(getIntent().hasExtra(EXTRA_CONTACT_KEY)){
            key = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        }else if(getIntent().hasExtra(EXTRA_EDIT_NOTE)){
            key = getIntent().getStringExtra(EXTRA_EDIT_NOTE);
            editTextNote.requestFocus();
        }else if(getIntent().hasExtra(EXTRA_EDIT_DATE)){
            key = getIntent().getStringExtra(EXTRA_EDIT_DATE);
            showDatePickerDialog();
        }
        if(key != null)
            updateUIfromFirebase(key);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBoolean("onSave", true);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateUI(Contact contact) {
        nameText.setText(contact.name);
        nameText.setSelection(contact.name.length());
        phoneText.setText(contact.phone);
        emailText.setText(contact.email);
        editTextNote.setText(contact.note);
        Log.d("tag EditActivity", "contact return_date_millis = " + contact.return_date_millis);
        if(contact.return_date_millis != -1) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(contact.return_date_millis);
            dateText.setText(Utils.calendarToString(cal));
            calendar = cal;
        }
        if(getIntent().hasExtra(EXTRA_EDIT_NOTE)) {
            editTextNote.setSelection(editTextNote.length());
            editTextNote.requestFocus();
        }
        else{
            nameText.requestFocus();
        }

    }

    @Override
    public void onDateSelected(Calendar cal) {
        dateText.setText(Utils.calendarToString(cal));
        calendar = cal;
        checkBox.setChecked(false);
        dateText.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        if(key == null)
            menu.findItem(R.id.action_delete_contact).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_contact:
                createDeleteDialogAlert(key);
                break;
            case R.id.action_save:
                if (saveToFirebase()) {
                    Intent intent = new Intent(this, QuickContactActivity.class);
                    intent.putExtra(EXTRA_CONTACT_KEY, key);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initViews() {
        nameText = ((EditText) findViewById(R.id.nameEditText));
        phoneText = ((EditText) findViewById(phoneEditText));
        emailText = ((EditText) findViewById(R.id.emailEditText));
        editTextNote = ((EditText) findViewById(R.id.noteEditText));
        dateText = ((EditText) findViewById(R.id.dateEditText));
        checkBox = (CheckBox) findViewById(R.id.checkBoxDate6M);
        dateText.setOnClickListener(this);
        findViewById(R.id.icon_sandglass_edit_activity).setOnClickListener(this);
        checkBox.setOnClickListener(this);
        findViewById(R.id.button_clear_date).setOnClickListener(this);
    }

    private boolean saveToFirebase() {
        String name = nameText.getText().toString();
        if(name.isEmpty()) {
            nameText.setError(getString(R.string.required));
            return false;
        }
        Contact contact = new Contact(name);
        Contact contactPhoneEmail = new Contact();
        Contact contactNameDate = new Contact(name);

        String phone = phoneText.getText().toString();
        contactPhoneEmail.phone = phone;  // even if null - to have complete list
        if(!phone.isEmpty())
            contact.phone = phone;


        String email = emailText.getText().toString();
        if(!email.isEmpty()) {
            if (!Utils.isValidEmail(email)) {
                emailText.setError(getString(R.string.invalid_email));
                return false;
            }
            contact.email = email;
            contactPhoneEmail.email = email;
        }
        String note = editTextNote.getText().toString();
        if(!note.isEmpty())
            contact.note = note;

        contact.calendar = calendar;
        contactNameDate.calendar = calendar;

        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> mapContact = contact.toMap();
        Map<String, Object> mapContactNameDate = contactNameDate.toMap();
        Map<String, Object> mapContactPhoneEmail = contactPhoneEmail.toMap();

        if(key == null)
            key = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();  //generate new key

        updates.put(FIREBASE_LOCATION_CONTACTS + "/" + key, mapContact);
        updates.put(FIREBASE_LOCATION_NAMES_DATES + "/" + key, mapContactNameDate);
        updates.put(FIREBASE_LOCATION_PHONES_EMAILS + "/" + key, mapContactPhoneEmail);
        Utils.getUserNode().updateChildren(updates);
        return true;
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.icon_sandglass_edit_activity:
                showDatePickerDialog();
                break;
            case R.id.dateEditText:
                showDatePickerDialog();
                break;
            case R.id.checkBoxDate6M:
                if(checkBox.isChecked()) {
                    calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    dateText.setText(Utils.calendarToString(calendar));
                }else {
                    calendar = null;
                    dateText.setText(null);
                }
                break;
            case R.id.button_clear_date:
                calendar = null;
                dateText.setText(null);
                checkBox.setChecked(false);
                break;
        }
    }



}
