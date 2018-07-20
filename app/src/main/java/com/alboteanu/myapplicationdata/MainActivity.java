package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.ContactHolder;
import com.alboteanu.myapplicationdata.models.User;
import com.alboteanu.myapplicationdata.others.Constants;
import com.alboteanu.myapplicationdata.others.MyDragShadowBuilder;
import com.alboteanu.myapplicationdata.others.MyLayoutManager;
import com.alboteanu.myapplicationdata.others.SettingsActivity;
import com.alboteanu.myapplicationdata.others.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.alboteanu.myapplicationdata.others.Constants.ACTION_CONTACT_DELETED;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;

public class MainActivity extends BaseActivity {
    private static final String KEY_SAVED_CONTACTS = "saved_contacts";
    private Map<String, Contact> selected;
    private FirebaseRecyclerAdapter<Contact, ContactHolder> firebaseRecyclerAdapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        if (savedInstanceState != null) {
            selected = (LinkedHashMap<String, Contact>) savedInstanceState.getSerializable(KEY_SAVED_CONTACTS);
        }
        populateRecyclerView();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        updateUser(user);

    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra(ACTION_CONTACT_DELETED)) {
            String key = intent.getStringExtra(ACTION_CONTACT_DELETED);
            selected.remove(key);
            intent.removeExtra(ACTION_CONTACT_DELETED);
        }
    }

    private void populateRecyclerView() {
        Query query = getMainNode().child(FIREBASE_LOCATION_CONTACTS).orderByChild(FIREBASE_LOCATION_NAME);

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(query, Contact.class)
                        .build();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(options) {


            @NonNull
            @Override
            public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                // Create a new instance of the ViewHolder, in this case we are using a custom
                // layout called R.layout.contact_view for each item
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.contact_view, parent, false);

                return new ContactHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ContactHolder contactHolder, int position, @NonNull Contact contact) {
                // Bind the Contact object to the ContactHolder
                final String key = getRef(position).getKey();
                if (selected == null)
                    selected = new LinkedHashMap<>();
                contactHolder.checkBox.setChecked(selected.containsKey(key));
                View.OnClickListener onClickListener = new View.OnClickListener() {

                    @Override
                    public void onClick(@NonNull View view) {
                        if (view.getId() == R.id.checkBoxSelect) {
                            if (((CheckBox) view).isChecked())
                                addToSelected(key);
                            else {
                                selected.remove(key);
                            }
                        } else   // R.id.contact_view || R.id.icon_sandglass
                            startActivity(new Intent(MainActivity.this, DetailsActivity.class).putExtra(EXTRA_CONTACT_KEY, key));
                    }
                };
                if (contact.date > 0 && (System.currentTimeMillis() > contact.date)) {
                    prepareIcons(contactHolder, key, onClickListener);
                } else
                    contactHolder.sandglass.setVisibility(View.GONE);
                contactHolder.itemView.setOnClickListener(onClickListener);
                contactHolder.checkBox.setOnClickListener(onClickListener);
                contactHolder.bindContact(contact);
            }


        };

        RecyclerView recyclerView = findViewById(R.id.contact_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new MyLayoutManager(this));
        recyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void addAllToSelected() {
        getMainNode().child(FIREBASE_LOCATION_CONTACTS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Contact contact = snapshot.getValue(Contact.class);
                            if (selected == null)
                                selected = new LinkedHashMap<>();
                            selected.put(snapshot.getKey(), contact);
                        }
                        firebaseRecyclerAdapter.notifyDataSetChanged();
                        menu.findItem(R.id.action_select_none).setVisible(true);
                        menu.findItem(R.id.action_select_all).setVisible(false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        // nu vor fi toate cheile in lista
    }

    private void addToSelected(@NonNull final String key) {
        getMainNode().child(FIREBASE_LOCATION_CONTACTS).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Contact contact = dataSnapshot.getValue(Contact.class);
                        if (selected == null)
                            selected = new LinkedHashMap<>();
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
                                    getMainNode().child(FIREBASE_LOCATION_CONTACTS + "/"
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

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        this.menu = menu;
        return true;
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
                    if (!phonesList.isEmpty())
                        Utils.composeSMS(phonesList.toArray(new String[0]), this);
                }
                break;
            case R.id.action_select_all:
                addAllToSelected();
                break;
            case R.id.action_select_none:
                selected.clear();
                firebaseRecyclerAdapter.notifyDataSetChanged();
                menu.findItem(R.id.action_select_none).setVisible(false);
                menu.findItem(R.id.action_select_all).setVisible(true);
                break;
            case R.id.action_logout:
                logOutAndGoToLoginPage();
                break;
//            case R.id.action_test:
//                causeCrash();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logOutAndGoToLoginPage() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient;
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(KEY_SAVED_CONTACTS, (Serializable) selected);
//        outState.putParcelable(KEY_LIST_STATE, layoutManager.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter.stopListening();
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null) {
            logOutAndGoToLoginPage();
        }
    }

    void updateUser(final FirebaseUser firebaseUser) {
        getMainNode().child(Constants.FIREBASE_USER).child(Constants.FIREBASE_LOCATION_EMAIL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                String labelVisit = "visited";
                if (email == null) {
                    labelVisit = "created";
                    pushFakeContacts();
                }
                Map<String, Object> userMap = new User(firebaseUser).toMap(labelVisit);
                getMainNode().child(Constants.FIREBASE_USER).updateChildren(userMap);
            }

            private void pushFakeContacts() {
                long now = System.currentTimeMillis();
                Contact contact3 = new Contact("Christy (dummy contact)", "0664 217 01 21", "ChristyCloosterman@rhyta.com", "remember to congratulate on her birthday", (now + 86400000 * 15L));
                Contact contact1 = new Contact("Jeffery Weiss (dummy contact)", "0734 857 2075", "JefferyEWeiss@rhyta.com", "invite to dinner on Friday evening", now - 86400000 * 9L);
                Contact contact2 = new Contact("Nicole Pinto (dummy contact)", "2523 452 45 32", "NicolePintoPereira@jourrapide.com", "should come back for revision", now + 86400000 * 11L);

                Map<String, Object> mapContact1 = contact1.toMap();
                Map<String, Object> mapContact2 = contact2.toMap();
                Map<String, Object> mapContact3 = contact3.toMap();

                String key1 = getMainNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();  //generate new key
                String key2 = getMainNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();
                String key3 = getMainNode().child(FIREBASE_LOCATION_CONTACTS).push().getKey();

                Map<String, Object> updates = new HashMap<>();
                updates.put(FIREBASE_LOCATION_CONTACTS + "/" + key1, mapContact1);
                updates.put(FIREBASE_LOCATION_CONTACTS + "/" + key2, mapContact2);
                updates.put(FIREBASE_LOCATION_CONTACTS + "/" + key3, mapContact3);

                getMainNode().updateChildren(updates);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void causeCrash() {
        throw new NullPointerException("Fake null pointer exception");
    }

}
