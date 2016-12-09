package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.others.ContactHolder;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.login.MainSignInActivity;
import com.alboteanu.myapplicationdata.models.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import static com.alboteanu.myapplicationdata.R.layout.contact_view;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAILS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAMES_DATES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN_RETUR;
import static com.alboteanu.myapplicationdata.others.Utils.getUid;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private FirebaseRecyclerAdapter<Contact, ContactHolder> firebaseRecyclerAdapter;
    Toolbar toolbar;
    private Menu menu;
    HashMap<String, String> phonesMap = new HashMap<>();
    HashMap<String, String> emailsMap = new HashMap<>();
    private GoogleApiClient client;
    ArrayList<String> selectedCheckBoxes = new ArrayList<>();
    private static final String FLAG_SELECT_ALL = "select_all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        if(savedInstanceState != null){
            selectedCheckBoxes = savedInstanceState.getStringArrayList("selectedCheckBoxes");
            if(selectedCheckBoxes.contains(FLAG_SELECT_ALL)){
                getAllPhones();
                getAllEmails();
            }else {
                ListIterator<String> iterator = selectedCheckBoxes.listIterator();
                while (iterator.hasNext()){
                    String key = iterator.next();
                    putPhoneToMap(key);
                    putEmailToMap(key);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArrayList("selectedCheckBoxes", selectedCheckBoxes);
        super.onSaveInstanceState(outState);
    }

    private void populateRecyclerView() {
        RecyclerView mRecycler = (RecyclerView) findViewById(R.id.contact_list);
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager mManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mManager);
        Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES).orderByChild(FIREBASE_LOCATION_NAME);
        final Calendar calendarNow = Calendar.getInstance();
        final Calendar calendarReturn = Calendar.getInstance();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(Contact.class, contact_view,
                ContactHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(ContactHolder contactHolder, Contact contact, int position) {
                final DatabaseReference postRef = getRef(position);
                final String key = postRef.getKey();
                if (contact.retur.containsKey(FIREBASE_LOCATION_RETURN_RETUR)) {
                    calendarReturn.setTimeInMillis(contact.retur.get(FIREBASE_LOCATION_RETURN_RETUR));
                    if (calendarNow.after(calendarReturn))
                        contactHolder.clepsidra.setVisibility(View.VISIBLE);
                }
                if(selectedCheckBoxes.contains(FLAG_SELECT_ALL) ){
                    if(!selectedCheckBoxes.contains(key))
                        contactHolder.checkBox.setChecked(true);
                }else if(selectedCheckBoxes.contains(key))
                    contactHolder.checkBox.setChecked(true);
                else
                    contactHolder.checkBox.setChecked(false);
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (view.getId()) {
                            case R.id.checkBoxSelectContact:
                                boolean checked = ((CheckBox) view).isChecked();
                                if (checked) {
                                    putPhoneToMap(key);
                                    putEmailToMap(key);
                                    if(selectedCheckBoxes.contains(FLAG_SELECT_ALL) )
                                        selectedCheckBoxes.remove(key);
                                    else
                                        selectedCheckBoxes.add(key);
                                } else {
                                    phonesMap.remove(key);
                                    emailsMap.remove(key);
                                    if(selectedCheckBoxes.contains(FLAG_SELECT_ALL))
                                        selectedCheckBoxes.add(key);
                                    else selectedCheckBoxes.remove(key);
                                }
                                break;
                            default:
                                Intent intent = new Intent(MainActivity.this, QuickContactActivity.class);
                                intent.putExtra(EXTRA_CONTACT_KEY, key);
                                startActivity(intent);
                        }
                    }
                };
                contactHolder.bindContact(contact, getDrawable(R.drawable.shape_oval), onClickListener);
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        populateRecyclerView();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }


    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle(Utils.getSavedTitle(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.cleanup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        this.menu = menu;
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_logout:
                mAuth.signOut();
                goToSignInActivity();
                break;
            case R.id.action_select_all:
                selectedCheckBoxes.clear();
                selectedCheckBoxes.add(FLAG_SELECT_ALL);
                firebaseRecyclerAdapter.notifyDataSetChanged();
                phonesMap.clear();
                emailsMap.clear();
                getAllPhones();
                getAllEmails();
                break;
            case R.id.action_select_none:
                selectedCheckBoxes.clear();
                phonesMap.clear();
                emailsMap.clear();
                firebaseRecyclerAdapter.notifyDataSetChanged();
                break;
            case R.id.action_email:
                String[] emails = emailsMap.values().toArray(new String[0]);
                if (emails.length > 0)
                    Utils.composeEmail(this, emails, "title");
                break;
            case R.id.action_sms:
                String[] phoneList = phonesMap.values().toArray(new String[0]);
                Log.d("tag", "length " + phoneList.length);
                if (phoneList.length > 0)
                    Utils.composeSMS(phoneList, this);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void clearReturnDate() {
    }

    private void goToSignInActivity() {
        Intent intent = new Intent(this, MainSignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab:
                startActivity(new Intent(MainActivity.this, EditActivity.class));
                break;
        }
    }


    private void putPhoneToMap(String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String phone = (String) dataSnapshot.getValue();
                        if (phone != null)
                            phonesMap.put(dataSnapshot.getKey(), phone);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void putEmailToMap(String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_EMAILS).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String email = (String) dataSnapshot.getValue();
                        if (email != null)
                            emailsMap.put(dataSnapshot.getKey(), email);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getAllPhones() {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        phonesMap = (HashMap<String, String>) dataSnapshot.getValue();

                        //remouving the unselected phones
                        ListIterator<String> iterator = selectedCheckBoxes.listIterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if(key != FLAG_SELECT_ALL){
                                phonesMap.remove(key);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getAllEmails() {
        Utils.getUserNode().child(FIREBASE_LOCATION_EMAILS)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        emailsMap = (HashMap<String, String>) dataSnapshot.getValue();

                        //remouving the unselected
                        ListIterator<String> iterator = selectedCheckBoxes.listIterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if(key != FLAG_SELECT_ALL){
                                emailsMap.remove(key);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
