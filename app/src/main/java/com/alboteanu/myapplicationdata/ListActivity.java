package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.DateToReturn;
import com.alboteanu.myapplicationdata.viewholder.PostHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACTS_PHONES;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATES;

public class ListActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "ListActivity";
    private FirebaseRecyclerAdapter<Contact, PostHolder> firebaseRecyclerAdapter;
    Toolbar toolbar;
    private Menu menu;
    String[] allPhonesArray;
    String[] allEmailsArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListActivity.this, ContactEditorActivity.class));
            }
        });

    }

    private void populateRecyclerView() {
        RecyclerView mRecycler = (RecyclerView) findViewById(R.id.posts_list);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(false);  //era true
        mManager.setStackFromEnd(false);   //era true
        mRecycler.setLayoutManager(mManager);
        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT).orderByChild(FIREBASE_LOCATION_NAME);
//        Log.d(TAG, "postsQuery.toString() " + postsQuery.toString());

        final long CurrentTime = System.currentTimeMillis();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, PostHolder>(Contact.class, R.layout.contact,
                PostHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostHolder viewHolder, final Contact contact, final int position) {
                final DatabaseReference postRef = getRef(position);
                final String contactKey = postRef.getKey();
                viewHolder.bindToPost(contact);
                if (contact.date != 0 && (CurrentTime > contact.date)) {
                    viewHolder.ball.setVisibility(View.VISIBLE);
                    viewHolder.ball.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String[] phone = new  String[]{contact.phone};
                            Utils.composeSMS(phone, getApplication());
                        }
                    });
                }
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ListActivity.this, QuickContactActivity.class);
                        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_KEY, contactKey);
//                        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_NAME, contact.name);
//                        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_PHONE, contact.phone);
                        intent.putExtra(FIREBASE_LOCATION_CONTACT, contact);
                        startActivity(intent);
                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.setTitle(Utils.getSavedTitle(this));
        populateRecyclerView();
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
        getMenuInflater().inflate(R.menu.menu_list, menu);
        this.menu = menu;
        getExpired();
        getAllPhones();
        getAllEmails();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(ListActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_send_email_to_all) {
            Utils.composeEmail(this, allEmailsArray, "subject");
            return true;
        } else if (id == R.id.action_send_sms_to_all) {
            Utils.composeSMS(allPhonesArray, this);
            return true;
        } else if (id == R.id.action_sms_to_expired) {
            String[] expiredPhonesArray = phoneList.toArray(new String[phoneList.size()]);
            Utils.composeSMS(expiredPhonesArray, this);
            return true;
        }if (id == R.id.action_logout) {
            logOut();
            goToSignInActivity();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logOut() {
        // Firebase sign out
        mAuth.signOut();
    }

    private void goToSignInActivity() {
        Intent intent = new Intent(this, EmailPasswordActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void getAllPhones() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS_PHONES)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                        if (map == null)
                            return;
                        Collection<String> values = map.values();
                        if(!values.isEmpty()){
                            allPhonesArray = values.toArray(new String[0]);
                            menu.findItem(R.id.action_send_sms_to_all).setVisible(true);
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
                        if(!values.isEmpty()){
                            allEmailsArray = values.toArray(new String[0]);
                            menu.findItem(R.id.action_send_email_to_all).setVisible(true);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    List<String> phoneList = new ArrayList<>();
    public void getExpired() {
        final long currentTime = System.currentTimeMillis();
        Utils.getUserNode().child(FIREBASE_LOCATION_RETURN_DATES).orderByChild(FIREBASE_LOCATION_RETURN_DATE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                DateToReturn dateToReturn = dataSnapshot.getValue(DateToReturn.class);
                if (dateToReturn != null) {
                    Log.d("tag", String.valueOf(dateToReturn.date));
                    if(dateToReturn.date != 0 && currentTime > dateToReturn.date){
                        phoneList.add(dateToReturn.phone);
                        menu.findItem(R.id.action_sms_to_expired).setTitle(String.valueOf(phoneList.size())).setVisible(true);
                    }

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

}
