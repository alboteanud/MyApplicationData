package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.ContactShort;

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
        if (contactLong == null) {
            getContactFromFirebaseAndUpdateUI();
        }else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)) {
            contactShort = (ContactShort) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
            name = contactShort.name;
            phone = contactShort.phone;
            date = contactShort.date;
        }
        updateUI();
    }

    public void updateUI() {
        if(contactLong != null){
            ((TextView) findViewById(R.id.name)).setText(contactLong.name);
            ((TextView) findViewById(R.id.phone)).setText(contactLong.phone);
            ((TextView) findViewById(R.id.email)).setText(contactLong.email);
            ((TextView) findViewById(R.id.other)).setText(contactLong.other);
            if(contactLong.date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(contactLong.date);
                String dateString = Utils.calendarToString(calendar);
                ((TextView) findViewById(R.id.return_date_textView)).setText(dateString);
            }
        }   //short update
        else if(contactShort != null){
            ((TextView) findViewById(R.id.name)).setText(contactShort.name);
            ((TextView) findViewById(R.id.phone)).setText(contactShort.phone);
            if(contactShort.date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(contactShort.date);
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
        if (contactLong != null){
            intent.putExtra(FIREBASE_LOCATION_CONTACT, contactLong);
        } else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)){
            ContactShort contactShort = (ContactShort) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
            intent.putExtra(FIREBASE_LOCATION_CONTACT_S, contactShort);
        }
        startActivity(intent);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ic_action_phone:
                if (contactLong != null){
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactLong.phone)));
                } else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)) {
                    ContactShort contactShort = (ContactShort) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contactShort.phone)));
                }
                break;
            case R.id.ic_action_email:
                if(contactLong != null && contactLong.email != null){
                    String[] emails = new String[]{ contactLong.email};
                    Utils.composeEmail(v.getContext(), emails, null);
                }
                break;
            case R.id.ic_action_note:

                break;
            case R.id.ic_action_date_return:
                goToEditActivity();
                break;
        }
    }

}
