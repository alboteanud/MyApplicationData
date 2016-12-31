package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAILS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAMES_DATES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;


public class EditActivity extends BaseDetailsActivity
        implements View.OnClickListener, DatePickerFragment.OnHeadlineSelectedListener {
    EditText nameText, phoneText, emailText, otherText, returnText;
    private String mContactKey;
    @Nullable
    private Calendar calendar;
    private CheckBox checkBox;
    ImageView clearDateButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        mContactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
//        Log.d("tag", "key " + mContactKey);
        if(savedInstanceState == null && mContactKey != null)
            updateUIfromFirebase(mContactKey);
        if(QuickContactActivity.ACTION_SHOW_DATE_PICKER.equals(getIntent().getAction())){
            showDatePickerDialog();
        }else if(QuickContactActivity.ACTION_NOTE.equals(getIntent().getAction())){
            otherText.requestFocus();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBoolean("onSave", true);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void updateUI(@NonNull Contact contact) {
        nameText.setText(contact.name);
        nameText.setSelection(contact.name.length());
        phoneText.setText(contact.phone);
        emailText.setText(contact.email);
        otherText.setText(contact.other);
        if(contact.retur.containsKey(FIREBASE_LOCATION_RETURN)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(contact.retur.get(FIREBASE_LOCATION_RETURN));
            returnText.setText(Utils.calendarToString(cal));
            calendar = cal;
            clearDateButton.setVisibility(View.VISIBLE);
        }else{
            clearDateButton.setVisibility(View.GONE);
        }
        if(QuickContactActivity.ACTION_NOTE.equals(getIntent().getAction())) {
            otherText.setSelection(otherText.length());
            otherText.requestFocus();
        }
        else
            nameText.requestFocus();
    }

    @Override
    public void onDateSelected(Calendar cal) {
        returnText.setText(Utils.calendarToString(cal));
        calendar = cal;
        checkBox.setChecked(false);
        clearDateButton.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        if(mContactKey == null)
            menu.findItem(R.id.action_delete_contact).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_contact:
                createDeleteDialogAlert(mContactKey);
                break;
            case R.id.action_save:
                if (isSucessfulSave()) {
                    Intent intent = new Intent(this, QuickContactActivity.class);
                    intent.putExtra(EXTRA_CONTACT_KEY, mContactKey);
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
        otherText = ((EditText) findViewById(R.id.noteEditText));
        returnText = ((EditText) findViewById(R.id.dateEditText));
        clearDateButton = (ImageView) findViewById(R.id.button_clear_date);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        returnText.setOnClickListener(this);
        findViewById(R.id.icon_sandglass).setOnClickListener(this);
        checkBox.setOnClickListener(this);
        clearDateButton.setOnClickListener(this);

    }

    private boolean isSucessfulSave() {
        Contact contact = new Contact();
        Contact nameDate = new Contact();
        if(nameText.getText().toString().isEmpty()) {
            nameText.setError(getString(R.string.required));
            return false;
        }else {
            String name = nameText.getText().toString();
            contact.name = name;
            nameDate.name = name;
        }
        if(!phoneText.getText().toString().isEmpty()) {
            String phone = phoneText.getText().toString();
            contact.phone = phone;
        }
        if(!emailText.getText().toString().isEmpty()) {
            String email = emailText.getText().toString();
            if (!Utils.isValidEmail(email)) {
                emailText.setError(getString(R.string.invalid_email));
                return false;
            }
            contact.email = email;
        }
        if(!otherText.getText().toString().isEmpty())
            contact.other = otherText.getText().toString();
        if(calendar != null){
            contact.retur.put(FIREBASE_LOCATION_RETURN, calendar.getTimeInMillis());
            nameDate.retur.put(FIREBASE_LOCATION_RETURN, calendar.getTimeInMillis());
        }

        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> contactMap = contact.toMap();
        Map<String, Object> nameDateMap = nameDate.toMap();

        if(mContactKey == null)
            mContactKey = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();  //generate new key

        updates.put(FIREBASE_LOCATION_CONTACTS + "/" + mContactKey, contactMap);
        updates.put(FIREBASE_LOCATION_NAMES_DATES + "/" + mContactKey, nameDateMap);
        updates.put(FIREBASE_LOCATION_PHONES + "/" + mContactKey, contact.phone);
        updates.put(FIREBASE_LOCATION_EMAILS + "/" + mContactKey, contact.email);
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
                    returnText.setText(Utils.calendarToString(calendar));
                    clearDateButton.setVisibility(View.VISIBLE);
                }else {
                    calendar = null;
                    returnText.setText(null);
//                    clearDateButton.setVisibility(View.GONE);
                }
                break;
            case R.id.button_clear_date:
                calendar = null;
                returnText.setText(null);
                checkBox.setChecked(false);
                break;
        }
    }



}
