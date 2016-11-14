package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.ContactDet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_DETAILED;
import static com.alboteanu.myapplicationdata.ContactEditorActivity.EXTRA_CONTACT_KEY;

public class QuickContactActivity extends BaseActivity {
    String contactKey;
    String contactName;
    String contactPhone;
    long returnDate;
    TextView name, nameF, phone, phoneF, email, emailF, other1, other1F, returnD, returnF;
    CheckBox checkBox_6M;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        contactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        initViews();
        populateALL();
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
        checkBox_6M = (CheckBox) findViewById(R.id.checkBox6Luni);
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
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_KEY, contactKey);
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_NAME, contactName);
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_PHONE, contactPhone);
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_RETURN_DATE, returnDate);
        startActivity(intent);
    }


    public void populateALL() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_DETAILED).child(contactKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ContactDet contactDet = dataSnapshot.getValue(ContactDet.class);
                        if (contactDet != null) {
                            nameF.setText(contactDet.namef);
                            name.setText(contactDet.name);
                            phoneF.setText(contactDet.phonef); //phonef
                            phone.setText(contactDet.phone);
                            emailF.setText(contactDet.emailf);
                            email.setText(contactDet.email);
                            other1F.setText(contactDet.others1f);
                            other1.setText(contactDet.others1);
                            returnDate = contactDet.date;
                            if(returnDate != 0){
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(returnDate);
                                Date data = calendar.getTime();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
                                String dateString = simpleDateFormat.format(data);
                                returnD.setText(dateString);
                            }
                            contactName = contactDet.name;
                            contactPhone = contactDet.phone;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }


}
