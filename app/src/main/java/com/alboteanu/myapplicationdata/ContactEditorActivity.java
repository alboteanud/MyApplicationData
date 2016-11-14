package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.ContactDetailed;
import com.alboteanu.myapplicationdata.models.DateToReturn;
import com.alboteanu.myapplicationdata.models.FixedFields;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACTS_PHONES;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_DETAILED;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_FIXED;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATES;


public class ContactEditorActivity extends BaseActivity {
    public String contactKey;
    long returnDate;
    CheckBox checkBox_6M;
    private static final String REQUIRED = "Required";
    private static final String INVALID_EMAIL = "Invalid email";
    public static final String EXTRA_CONTACT_KEY = "key";
    EditText name, nameF, phone, phoneF, email, emailF, other1, other1F, returnF;
    TextView returnD;
    ContactDetailed contactDetailed;
    Contact contact;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        setListeners();
        contactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT)) {
            contact = (Contact) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT);
            populateMobilesSimple();
            updateFieldsFromFirebase();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);            //hide keyboard

        }
        else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_DETAILED)) {
            contactDetailed = (ContactDetailed) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_DETAILED);
            populateMobiles();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);            //hide keyboard
        }                            // new Contact
        populateFixed();
        name.requestFocus();
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

    private void populateMobiles() {
        name.setText(contactDetailed.name);
        phone.setText(contactDetailed.phone);
        email.setText(contactDetailed.email);
        other1.setText(contactDetailed.others1);
        returnDate = contactDetailed.date;
        if(returnDate != 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(returnDate);
            Date data = calendar.getTime();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
            String dateString = simpleDateFormat.format(data);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        if (contactKey == null) {
            menu.findItem(R.id.action_delete_contact).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_contact) {
            createDeleteDialogAlert(contactKey);
            return true;
        }
        if (id == R.id.action_save) {
            if (saveAndSend()) {
                goToQuickContactActivity();
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initViews() {
        name = ((EditText) findViewById(R.id.name));
        nameF = ((EditText) findViewById(R.id.nameF));
        phone = ((EditText) findViewById(R.id.phone));
        phoneF = ((EditText) findViewById(R.id.phoneF));
        email = ((EditText) findViewById(R.id.email));
        emailF = ((EditText) findViewById(R.id.emailF));
        other1 = ((EditText) findViewById(R.id.other1));
        other1F = ((EditText) findViewById(R.id.other1F));
        returnF = ((EditText) findViewById(R.id.returnF));
        returnD = ((TextView) findViewById(R.id.returnD));
        checkBox_6M = (CheckBox) findViewById(R.id.checkBox6Luni);
    }

    private void setListeners() {
        checkBox_6M.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    Date data = calendar.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
                    String dateString = simpleDateFormat.format(data);
                    returnD.setText(dateString);
                    returnDate = calendar.getTimeInMillis();
                } else {
                    returnD.setText("");
                    returnD.setHint(getString(R.string.pick_date_hint));
                    returnDate = 0;
                }
            }
        });

        returnD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }


    private void goToQuickContactActivity() {
        Intent intent = new Intent(this, QuickContactActivity.class);
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_KEY, contactKey);
        if (contactDetailed != null){
            intent.putExtra(FIREBASE_LOCATION_CONTACT_DETAILED, contactDetailed);
        } else {
            intent.putExtra(FIREBASE_LOCATION_CONTACT, contact);
        }
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private boolean saveAndSend() {
        final String nameFS = nameF.getText().toString();
        final String nameS = name.getText().toString();
        final String phoneFS = phoneF.getText().toString();
        final String phoneS = phone.getText().toString();
        final String emailFS = emailF.getText().toString();
        final String emailS = email.getText().toString();
        final String other1FS = other1F.getText().toString();
        final String other1S = other1.getText().toString();
        final String returnFS = returnF.getText().toString();

        if (nameS.isEmpty()) {
            name.setError(REQUIRED);
            return false;
        }

        if (phoneS.isEmpty()) {
            phone.setError(REQUIRED);
            return false;
        }

        if (!emailS.isEmpty() && !Utils.isValidEmail(emailS)) {
                email.setError(INVALID_EMAIL);
                return false;
            }

        if (contactKey == null)
            contactKey = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT).push().getKey();  //generate new key

        Map<String, Object> updates = new HashMap<>();

        Contact contact = new Contact(nameS, phoneS, returnDate);
        FixedFields fixedFields = new FixedFields(nameFS, phoneFS, emailFS, other1FS, returnFS);
        contactDetailed = new ContactDetailed(nameS, phoneS, emailS, other1S, returnDate);

        Map<String, Object> contactMap = contact.toMap();
        Map<String, Object> contactDetMap = contactDetailed.toMap();
        Map<String, Object> fixedMap = fixedFields.toMap();
        updates.put(FIREBASE_LOCATION_CONTACT + "/" + contactKey, contactMap);
        updates.put(FIREBASE_LOCATION_CONTACT_FIXED, fixedMap);
        updates.put(FIREBASE_LOCATION_CONTACT_DETAILED + "/" + contactKey, contactDetMap);
        updates.put(FIREBASE_LOCATION_CONTACTS_PHONES + "/" + contactKey, phoneS);
        updates.put(FIREBASE_LOCATION_EMAIL + "/" + contactKey, emailS);
        DateToReturn dateToReturn = new DateToReturn(this.returnDate, phoneS);  //data si tel
        Map<String, Object> returnMap = dateToReturn.toMap();
        updates.put(FIREBASE_LOCATION_RETURN_DATES + "/" + contactKey, returnMap);
        Utils.getUserNode().updateChildren(updates);
        return true;
    }


}
