package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.DetailedContact;
import com.alboteanu.myapplicationdata.models.ReturnDate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ContactEditorActivity extends BaseActivity {
    public String contactKey;
    long returnDate;
    CheckBox checkBox_6M;
    private static final String REQUIRED = "Required";
    private static final String INVALID_EMAIL = "Invalid email";
    public static final String EXTRA_CONTACT_KEY = "key";
    public static final String EXTRA_CONTACT_NAME = "name";
    public static final String EXTRA_CONTACT_PHONE = "phone";
    EditText[] editTexts = new EditText[12];
    TextView dateTextView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        contactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initViews();
        setListeners();
        if (contactKey != null) {
            linkVariableFieldsToFirebase();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);            //hide keyboard
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact_editor, menu);
        if(contactKey==null){
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
        } if (id == R.id.action_save) {
            if(submitPost()) {
                goToQuickContactActivity();
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    public void initViews() {
        for(int i = 0; i < editTexts.length; i++){
            int id = getResources().getIdentifier(String.valueOf("edit_text" + i),"id", this.getPackageName());
            editTexts[i] = (EditText) findViewById(id);
        }
        editTexts[1].setText(getIntent().getStringExtra(EXTRA_CONTACT_NAME));
        editTexts[3].setText(getIntent().getStringExtra(EXTRA_CONTACT_PHONE));
        dateTextView = ((TextView) findViewById(R.id.dateTextView));
        checkBox_6M = (CheckBox) findViewById(R.id.checkBox6Luni);
    }

    private void setListeners(){
        checkBox_6M.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, 6);
                    Date data = calendar.getTime();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
                    String dateString = simpleDateFormat.format(data);
                    dateTextView.setText(dateString);
                    returnDate = calendar.getTimeInMillis();
                } else {
                    dateTextView.setText("");
                    dateTextView.setHint(getString(R.string.pick_date_hint));
                    returnDate = 0;
                }
            }
        });

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
    }

    private boolean submitPost() {
        final String nameF = editTexts[0].getText().toString();
        final String name = editTexts[1].getText().toString();
        final String phoneF = editTexts[2].getText().toString();
        final String phone = editTexts[3].getText().toString();
        final String emailF = editTexts[4].getText().toString();
        final String email = editTexts[5].getText().toString();
        final String otherF = editTexts[6].getText().toString();
        final String other = editTexts[7].getText().toString();
        final String dateF = editTexts[8].getText().toString();

        if(name.isEmpty()){
            editTexts[1].setError(REQUIRED);
            return false;
        }
        if (contactKey == null)
            contactKey = Utils.getUserNode().child(getString(R.string.contact_node)).push().getKey();  //generate new key
        Map<String, Object> updates = new HashMap<>();

        if(!email.isEmpty()){
                   if (!Utils.isValidEmail(email)) {
                       editTexts[5].setError(INVALID_EMAIL);
                       return false;
                   }else {
                       updates.put(getString(R.string.posts_emails) + "/" + contactKey, email);
                   }
            }




        Contact contact = new Contact(name, phone, returnDate);
        DetailedContact detailedContact = new DetailedContact(name, phone, email, other, returnDate, nameF, phoneF, emailF, otherF, dateF);

        Map<String, Object> contactMap = contact.toMap();
        Map<String, Object> detailedContactMap = detailedContact.toMap();

        updates.put(getString(R.string.contact_node) + "/" + contactKey, contactMap);
        updates.put(getString(R.string.detailed_contact_node) + "/" + contactKey, detailedContactMap);
        if(!phone.isEmpty())
            updates.put(getString(R.string.posts_phones) + "/" + contactKey, phone);
        if(returnDate != 0){
            if (phone.isEmpty()) {
                editTexts[3].setError(REQUIRED);
                return false;
            }
            ReturnDate returnDate = new ReturnDate(this.returnDate, phone);  //data si tel
            Map<String, Object> returnMap = returnDate.toMap();
            updates.put(getString(R.string.return_date_node) + "/" + contactKey, returnMap);
        }

        Utils.getUserNode().updateChildren(updates);
        return true;
    }

    

    public void linkVariableFieldsToFirebase() {
        Utils.getUserNode().child(getString(R.string.detailed_contact_node)).child(contactKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DetailedContact detailedContact = dataSnapshot.getValue(DetailedContact.class);
                        if (detailedContact != null) {
                            editTexts[0].setText(detailedContact.nameF);
                            editTexts[1].setText(detailedContact.name);
                            editTexts[2].setText(detailedContact.phoneF);
                            editTexts[3].setText(detailedContact.phone);
                            editTexts[4].setText(detailedContact.emailF);
                            editTexts[5].setText(detailedContact.email);
                            editTexts[6].setText(detailedContact.others1F);
                            editTexts[7].setText(detailedContact.others1);
                            editTexts[1].setSelection(editTexts[1].length());
                            editTexts[1].requestFocus();
                            returnDate = detailedContact.date;
                            if(returnDate != 0){
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTimeInMillis(returnDate);
                                Date data = calendar.getTime();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(R.string.date_format));
                                String dateString = simpleDateFormat.format(data);
                                dateTextView.setText(dateString);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    private void goToQuickContactActivity() {
        Intent intent = new Intent(this, QuickContactActivity.class);
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_KEY, contactKey);
        startActivity(intent);
    }

}
