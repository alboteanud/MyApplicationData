package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.login.GoogleLoginActivity;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.ContactHolder;
import com.alboteanu.myapplicationdata.others.MyDragShadowBuilder;
import com.alboteanu.myapplicationdata.others.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.screens.BaseDetailsActivity.ACTION_CONTACT_DELETED;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String RECYCLER_STATE = "recycler_state";
    private static final String SAVED_SELECTED_CONTACTS = "saved_contacts";
    private HashMap<String, Contact> selected = new HashMap<>();
    private FirebaseRecyclerAdapter<Contact, ContactHolder> recyclerAdapter;
    private LinearLayoutManager mManager;
    private Bundle instanceState;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        mManager = new LinearLayoutManager(this);
        populateRecyclerView();

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceStates");
        instanceState = savedInstanceState;
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()  " + intent.toString());
        if (intent.hasExtra(ACTION_CONTACT_DELETED)) {
            String key = intent.getStringExtra(ACTION_CONTACT_DELETED);
            selected.remove(key);
            intent.removeExtra(ACTION_CONTACT_DELETED);
        }
//        setIntent(intent);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.action_email:
                List<String> emailsList = new ArrayList<>();
                for (Contact contact : selected.values()) {
                    if (contact != null && contact.email != null)
                        emailsList.add(contact.email);
                }
                if (!emailsList.isEmpty())
                    Utils.composeEmail(this, emailsList.toArray(new String[0]));
                break;
            case R.id.action_sms:
                List<String> phonesList = new ArrayList<>();
                for (Contact contactPhoneEmail : selected.values()) {
                    if (contactPhoneEmail != null && contactPhoneEmail.phone != null)
                        phonesList.add(contactPhoneEmail.phone);
                }
                if (!phonesList.isEmpty())
                    Utils.composeSMS(phonesList.toArray(new String[0]), this);
                break;
            case R.id.action_select_all:
                addAllContactsToMapSelected();
                break;
            case R.id.action_select_none:
                selected.clear();
                recyclerAdapter.notifyDataSetChanged();
                menu.findItem(R.id.action_select_none).setVisible(false);
                menu.findItem(R.id.action_select_all).setVisible(true);
                break;
            case R.id.action_logout:
                mAuth.signOut();
                startActivity(new Intent(this, GoogleLoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateRecyclerView() {
        Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).orderByChild(FIREBASE_LOCATION_NAME);

        recyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(Contact.class, R.layout.contact_view,
                ContactHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(@NonNull ContactHolder contactHolder,
                                              @NonNull Contact contact, int position) {
                final DatabaseReference postRef = getRef(position);
                final String key = postRef.getKey();
                contactHolder.checkBox.setChecked(selected.containsKey(key));
                View.OnClickListener onClickListener = new View.OnClickListener() {

                    @Override
                    public void onClick(@NonNull View view) {
                        if (view.getId() == R.id.checkBoxSelect) {
                            if (((CheckBox) view).isChecked())
                                addContactToMapSelected(key);
                            else {
                                selected.remove(key);
                            }
                        } else   // R.id.contact_view || R.id.icon_sandglass
                            startActivity(new Intent(MainActivity.this, QuickContactActivity.class).putExtra(EXTRA_CONTACT_KEY, key));
                    }
                };
                if (contact.date > 0 && (System.currentTimeMillis() > contact.date)) {
                    prepareIcons(contactHolder, key, onClickListener);
                }
                else
                    contactHolder.sandglass.setVisibility(View.GONE);
                contactHolder.itemView.setOnClickListener(onClickListener);
                contactHolder.checkBox.setOnClickListener(onClickListener);
                contactHolder.bindContact(contact);
            }
        };
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.contact_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void addAllContactsToMapSelected() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Contact contact = snapshot.getValue(Contact.class);
                            selected.put(snapshot.getKey(), contact);
                        }
                        recyclerAdapter.notifyDataSetChanged();
                        menu.findItem(R.id.action_select_none).setVisible(true);
                        menu.findItem(R.id.action_select_all).setVisible(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        // nu vor fi toate cheile in lista
    }

    private void addContactToMapSelected(@NonNull final String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Contact contact = dataSnapshot.getValue(Contact.class);
                        selected.put(key, contact);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        //lista va fi completa incl cu cei ce nu au nr de tel
    }

    private void prepareIcons(@NonNull final ContactHolder contactHolder, final String key, View.OnClickListener onClickListener) {
        contactHolder.sandglass.setVisibility(View.VISIBLE);
        contactHolder.sandglass.setOnClickListener(onClickListener);
        contactHolder.sandglass.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(@NonNull View sandGlass) {
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(sandGlass);
                View.OnDragListener onDragListener = new View.OnDragListener() {
                    @Override
                    public boolean onDrag(@NonNull View view, @NonNull DragEvent dragEvent) {
                        switch (dragEvent.getAction()) {
                            case DragEvent.ACTION_DRAG_STARTED:
                                if (view.getId() == R.id.icon_sandglass)
                                    ((ImageView) view).setColorFilter(Color.LTGRAY);
                                    view.invalidate();
                                return true;

                            case DragEvent.ACTION_DRAG_ENTERED:
                                if (view.getId() == R.id.icon_bin)
                                    view.setBackgroundColor(Color.LTGRAY);
                                view.invalidate();
                                return true;

                            case DragEvent.ACTION_DRAG_EXITED:  //out of the box
                                if (view.getId() == R.id.icon_sandglass) {
                                    ((ImageView) view).setColorFilter(Color.LTGRAY);
                                } else if (view.getId() == R.id.icon_bin)
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                view.invalidate();
                                return true;

                            case DragEvent.ACTION_DROP:
                                if (view.getId() == R.id.icon_bin) {
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                    Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS + "/"
                                            + key + "/" + FIREBASE_LOCATION_DATE).removeValue();
                                }
                                return true;

                            case DragEvent.ACTION_DRAG_ENDED:
                                if (view.getId() == R.id.icon_sandglass
                                        && !dragEvent.getResult()) {     // Delete unsuccessful
                                    // do nothing with the sandglass
                                } else
                                    view.setVisibility(View.GONE);
                                ((ImageView) view).clearColorFilter();
                                view.invalidate();
                                view.setOnDragListener(null);
                                // the value is ignored.
                                return true;

                            // An unknown action type was received.
                            default:
                                break;
                            // returns true to indicate that the View can accept the dragged data.
                        }

                        return false;
                    }
                };
                sandGlass.setOnDragListener(onDragListener);
                contactHolder.bin.setVisibility(View.VISIBLE);
                contactHolder.bin.setOnDragListener(onDragListener);

                // Starts the drag
                sandGlass.startDrag(null,  // the data to be dragged
                        myShadow,  // the drag shadow builder
                        null,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
                return false;
            }
        });
    }

    private void putInstanceStateToBundle() {
        if (instanceState == null)
            instanceState = new Bundle();
        instanceState.putSerializable(SAVED_SELECTED_CONTACTS, selected);
        final Parcelable state = mManager.onSaveInstanceState();
        instanceState.putParcelable(RECYCLER_STATE, state);
    }

    private void restoreListPosition() {
        Parcelable recyclerViewState = instanceState.getParcelable(RECYCLER_STATE);
        mManager.onRestoreInstanceState(recyclerViewState);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        this.menu = menu;
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (instanceState != null) {

            restoreListPosition();
        }
//        restoreListPosition();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        putInstanceStateToBundle();
        outState = instanceState;
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceStates");
    }

    @Override
    public void onStop() {
        super.onStop();
        putInstanceStateToBundle();
    }

    @Override
    protected void onDestroy() {
        if (recyclerAdapter != null) {
            recyclerAdapter.cleanup();
        }
        super.onDestroy();
    }

}
