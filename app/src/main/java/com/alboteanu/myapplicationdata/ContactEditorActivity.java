package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.models.ContactShort;
import com.alboteanu.myapplicationdata.models.ContactLong;
import com.alboteanu.myapplicationdata.models.DateToReturn;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACTS_PHONES;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATES;


public class ContactEditorActivity extends BaseDetailsActivity implements View.OnClickListener{
    CheckBox checkBox_6M;
    EditText nameText, phoneText, emailText, otherText;
    TextView returnText;
    ContactShort contactShort;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        setListeners();
        if(contactKey == null){  //            creating new contactLong

        } else if (contactLong == null  ) {  // probably no internet. Short populating
            getContactFromFirebaseAndUpdateUI();
        }
        updateUI();
    }


    public void updateUI(){
        if(contactLong != null) {
            nameText.setText(contactLong.name);
            phoneText.setText(contactLong.phone);
            emailText.setText(contactLong.email);
            otherText.setText(contactLong.other);
            date = contactLong.date;
            if(date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date);
                returnText.setText(Utils.calendarToString(calendar));
            }
        }else if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT_S)) {
            ContactShort contactShort = (ContactShort) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT_S);
            nameText.setText(contactShort.name);
            phoneText.setText(contactShort.phone);
            date = contactShort.date;
            if(date != 0) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(date);
                String dateString = Utils.calendarToString(calendar);
                returnText.setText(dateString);
            }
        }
        nameText.requestFocus();
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
        nameText = ((EditText) findViewById(R.id.name));
        phoneText = ((EditText) findViewById(R.id.phone));
        emailText = ((EditText) findViewById(R.id.email));
        otherText = ((EditText) findViewById(R.id.other));
        returnText = ((TextView) findViewById(R.id.return_date_textView));
        checkBox_6M = (CheckBox) findViewById(R.id.checkBox6Luni);
    }

    private void setListeners() {
        checkBox_6M.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    returnText.setText(Utils.calendarToString(calendar));
                    date = calendar.getTimeInMillis();
                } else {
                    returnText.setText("");
                    returnText.setHint("--");
                    date = 0;
                }
            }
        });
    }


    private void goToQuickContactActivity() {
        Intent intent = new Intent(this, QuickContactActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, contactKey);
        if (contactLong != null){
            intent.putExtra(FIREBASE_LOCATION_CONTACT, contactLong);
        } else {
            intent.putExtra(FIREBASE_LOCATION_CONTACT_S, contactShort);
        }
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    private boolean saveAndSend() {
        final String nameS = nameText.getText().toString();
        final String phoneS = phoneText.getText().toString();
        final String email = emailText.getText().toString();
        final String other1S = otherText.getText().toString();

        if (nameS.isEmpty()) {
            nameText.setError(getString(R.string.required));
            return false;
        }

        if (!email.isEmpty() && !Utils.isValidEmail(email)) {
                emailText.setError(getString(R.string.invalid_email));
                return false;
            }

        if (contactKey == null)
            contactKey = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_S).push().getKey();  //generate new key

        Map<String, Object> updates = new HashMap<>();

        ContactShort contactShort = new ContactShort(nameS, phoneS, date);
        contactLong = new ContactLong(nameS, phoneS, email, other1S, date);

        Map<String, Object> contactMap = contactShort.toMap();
        Map<String, Object> contactDetMap = contactLong.toMap();
        updates.put(FIREBASE_LOCATION_CONTACT_S + "/" + contactKey, contactMap);
        updates.put(FIREBASE_LOCATION_CONTACT + "/" + contactKey, contactDetMap);
        updates.put(FIREBASE_LOCATION_CONTACTS_PHONES + "/" + contactKey, phoneS);
        updates.put(FIREBASE_LOCATION_EMAIL + "/" + contactKey, email);
        DateToReturn dateToReturn = new DateToReturn(this.date, phoneS);  //data si tel
        Map<String, Object> returnMap = dateToReturn.toMap();
        updates.put(FIREBASE_LOCATION_RETURN_DATES + "/" + contactKey, returnMap);
        Utils.getUserNode().updateChildren(updates);
        return true;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ic_action_date_return:
                showDatePickerDialog();
                break;
            case R.id.return_date_textView:
                showDatePickerDialog();
                break;
        }
    }
}
