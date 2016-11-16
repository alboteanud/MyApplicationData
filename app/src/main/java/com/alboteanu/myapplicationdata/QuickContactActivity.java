package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.ContactDetailed;
import com.alboteanu.myapplicationdata.models.FixedFields;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_DETAILED;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_FIXED;
import static com.alboteanu.myapplicationdata.ContactEditorActivity.EXTRA_CONTACT_KEY;

public class QuickContactActivity extends BaseActivity {
    long returnDate;
    TextView name, nameF, phone, phoneF, email, emailF, other1, other1F, returnD, returnF;
    Contact contact;
    ContactDetailed contactDetailed;
    String contactKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();
        contactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT)) {
            contact = (Contact) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT);
            populateMobilesSimple();
            updateFieldsFromFirebase();

        }
        else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_DETAILED)) {
            contactDetailed = (ContactDetailed) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_DETAILED);
            populateMobiles();
        }
        populateFixed();
    }

    private void populateMobiles() {
        name.setText(contactDetailed.name);
        phone.setText(contactDetailed.phone);
        email.setText(contactDetailed.email);
        other1.setText(contactDetailed.others1);
        returnDate = contactDetailed.date;
        if(returnDate != 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(returnDate);
            String dateString = Utils.calendarToString(calendar);
            returnD.setText(dateString);
        }
    }

    private void populateMobilesSimple() {
        name.setText(contact.name);
        phone.setText(contact.phone);
        returnD.setText(String.valueOf(contact.date));
    }

    private void populateFixed() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_FIXED)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FixedFields fixedFields = dataSnapshot.getValue(FixedFields.class);
                        if (fixedFields != null) {
                            nameF.setText(fixedFields.name);
                            phoneF.setText(fixedFields.phone);
                            emailF.setText(fixedFields.email);
                            other1F.setText(fixedFields.other);
                            returnF.setText(fixedFields.date);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public void initViews() {
        name = ((TextView) findViewById(R.id.name));
        nameF = ((TextView) findViewById(R.id.nameF));
        phone = ((TextView) findViewById(R.id.phone));
        phoneF = ((TextView) findViewById(R.id.phoneF));
        email = ((TextView) findViewById(R.id.email));
        emailF = ((TextView) findViewById(R.id.emailF));
        other1 = ((TextView) findViewById(R.id.other1));
        other1F = ((TextView) findViewById(R.id.other1F));
        returnD = ((TextView) findViewById(R.id.returnD));
        returnF = ((TextView) findViewById(R.id.returnF));
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
        if (contactDetailed != null){
            intent.putExtra(FIREBASE_LOCATION_CONTACT_DETAILED, contactDetailed);
        } else {
            intent.putExtra(FIREBASE_LOCATION_CONTACT, contact);
        }
        startActivity(intent);
    }


    public void updateFieldsFromFirebase() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_DETAILED).child(contactKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ContactDetailed receivedContactDetailed = dataSnapshot.getValue(ContactDetailed.class);
                        if (receivedContactDetailed != null) {
                            contactDetailed = receivedContactDetailed;
                            populateMobiles();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }


}
