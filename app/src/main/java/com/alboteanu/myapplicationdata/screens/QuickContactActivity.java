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
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.models.Contact;

import java.util.Calendar;

import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN_RETUR;

public class QuickContactActivity extends BaseDetailsActivity implements View.OnClickListener {
    public static final String ACTION_SHOW_DATE_PICKER = "action_date";
    String mContactKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get post key from intent
        mContactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        if (mContactKey == null) {
            throw new IllegalArgumentException("Must pass EXTRA_CONTACT_KEY");
        }

        updateUIfromFirebase(mContactKey);
        findViewById(R.id.ic_action_phone).setOnClickListener(this);
        findViewById(R.id.ic_action_message).setOnClickListener(this);
        findViewById(R.id.ic_action_email).setOnClickListener(this);
        findViewById(R.id.ic_action_date_return).setOnClickListener(this);
    }

    public void updateUI(Contact contact) {
        ((TextView) findViewById(R.id.nameText)).setText(contact.name);
        ((TextView) findViewById(R.id.phoneText)).setText(contact.phone);
        if(contact.phone != null)
            findViewById(R.id.ic_action_message).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.emailText)).setText(contact.email);
        ((TextView) findViewById(R.id.noteText)).setText(contact.other);
        if (contact.retur.containsKey(FIREBASE_LOCATION_RETURN_RETUR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(contact.retur.get(FIREBASE_LOCATION_RETURN_RETUR));
            String dateString = Utils.calendarToString(cal);
            ((TextView) findViewById(R.id.dateText)).setText(dateString);
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
            createDeleteDialogAlert(mContactKey);
            return true;
        } else if (id == R.id.action_edit) {
            sendUserToEditActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ic_action_phone:
                String phoneNumber = ((TextView) findViewById(R.id.phoneText)).getText().toString();
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                }
                break;
            case R.id.ic_action_message:
                String phoneMessage = ((TextView) findViewById(R.id.phoneText)).getText().toString();
                if (phoneMessage != null && !phoneMessage.isEmpty()) {
                    Utils.composeSMS( new String[]{phoneMessage} , this);
                }
                break;
            case R.id.ic_action_email:
                String email = ((TextView) findViewById(R.id.emailText)).getText().toString();
                if (!email.isEmpty()) {
                    String[] emails = new String[]{email};
                    Utils.composeEmail(v.getContext(), emails, null);
                }
                break;
            case R.id.ic_action_date_return:
                Intent intent = new Intent(QuickContactActivity.this, EditActivity.class);
                intent.putExtra(EXTRA_CONTACT_KEY, mContactKey);
                intent.setAction(ACTION_SHOW_DATE_PICKER);
                startActivity(intent);
                break;
        }

    }

    private void sendUserToEditActivity(){
        Intent intent = new Intent(QuickContactActivity.this, EditActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, mContactKey);
        startActivity(intent);
    }
}
