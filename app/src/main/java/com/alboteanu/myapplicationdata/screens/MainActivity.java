package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
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

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.others.ContactHolder;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.login.SignInActivity;
import com.alboteanu.myapplicationdata.models.Contact;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Calendar;

import static com.alboteanu.myapplicationdata.R.layout.contact;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Utils.getUid;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private FirebaseRecyclerAdapter<Contact, ContactHolder> firebaseRecyclerAdapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

    }

    private void populateRecyclerView() {
        RecyclerView mRecycler = (RecyclerView) findViewById(R.id.contact_list);
        mRecycler.setHasFixedSize(true);
        LinearLayoutManager mManager = new LinearLayoutManager(this);
        mRecycler.setLayoutManager(mManager);
        Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_S).orderByChild(FIREBASE_LOCATION_NAME);

        final Calendar calendarNow = Calendar.getInstance();
        final Calendar calendarReturn = Calendar.getInstance();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(Contact.class, contact,
                ContactHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final ContactHolder contactHolder, final Contact contact, final int position) {
                Log.d("tag", " contact " + contact.name);
                if (contact.retur.containsKey(getUid())) {
                    calendarReturn.setTimeInMillis(contact.retur.get(getUid()));
                    if (calendarNow.after(calendarReturn))
                        contactHolder.clepsidra.setVisibility(View.VISIBLE);
                }

                contactHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final DatabaseReference postRef = getRef(position);
                        Intent intent = new Intent(MainActivity.this, QuickContactActivity.class);
                        intent.putExtra(EXTRA_CONTACT_KEY, postRef.getKey());
                        startActivity(intent);
                    }
                });
                contactHolder.letterIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contactHolder.letterIcon.setVisibility(View.GONE);
                        contactHolder.checkBox.setVisibility(View.VISIBLE);
                        contactHolder.checkBox.setChecked(true);
                    }
                });
                contactHolder.bindContact(contact, getDrawable(R.drawable.shape_oval));
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
//        this.menu = menu;
//        getExpired();
//        getAllPhones();
//        getAllEmails();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            mAuth.signOut();
            goToSignInActivity();
            return true;
        } else if (id == R.id.action_email) {

            return true;
        }
            else if (id == R.id.action_sms) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setClepsidra(){

    }

    @Override
    protected void onPause() {
        super.onPause();
        Toast.makeText(this, "onPause() a fost apelata", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.fab){
            startActivity(new Intent(MainActivity.this, EditNewContactActivity.class));
        }
    }
}
