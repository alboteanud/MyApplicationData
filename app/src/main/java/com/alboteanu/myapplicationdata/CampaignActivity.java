package com.alboteanu.myapplicationdata;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN_DATE;

public class CampaignActivity extends BaseActivity
        implements View.OnClickListener {
    List<String> phoneList = new ArrayList<>();
    String[] allPhonesArray;
    String[] allEmailsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_campaign);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getAllPhones() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Contact[] contacts = (Contact[]) dataSnapshot.getValue();
                        ArrayList<String> phoneList = new ArrayList<>();
                        if (contacts != null) {
                            for (int i = 0; i < contacts.length; i++) {
                                phoneList.add(contacts[1].phone);
                            }
                            allPhonesArray = phoneList.toArray(new String[0]);
//                            menu.findItem(R.id.action_send_sms_to_all).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getAllEmails() {
        Utils.getUserNode().child(FIREBASE_LOCATION_EMAIL)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map == null)
                            return;
                        Collection<String> values = map.values();
                        if (!values.isEmpty()) {
                            allEmailsArray = values.toArray(new String[0]);
//                            menu.findItem(R.id.action_send_email_to_all).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void getExpired() {
        final long currentTime = System.currentTimeMillis();
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).orderByChild(FIREBASE_LOCATION_RETURN_DATE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Contact dateToReturn = dataSnapshot.getValue(Contact.class);
                if (dateToReturn.date != 0 && currentTime > dateToReturn.date ){
                    phoneList.add(dateToReturn.phone);
//                    menu.findItem(R.id.action_sms_to_expired).setTitle(String.valueOf(phoneList.size())).setVisible(true);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_sms_to_expired:

                break;
            case R.id.button_sms_to_all:

                break;
             case R.id.button_email_to_expired:

                break;
            case R.id.button_emails_to_all:

                break;

        }
    }
}
