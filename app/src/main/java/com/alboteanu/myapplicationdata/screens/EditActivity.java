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
import android.widget.ImageView;

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
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;


public class EditActivity extends BaseDetailsActivity
        implements View.OnClickListener, DatePickerFragment.OnDateSelectedListener {
    EditText nameText, phoneText, emailText, editTextNote, dateText;
    private String key;
    @Nullable
    private Calendar calendar;
    private CheckBox checkBox;
    ImageView clearDateX;

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
        if(key == null){
            clearDateX.setVisibility(View.GONE);
        }else {
            if(savedInstanceState == null)
                updateUIfromFirebase(key);
            else
                clearDateX.setVisibility(View.VISIBLE);
        }
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
        if(contact.retur.containsKey(FIREBASE_LOCATION_RETURN)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(contact.retur.get(FIREBASE_LOCATION_RETURN));
            dateText.setText(Utils.calendarToString(cal));
            calendar = cal;
            clearDateX.setVisibility(View.VISIBLE);
        }else{
            clearDateX.setVisibility(View.GONE);
        }
        if(getIntent().hasExtra(EXTRA_EDIT_NOTE)) {
            editTextNote.setSelection(editTextNote.length());
            editTextNote.requestFocus();
        }
        else{
            nameText.requestFocus();
            Log.d("tag EditActivity", "nameText.requestFocus()");
        }

    }

    @Override
    public void onDateSelected(Calendar cal) {
        dateText.setText(Utils.calendarToString(cal));
        calendar = cal;
        checkBox.setChecked(false);
        clearDateX.setVisibility(View.VISIBLE);
        dateText.requestFocus();
        Log.d("tag EditActivity", "dateText.requestFocus()");
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
                if (isSucessfulSave()) {
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
        clearDateX = (ImageView) findViewById(R.id.button_clear_date);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        dateText.setOnClickListener(this);
        findViewById(R.id.icon_sandglass).setOnClickListener(this);
        checkBox.setOnClickListener(this);
        clearDateX.setOnClickListener(this);


    }

    private boolean isSucessfulSave() {
        String name = nameText.getText().toString();
        if(name.isEmpty()) {
            nameText.setError(getString(R.string.required));
            return false;
        }
        Contact contact = new Contact(name);
        Contact contactPhoneEmail = new Contact();
        Contact contactNameDate = new Contact(name);

        String phone = phoneText.getText().toString();
        if(!phone.isEmpty()) {
            contact.phone = phone;
            contactPhoneEmail.phone = phone;
        }
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

        if(calendar!=null){
            Long dateMills = calendar.getTimeInMillis();
            contact.retur.put(FIREBASE_LOCATION_RETURN, dateMills);
            contactNameDate.retur.put(FIREBASE_LOCATION_RETURN, dateMills);
        }
        Map<String, Object> updates = new HashMap<>();
//        Contact finalContact = new Contact(contact.name, contact.phone, contact.email, contact.note, contact.retur);
        Map<String, Object> mapContact = contact.toMap();
        Map<String, Object> mapContactNameDate = contactNameDate.toMap();
        Map<String, Object> mapContactPhoneEmail = contactPhoneEmail.toMap();

        if(key == null)
            key = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();  //generate new key

        updates.put(FIREBASE_LOCATION_CONTACTS + "/" + key, mapContact);
        updates.put(FIREBASE_LOCATION_NAMES_DATES + "/" + key, mapContactNameDate);
        updates.put(FIREBASE_LOCATION_PHONES_EMAILS + "/" + key, mapContactPhoneEmail);
//        updates.put(FIREBASE_LOCATION_PHONES + "/" + key, contact.phone);
//        updates.put(FIREBASE_LOCATION_EMAILS + "/" + key, contact.email);
        Utils.getUserNode().updateChildren(updates);
        return true;
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.icon_sandglass:
                showDatePickerDialog();
                break;
            case R.id.dateEditText:
                showDatePickerDialog();
                break;
            case R.id.checkBox:
                if(checkBox.isChecked()) {
                    calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    dateText.setText(Utils.calendarToString(calendar));
                    clearDateX.setVisibility(View.VISIBLE);
                }else {
                    calendar = null;
                    dateText.setText(null);
//                    clearDateX.setVisibility(View.GONE);
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
