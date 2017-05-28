package com.alboteanu.myapplicationdata.screens;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.login.GoogleLoginActivity;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.models.ContactHolder;
import com.alboteanu.myapplicationdata.others.MyDragShadowBuilder;
import com.alboteanu.myapplicationdata.others.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAMES_DATES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONES_EMAILS;
import static com.alboteanu.myapplicationdata.screens.BaseDetailsActivity.ACTION_CONTACT_DELETED;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final String RECYCLER_STATE = "recycler_state";
    private static final String SAVED_SELECTED_CONTACTS = "saved_contacts";
    private HashMap<String, Contact> selectedContacts = new HashMap<>();
    private FirebaseRecyclerAdapter<Contact, ContactHolder> recyclerAdapter;
    private GoogleApiClient client;

    private LinearLayoutManager mManager;
    private RecyclerView recyclerView;
    private Bundle instanceState;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_UPDATE_LOCAL_CONTACTS)) {
            Utils.updateLocalDataBase();
        }
        mManager = new LinearLayoutManager(this);
        recyclerView = (RecyclerView) findViewById(R.id.contact_list);
        populateRecyclerView();

        // TODO
        // com.alboteanu.patientsListFree

        // ic_launcher_lite  in Manifest
        // google-services.json

    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
//        populateRecyclerView();
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceStates");
        instanceState = savedInstanceState;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent()  " + intent.toString());
        if (intent.hasExtra(ACTION_CONTACT_DELETED)) {
            String key = intent.getStringExtra(ACTION_CONTACT_DELETED);
            if (selectedContacts != null)
                selectedContacts.remove(key);
            intent.removeExtra(ACTION_CONTACT_DELETED);
        }
//        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (instanceState != null) {
            selectedContacts = (HashMap<String, Contact>) instanceState.getSerializable(SAVED_SELECTED_CONTACTS);
            restoreListPosition();
        }

    }

    // RUNNING APP

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
        Log.d("tag", "onStop");
        putInstanceStateToBundle();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    protected void onDestroy() {
        Log.d("tag MainAct", "onDestroy");
        if (recyclerAdapter != null) {
            recyclerAdapter.cleanup();
        }

        super.onDestroy();

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
//                finish();  // this enables new title check in onCreate
                break;

            case R.id.action_email:
                List<String> emailsList = new ArrayList<>();
                for (Contact contact : selectedContacts.values()) {
                    if (contact != null && contact.email != null)
                        emailsList.add(contact.email);
                }
                if (!emailsList.isEmpty())
                    Utils.composeEmail(this, emailsList.toArray(new String[0]));
                break;
            case R.id.action_sms:
                List<String> phonesList = new ArrayList<>();
                for (Contact contactPhoneEmail : selectedContacts.values()) {
                    if (contactPhoneEmail != null && contactPhoneEmail.phone != null)
                        phonesList.add(contactPhoneEmail.phone);
                }
                if (!phonesList.isEmpty())
                    Utils.composeSMS(phonesList.toArray(new String[0]), this);
                else {
                    Log.d("tag", "phonesList  empty");
                }
                break;
            case R.id.action_select_all:
                putAllContactsToMap();
//                recyclerAdapter.notifyDataSetChanged();
                break;
            case R.id.action_select_none:
                selectedContacts.clear();
                recyclerAdapter.notifyDataSetChanged();
                menu.findItem(R.id.action_select_none).setVisible(false);
                menu.findItem(R.id.action_select_all).setVisible(true);
                break;
            case R.id.action_logout:
                Utils.clearPreferences(MainActivity.this);
                mAuth.signOut();
                startActivity(new Intent(this, GoogleLoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
       /*     case R.id.action_set_alarm:
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 1);
                setAlarm();
                break;*/
         /*   case R.id.action_merge_with_google:
                googleSignIn();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }

/*    private void googleSignIn() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //atentie  initializati doar o data
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this  *//*FragmentActivity*//*, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } *//*OnConnectionFailedListener*//*)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        showProgressDialog();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
hideProgressDialog();
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
             mergeAccounts(account);
            } else {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(MainActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private void mergeAccounts(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
//                            updateUI(user);
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                            onAuthFail(task.getException().getMessage());
                        }

                    }
                });
    }

    //base function
    private void populateRecyclerView() {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mManager);
        Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES).orderByChild(FIREBASE_LOCATION_NAME);
        recyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(Contact.class, R.layout.contact_view,
                ContactHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(ContactHolder contactHolder,
                                              Contact contact_name_date, int position) {
                final DatabaseReference postRef = getRef(position);
                final String key = postRef.getKey();
                if (selectedContacts != null)
                    contactHolder.checkBox.setChecked(selectedContacts.containsKey(key));
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        if (view.getId() == R.id.checkBoxSelect) {
                            if (((CheckBox) view).isChecked())
                                addContactToSelectedList(key);
                            else
                                selectedContacts.remove(key);
                        } else   // R.id.contact_view || R.id.icon_sandglass
                            startActivity(new Intent(MainActivity.this, QuickContactActivity.class).putExtra(EXTRA_CONTACT_KEY, key));
                    }
                };
                if (contact_name_date.date > 0 && (System.currentTimeMillis() > contact_name_date.date)) {
                    prepareIcons(contactHolder, key, onClickListener);
                }
//                if (contact_name_date.return_date_millis > 0 &&  System.currentTimeMillis() > contact_name_date.return_date_millis)
//                    prepareIcons(contactHolder, key, onClickListener);
                else
                    contactHolder.sandglass.setVisibility(View.GONE);
                contactHolder.itemView.setOnClickListener(onClickListener);
                contactHolder.checkBox.setOnClickListener(onClickListener);
                contactHolder.bindContact(contact_name_date, getDrawable(R.drawable.shape_oval));
            }
        };
        recyclerView.setAdapter(recyclerAdapter);
    }

    private void putAllContactsToMap() {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES_EMAILS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("tag MainActivity", "onDataChange  putALLContactsToSelectedList");
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Contact contact = snapshot.getValue(Contact.class);
                            if (selectedContacts == null)
                                selectedContacts = new HashMap<>();
                            selectedContacts.put(snapshot.getKey(), contact);
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

    void addContactToSelectedList(final String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES_EMAILS).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Contact contact = dataSnapshot.getValue(Contact.class);
                        selectedContacts.put(key, contact);
                        final int size = selectedContacts.size();
                        Log.d(TAG, "selected size: " + size
                                + " \n" + selectedContacts.toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        //lista va fi completa incl cu cei ce nu au nr de tel
    }

    @NonNull
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName(getString(R.string.app_name)) // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("https://play.google.com/store/apps/details?id=com.alboteanu.patientsList"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    private void prepareIcons(final ContactHolder contactHolder, final String key, View.OnClickListener onClickListener) {
        contactHolder.sandglass.setVisibility(View.VISIBLE);
        Log.d(TAG, "sandglass VISIBLE prepareIcons key= " + key);
        contactHolder.sandglass.setOnClickListener(onClickListener);
        contactHolder.sandglass.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(@NonNull View sandGlass) {
                View.DragShadowBuilder myShadow = new MyDragShadowBuilder(sandGlass);
                View.OnDragListener onDragListener = new View.OnDragListener() {
                    @Override
                    public boolean onDrag(View view, DragEvent dragEvent) {
                        switch (dragEvent.getAction()) {
                            case DragEvent.ACTION_DRAG_STARTED:
                                if (view.getId() == R.id.icon_sandglass) {

                                    ((ImageView) view).setColorFilter(Color.LTGRAY);
                                    view.invalidate();
                                } else if (view.getId() == R.id.icon_bin) {

                                }
                                return true;

                            case DragEvent.ACTION_DRAG_ENTERED:
                                if (view.getId() == R.id.icon_sandglass) {
                                    if (Build.VERSION.SDK_INT >= M)
                                        ((ImageView) view).setColorFilter(getColor(R.color.colorPrimary));
                                    else
                                        ((ImageView) view).setColorFilter(getResources().getColor(R.color.colorPrimary));
                                } else if (view.getId() == R.id.icon_bin) {
                                    view.setBackgroundColor(Color.LTGRAY);
                                    ((ImageView) view).setColorFilter(Color.RED);
                                }
                                view.invalidate();
                                return true;

                            case DragEvent.ACTION_DRAG_EXITED:  //out of the box
                                if (view.getId() == R.id.icon_sandglass) {
                                    ((ImageView) view).setColorFilter(Color.LTGRAY);
                                } else if (view.getId() == R.id.icon_bin) {
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                    if (Build.VERSION.SDK_INT >= M)
                                        ((ImageView) view).setColorFilter(getColor(R.color.colorPrimary));
                                    else
                                        ((ImageView) view).setColorFilter(getResources().getColor(R.color.colorPrimary));
                                }
                                view.invalidate();
                                return true;

                            case DragEvent.ACTION_DROP:
                                if (view.getId() == R.id.icon_sandglass) {

                                } else if (view.getId() == R.id.icon_bin) {
                                    view.setBackgroundColor(Color.TRANSPARENT);
                                    //delete
                                    Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES + "/"
                                            + key + "/" + FIREBASE_LOCATION_DATE).removeValue();
                                }
                                return true;

                            case DragEvent.ACTION_DRAG_ENDED:
                                if (view.getId() == R.id.icon_sandglass) {
                                    if (dragEvent.getResult()) {     // Delete successful
                                        view.setVisibility(View.GONE);
                                    }
                                } else if (view.getId() == R.id.icon_bin) {
                                    view.setVisibility(View.GONE);
                                }
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
                Log.d(TAG, "bin VISIBLE  key= " + key);
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
        instanceState.putSerializable(SAVED_SELECTED_CONTACTS, selectedContacts);
        final Parcelable state = mManager.onSaveInstanceState();
        instanceState.putParcelable(RECYCLER_STATE, state);
    }

    private void restoreListPosition() {
        Parcelable recyclerViewState = instanceState.getParcelable(RECYCLER_STATE);
        mManager.onRestoreInstanceState(recyclerViewState);

//        mManager.onLayoutCompleted();
//        Object state = new RecyclerView.State();
//        state.put(R.id.recycler_view_state_key, recyclerViewState.);
    }

    public void setAlarm() {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent _) {
                try {
                    SmsManager.getDefault().sendTextMessage("0773360279", null, "mesaj de test " + String.valueOf(SystemClock.elapsedRealtime()), null, null);
                } catch (Exception e) {

                }
                context.unregisterReceiver(this); // this == BroadcastReceiver, not Activity
            }
        };

        this.registerReceiver(receiver, new IntentFilter("com.blah.blah.somemessage"));

        PendingIntent pintent = PendingIntent.getBroadcast(this, 0, new Intent("com.blah.blah.somemessage"), 0);
        AlarmManager manager = (AlarmManager) (getSystemService(Context.ALARM_SERVICE));
//        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 1000 * seconds, pintent);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000 * 20, 1000 * 20, pintent);
    }


}
