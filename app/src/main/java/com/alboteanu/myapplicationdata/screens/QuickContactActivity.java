package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.Utils;

import java.text.DateFormat;

import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_NOTE;

public class QuickContactActivity extends BaseDetailsActivity implements View.OnClickListener {
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
        super.updateUI(contact);
        getSupportActionBar().setTitle(contact.name);
//        ((TextView) findViewById(R.id.nameText)).setText(contact.name);
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
                Intent editDateIntent = new Intent(QuickContactActivity.this, EditActivity.class);
                editDateIntent.putExtra(EXTRA_EDIT_DATE, key);
                startActivity(editDateIntent);
                break;
            case R.id.ic_action_note:
                Intent noteIntent = new Intent(QuickContactActivity.this, EditActivity.class);
                noteIntent.putExtra(EXTRA_EDIT_NOTE, key);
                startActivity(noteIntent);
                break;
        }

    }

    private void sendUserToEditActivity(){
        Intent intent = new Intent(QuickContactActivity.this, EditActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, key);
        startActivity(intent);
    }


}
