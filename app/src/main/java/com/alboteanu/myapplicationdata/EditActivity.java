package com.alboteanu.myapplicationdata;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.DatePickerFragment;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.ACTION_CONTACT_DELETED;
import static com.alboteanu.myapplicationdata.DetailsActivity.deleteContact;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_NOTE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;


public class EditActivity extends BaseActivity
        implements View.OnClickListener, DatePickerFragment.OnDateSelectedListener {
    private EditText nameText, phoneText, emailText, editTextNote, dateText;
    private String key;
    private long longDate = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        if (getIntent().hasExtra(EXTRA_CONTACT_KEY)) {
            key = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        } else if (getIntent().hasExtra(EXTRA_EDIT_NOTE)) {
            key = getIntent().getStringExtra(EXTRA_EDIT_NOTE);
            editTextNote.requestFocus();
        } else if (getIntent().hasExtra(EXTRA_EDIT_DATE)) {
            key = getIntent().getStringExtra(EXTRA_EDIT_DATE);
            showDatePickerDialog();
        }
        if (key != null && savedInstanceState == null)  // exista un contact si e prima data
            updateUIfromFirebase(key);
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
        editTextNote.setText(contact.note);
        longDate = contact.date;
        if (longDate > 0) {
            String dateFormated = DateFormat.getDateInstance().format(longDate);
            dateText.setText(dateFormated);
        }
        if (getIntent().hasExtra(EXTRA_EDIT_NOTE)) {
            editTextNote.setSelection(editTextNote.length());
            editTextNote.requestFocus();
        } else {
            nameText.requestFocus();
        }

    }

    @Override
    public void onDateSelected(long date) {
        String dateFormated = DateFormat.getDateInstance().format(date);
        dateText.setText(dateFormated);
        dateText.requestFocus();
        this.longDate = date;
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        if (key == null)
            menu.findItem(R.id.action_delete_contact).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete_contact:
                createDeleteDialogAlert(key);
                break;
            case R.id.action_save:
                if (saveToFirebase()) {
                    Intent intent = new Intent(this, DetailsActivity.class);
                    intent.putExtra(EXTRA_CONTACT_KEY, key);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        nameText = findViewById(R.id.nameEditText);
        phoneText = findViewById(R.id.phoneEditText);
        emailText = findViewById(R.id.emailEditText);
        editTextNote = findViewById(R.id.noteEditText);
        dateText = findViewById(R.id.dateEditText);
        dateText.setOnClickListener(this);
        findViewById(R.id.icon_sandglass_edit_activity).setOnClickListener(this);
        findViewById(R.id.button_clear_date).setOnClickListener(this);
    }

    private boolean saveToFirebase() {
        String name = nameText.getText().toString();
        if (name.isEmpty()) {
            nameText.setError(getString(R.string.required));
            return false;
        }
        Contact contact = new Contact(name);
        Contact contactPhoneEmail = new Contact();
        Contact contactNameDate = new Contact(name);

        String phone = phoneText.getText().toString();
        contactPhoneEmail.phone = phone;  // even if null - to have complete list
        if (!phone.isEmpty())
            contact.phone = phone;


        String email = emailText.getText().toString();
        if (!email.isEmpty()) {
            if (Utils.isValidEmail(email)) {
                emailText.setError(getString(R.string.invalid_email));
                return false;
            }
            contact.email = email;
            contactPhoneEmail.email = email;
        }
        String note = editTextNote.getText().toString();
        if (!note.isEmpty())
            contact.note = note;

        contact.date = longDate;
        contactNameDate.date = longDate;

        Map<String, Object> updates = new HashMap<>();
        Map<String, Object> mapContact = contact.toMap();

        if (key == null)
            key = getUserNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();  //generate new key

        updates.put(FIREBASE_LOCATION_CONTACTS + "/" + key, mapContact);
        getUserNode().updateChildren(updates);
        return true;
    }

    @Override
    public void onClick(@NonNull View view) {
        switch (view.getId()) {
            case R.id.icon_sandglass_edit_activity:
//                showDatePickerDialog();
                addMonthsToDate();
                break;
            case R.id.dateEditText:
                showDatePickerDialog();
                break;
            case R.id.button_clear_date:
                longDate = -1;
                dateText.setText(null);
                break;
        }
    }

    private void addMonthsToDate() {
        Calendar calendar = Calendar.getInstance();
        if (longDate != -1)
            calendar.setTimeInMillis(longDate);
        calendar.add(Calendar.MONTH, 1);
        longDate = calendar.getTimeInMillis();

        String dateFormated = DateFormat.getDateInstance().format(longDate);
        dateText.setText(dateFormated);
    }

    void updateUIfromFirebase(@NonNull final String contactKey) {
        final DatabaseReference ref = getUserNode().child(FIREBASE_LOCATION_CONTACTS).child(contactKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                if (contact != null) {
                    updateUI(contact);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void createDeleteDialogAlert(final String contactKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation_dialog_delete))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        deleteContact(contactKey);
                        Intent intent = new Intent(EditActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(ACTION_CONTACT_DELETED, contactKey);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(@NonNull DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    void showDatePickerDialog() {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getFragmentManager(), "datePicker");
    }



}
