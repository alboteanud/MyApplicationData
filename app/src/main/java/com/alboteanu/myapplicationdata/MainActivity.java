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

import com.alboteanu.myapplicationdata.models.Post;
import com.alboteanu.myapplicationdata.models.PostReturn;
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

public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private FirebaseRecyclerAdapter<Post, PostHolder> firebaseRecyclerAdapter;
    Toolbar toolbar;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DetailActivity.class));
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
        final boolean detailedList = Utils.getListStateView(this);
        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = Utils.getUserNode().child(getString(R.string.posts_title)).orderByChild(getString(R.string.name));
//        Log.d(TAG, "postsQuery.toString() " + postsQuery.toString());
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(Post.class, R.layout.post_item,
                PostHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostHolder viewHolder, final Post post, final int position) {
                final DatabaseReference postRef = getRef(position);
                Log.d(TAG, "position = " + position);
                // Set click listener for the whole post view
                final String postKey = postRef.getKey();

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                if (detailedList)
                    viewHolder.bindToPost(post);
                else
                    viewHolder.bindToPostSimple(post);
                if (post.date != 0 && (System.currentTimeMillis() > post.date)) {
                    viewHolder.starView.setVisibility(View.VISIBLE);
                }
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch DetailActivity
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_POST_KEY, postKey);
                        intent.putExtra(DetailActivity.EXTRA_POST_TEXT4, post.text2);
                        intent.putExtra(DetailActivity.EXTRA_POST_TEXT4, post.text4);
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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        expiredPhoneList = getKeysForExpiredDates();
        Log.d("nr expirate", String.valueOf(expiredPhoneList.size()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_send_email_to_all) {
            Utils.getUserNode().child(getString(R.string.posts_emails))
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                            if (map == null) {
                                Toast.makeText(MainActivity.this, "No emails registered", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(MainActivity.this, "No phone registered", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Collection<String> values = map.values();
                            String[] phonesArray = values.toArray(new String[0]);
                            composeSMS(phonesArray);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

            return true;
        } else if (id == R.id.action_notifications) {
            composeSMS(expiredPhoneList.toArray(new String[expiredPhoneList.size()]));
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

    public void composeSMS(String[] phonesArray) {
        StringBuilder stringBuilder = new StringBuilder("smsto: ");
        for (int i = 0; i < phonesArray.length; i++) {
            stringBuilder.append(phonesArray[i]);
            stringBuilder.append(", ");
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(stringBuilder.toString())); // only sms apps should handle this
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    List<String> expiredPhoneList = new ArrayList<>();
    public List<String> getKeysForExpiredDates() {
        Utils.getUserNode().child(getString(R.string.post_return_date)).orderByChild("date").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                PostReturn postReturn = dataSnapshot.getValue(PostReturn.class);
                if (postReturn != null) {
                    Log.d("onChildAded", "date: " + postReturn.date);
                    if(System.currentTimeMillis() > postReturn.date){
                        expiredPhoneList.add(postReturn.phone);
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



}
