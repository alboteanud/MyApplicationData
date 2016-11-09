package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.ReturnDate;
import com.alboteanu.myapplicationdata.viewholder.PostHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class ListActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "ListActivity";
    private FirebaseRecyclerAdapter<Contact, PostHolder> firebaseRecyclerAdapter;
    Toolbar toolbar;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
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
        Query postsQuery = Utils.getUserNode().child(getString(R.string.contact_node)).orderByChild(getString(R.string.name));
//        Log.d(TAG, "postsQuery.toString() " + postsQuery.toString());
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, PostHolder>(Contact.class, R.layout.contact,
                PostHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostHolder viewHolder, final Contact contact, final int position) {
                final DatabaseReference postRef = getRef(position);
                Log.d(TAG, "position = " + position);
                // Set click listener for the whole contact view
                final String postKey = postRef.getKey();
                viewHolder.bindToPost(contact);
                if (contact.date != 0 && (System.currentTimeMillis() > contact.date)) {
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
                        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_KEY, postKey);
                        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_NAME, contact.name);
                        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_PHONE, contact.phone);
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
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
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
        expiredPhoneList = getKeysForExpiredDates();
        Log.d("nr expirate", String.valueOf(expiredPhoneList.size()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(ListActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_send_email_to_all) {
            Utils.getUserNode().child(getString(R.string.posts_emails))
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                            if (map == null) {
                                Toast.makeText(ListActivity.this, "No emails registered", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Collection<String> values = map.values();
                            String[] emails = values.toArray(new String[0]);
                            composeEmail(emails, "subject");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            return true;
        } else if (id == R.id.action_send_sms_to_all) {
            Utils.getUserNode().child(getString(R.string.posts_phones))
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                            if (map == null) {
                                Toast.makeText(ListActivity.this, "No phone registered", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Collection<String> values = map.values();
                            String[] phonesArray = values.toArray(new String[0]);
                            Utils.composeSMS(phonesArray, getApplication());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            return true;
        } else if (id == R.id.action_notifications) {
            Utils.composeSMS(expiredPhoneList.toArray(new String[expiredPhoneList.size()]), this);
        }

        return super.onOptionsItemSelected(item);
    }


    public void composeEmail(String[] addresses, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
//        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }


    List<String> expiredPhoneList = new ArrayList<>();
    public List<String> getKeysForExpiredDates() {
        Utils.getUserNode().child(getString(R.string.return_date_node)).orderByChild("date").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ReturnDate returnDate = dataSnapshot.getValue(ReturnDate.class);
                if (returnDate != null) {
                    Log.d("onChildAded", "date: " + returnDate.date);
                    if(System.currentTimeMillis() > returnDate.date){
                        expiredPhoneList.add(returnDate.phone);
                    }
                }
                if(expiredPhoneList.size()!=0){
                    menu.findItem(R.id.action_notifications).setTitle(String.valueOf(expiredPhoneList.size())).setVisible(true);
                }else{
                    menu.findItem(R.id.action_notifications).setTitle("0").setVisible(false);
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

        return expiredPhoneList;

    }


    public void onBallClick(View view) {
    }
}
