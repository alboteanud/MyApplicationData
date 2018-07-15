package com.alboteanu.myapplicationdata;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.Constants;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;

import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_NOTE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;

public class DetailsActivity extends BaseActivity implements View.OnClickListener {
    private String key;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        key = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        updateUIfromFirebase(key);
        findViewById(R.id.ic_action_phone).setOnClickListener(this);
        findViewById(R.id.ic_action_message).setOnClickListener(this);
        findViewById(R.id.ic_action_email).setOnClickListener(this);
        findViewById(R.id.ic_action_date).setOnClickListener(this);
        findViewById(R.id.ic_action_note).setOnClickListener(this);

    }

    public void updateUI(@NonNull Contact contact) {
        getSupportActionBar().setTitle(contact.name);
        ((TextView) findViewById(R.id.phoneText)).setText(contact.phone);
        if(contact.phone != null)
            findViewById(R.id.ic_action_message).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.emailText)).setText(contact.email);
        ((TextView) findViewById(R.id.noteText)).setText(contact.note);
        if(contact.date > 0) {
            String date = DateFormat.getDateInstance().format(contact.date);
            ((TextView) findViewById(R.id.dateText)).setText(date);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quick_contact, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.action_delete_contact:
                createDeleteDialogAlert(key);
                return true;
            case R.id.action_edit:
                sendUserToEditActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ic_action_phone:
                String phoneNumber = ((TextView) findViewById(R.id.phoneText)).getText().toString();
                if (!phoneNumber.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                }
                break;
            case R.id.ic_action_message:
                String phoneMessage = ((TextView) findViewById(R.id.phoneText)).getText().toString();
                if (!phoneMessage.isEmpty()) {
                    Utils.composeSMS( new String[]{phoneMessage} , this);
                }
                break;
            case R.id.ic_action_email:
                String email = ((TextView) findViewById(R.id.emailText)).getText().toString();
                if (!email.isEmpty()) {
                    String[] emails = new String[]{email};
                    Utils.composeEmail(v.getContext(), emails);
                }
                break;
            case R.id.ic_action_date:
                Intent editDateIntent = new Intent(DetailsActivity.this, EditActivity.class);
                editDateIntent.putExtra(EXTRA_EDIT_DATE, key);
                startActivity(editDateIntent);
                break;
            case R.id.ic_action_note:
                Intent noteIntent = new Intent(DetailsActivity.this, EditActivity.class);
                noteIntent.putExtra(EXTRA_EDIT_NOTE, key);
                startActivity(noteIntent);
                break;
        }

    }

    private void sendUserToEditActivity(){
        Intent intent = new Intent(DetailsActivity.this, EditActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, key);
        startActivity(intent);
    }

    void updateUIfromFirebase(@NonNull final String contactKey) {
        final DatabaseReference ref = getUserNode().child(FIREBASE_LOCATION_CONTACTS).child(contactKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                if(contact != null){
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
                        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(Constants.ACTION_CONTACT_DELETED, contactKey);
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


    public static void deleteContact(String contactKey) {
        getUserNode().child(FIREBASE_LOCATION_CONTACTS + "/" + contactKey).removeValue();
    }


}
