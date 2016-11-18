package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.ContactS;
import java.util.Calendar;

import static com.alboteanu.myapplicationdata.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT;

public class QuickContactActivity extends BaseDetailsActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (contact == null) {
            getContactFromFirebaseAndUpdateUI();
        }
        updateUI();
    }

    public void updateUI() {
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
        }   //short update
        else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)){
            ContactS contactS = (ContactS) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
            ((TextView) findViewById(R.id.name)).setText(contactS.name);
            ((TextView) findViewById(R.id.phone)).setText(contactS.phone);
            if(contactS.date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(contactS.date);
                String dateString = Utils.calendarToString(calendar);
                ((TextView) findViewById(R.id.return_date_textView)).setText(dateString);
            }
        }

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
        if (contact != null){
            intent.putExtra(FIREBASE_LOCATION_CONTACT, contact);
        } else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)){
            ContactS contactS = (ContactS) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
            intent.putExtra(FIREBASE_LOCATION_CONTACT_S, contactS);
        }
        startActivity(intent);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_action_phone:
                if (contact != null){
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.phone)));
                } else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)) {
                    ContactS contactS = (ContactS) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactS.phone)));
                }
                break;
            case R.id.ic_action_email:

                break;
            case R.id.ic_action_note:

                break;
            case R.id.ic_action_return:

                break;
        }
    }

}
