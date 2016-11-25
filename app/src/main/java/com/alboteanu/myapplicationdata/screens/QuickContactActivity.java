package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Constants;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.models.Contact;

import java.util.Calendar;

import static com.alboteanu.myapplicationdata.R.layout.contact;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;

public class QuickContactActivity extends BaseDetailsActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getContactFromFirebaseAndUpdateUI();
    }

    public void updateUI(Contact contact) {
        if(contact != null){
            ((TextView) findViewById(R.id.name)).setText(contact.name);
            ((TextView) findViewById(R.id.phone)).setText(contact.phone);
            ((TextView) findViewById(R.id.email)).setText(contact.email);
            ((TextView) findViewById(R.id.other)).setText(contact.other);
            if(contact.date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(contact.date);
                String dateString = Utils.calendarToString(calendar);
                ((TextView) findViewById(R.id.return_date_textView)).setText(dateString);
            }
        }
        findViewById(R.id.ic_action_phone).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quick_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_contact) {
            createDeleteDialogAlert(contactKey);
            return true;
        }else if (id == R.id.action_edit) {
            goToEditActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToEditActivity() {
        Intent intent = new Intent(QuickContactActivity.this, ContactEditorActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, contactKey);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_action_phone:
                String phoneNumber = ((TextView) findViewById(R.id.phone)).getText().toString();
                if (phoneNumber != null && !phoneNumber.isEmpty()){
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                }
                break;
            case R.id.ic_action_email:
                String email = ((TextView) findViewById(R.id.email)).getText().toString();
                if(!email.isEmpty()){
                    String[] emails = new String[]{ email};
                    Utils.composeEmail(v.getContext(), emails, null);
                }
                break;
            case R.id.ic_action_note:

                break;
            case R.id.ic_action_date_return:
//                goToEditActivity();
                break;
        }
    }

}
