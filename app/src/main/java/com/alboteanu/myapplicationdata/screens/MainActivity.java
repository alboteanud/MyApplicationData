package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.login.ActivitySignIn;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.setting.SettingsActivity;
import com.alboteanu.myapplicationdata.viewholder.ContactHolder;
import com.alboteanu.myapplicationdata.others.MyAnimationListener;
import com.alboteanu.myapplicationdata.others.MyDragShadowBuilder;
import com.alboteanu.myapplicationdata.others.Utils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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

import static com.alboteanu.myapplicationdata.R.layout.contact_view;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAMES_DATES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONES_EMAILS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;
import static com.alboteanu.myapplicationdata.setting.SettingsActivity.ACTION_TITLE_CHANGED;

public class MainActivity extends BaseActivity {
    private static final String RECYCLER_STATE = "recycler_state";
    private static final String SAVED_SELECTED_CONTACTS = "saved_contacts";
    HashMap<String, Contact> selectedContactsPhoneEmail = new HashMap<>();
    RecyclerView mRecycler;
    private FirebaseRecyclerAdapter<Contact, ContactHolder> recyclerAdapter;
    private GoogleApiClient client;
    AdView mAdView;
    LinearLayoutManager mManager;
    Bundle savedInstanceState;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(Utils.getSavedTitle(this));
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
            updateLocalDataBase();
            Utils.saveDefaultTitle(this);
        }
        mManager = new LinearLayoutManager(this);
        mRecycler = (RecyclerView) findViewById(R.id.contact_list);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(mManager);
//        this.savedInstanceState = savedInstanceState;

         loadAd();
    }

    private void loadAd() {
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3931793949981809~8705632377");  //app ID din Banner Petru si Dan
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putSerializable(SAVED_SELECTED_CONTACTS, selectedContactsPhoneEmail);
//        outState.putStringArrayList(SELECTED_BOXES_STATE, selectedCheckBoxes);
        // save position of recyclerView
        outState.putParcelable(RECYCLER_STATE, mManager.onSaveInstanceState());
//        int firstItem = mManager.findFirstCompletelyVisibleItemPosition();
//        outState.putInt("recyclerOffset", firstItem);
        super.onSaveInstanceState(outState);
        Log.d("tag", "onSaveInstanceStates");
        this.savedInstanceState = outState;
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
//        rebuilStateOfMapsAndCheckboxes(savedInstanceState);
        //      selectedContactsPhoneEmail = (HashMap<String, Contact>) savedInstanceState.getSerializable(SAVED_SELECTED_CONTACTS);  //already dit it in onStart()
        Log.d("tag", "onRestoreInstanceStates");
    }

    private void restoreLayoutManagerPosition() {
        if (savedInstanceState != null) {
            Parcelable recyclerViewState = savedInstanceState.getParcelable(RECYCLER_STATE);
            mManager.onRestoreInstanceState(recyclerViewState);
            Log.d("tag", "restoreLayoutManagerPosition");
        }

    }

    private void populateRecyclerView() {
        final Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES).orderByChild(FIREBASE_LOCATION_NAME);
        recyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(Contact.class, contact_view,
                ContactHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(@NonNull final ContactHolder contactHolder, @NonNull Contact contact_name_date, final int position) {
//                Log.d("tag", " populateViewHolder()  pos " + position);
                final DatabaseReference postRef = getRef(position);
                final String key = postRef.getKey();
                int sunglassVisibility = View.INVISIBLE;
                if (contact_name_date.retur.containsKey(FIREBASE_LOCATION_RETURN)) {
                    long returnMills = contact_name_date.retur.get(FIREBASE_LOCATION_RETURN);
                    Calendar calendarReturn = Calendar.getInstance();
                    calendarReturn.setTimeInMillis(returnMills);
                    if (Calendar.getInstance().after(calendarReturn))
                        sunglassVisibility = View.VISIBLE;
                    contactHolder.sandglass.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(@NonNull View view) {
                            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(view);
                            view.setOnDragListener(new View.OnDragListener() {

                                @Override
                                public boolean onDrag(@NonNull View view, @NonNull DragEvent dragEvent) {
                                    switch (dragEvent.getAction()) {

                                        case DragEvent.ACTION_DRAG_STARTED:
                                            // Determines if this View can accept the dragged data
                                            ((ImageView) view).setColorFilter(Color.LTGRAY);
                                            view.invalidate();
                                            // returns true to indicate that the View can accept the dragged data.
                                            return true;

                                        case DragEvent.ACTION_DRAG_ENTERED:
//                                          Log.d("tag", "ACTION_DRAG_ENTERED  id " + id);
                                            // Applies a green tint to the View. Return true; the return value is ignored.
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                                ((ImageView) view).setColorFilter(getColor(R.color.colorPrimary));
                                            else
                                                ((ImageView) view).setColorFilter(getResources().getColor(R.color.colorPrimary));
                                            view.invalidate();

                                            return true;

                                        case DragEvent.ACTION_DRAG_EXITED:  //out of the box
                                            ((ImageView) view).setColorFilter(Color.LTGRAY);
                                            // Invalidate the view to force a redraw in the new tint
                                            view.invalidate();

                                            return true;

                                        case DragEvent.ACTION_DRAG_ENDED:
                                            Log.d("tag", " sandGlass  ACTION_DRAG_ENDED");
                                            if (dragEvent.getResult()) {      // Delete successful

                                            } else
                                                ((ImageView) view).clearColorFilter();
                                            view.invalidate();
                                            view.setOnDragListener(null);
                                            // returns true; the value is ignored.
                                            return true;

                                        // An unknown action type was received.
                                        default:
                                            break;
                                    }
                                    return false;
                                }
                            });
                            contactHolder.bin.setVisibility(View.VISIBLE);
                            contactHolder.bin.setOnDragListener(new View.OnDragListener() {
                                @Override
                                public boolean onDrag(@NonNull View view, @NonNull DragEvent dragEvent) {
                                    switch (dragEvent.getAction()) {

                                        case DragEvent.ACTION_DRAG_STARTED:
                                            // returns true to indicate that the View can accept the dragged data.
                                            return true;

                                        case DragEvent.ACTION_DRAG_ENTERED:
                                            view.setBackgroundColor(Color.LTGRAY);
                                            ((ImageView) view).setColorFilter(Color.RED);
                                            view.invalidate();
                                            return true;

                                        case DragEvent.ACTION_DRAG_EXITED:  //out of the box
                                            view.setBackgroundColor(Color.TRANSPARENT);
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                                ((ImageView) view).setColorFilter(getColor(R.color.colorPrimary));
                                            else
                                                ((ImageView) view).setColorFilter(getResources().getColor(R.color.colorPrimary));
                                            view.invalidate();
                                            return true;

                                        case DragEvent.ACTION_DROP:
                                            view.setBackgroundColor(Color.TRANSPARENT);
//                                            view.invalidate();
//                                            String firebase_key = recyclerAdapter.getRef(position).getKey();
                                            Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES + "/" + key)
                                                    .child(FIREBASE_LOCATION_RETURN).removeValue();
                                            return true;

                                        case DragEvent.ACTION_DRAG_ENDED:
                                            Animation animationFadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                                            animationFadeOut.setAnimationListener(new MyAnimationListener(view));
                                            view.startAnimation(animationFadeOut);
                                            ((ImageView) view).clearColorFilter();
                                            view.invalidate();
                                            view.setOnDragListener(null);
                                            // returns true; the value is ignored.
                                            return true;

                                        // An unknown action type was received.
                                        default:
                                            break;
                                    }
                                    return true;
                                }
                            });

                            // Starts the drag
                            view.startDrag(null,  // the data to be dragged
                                    myShadow,  // the drag shadow builder
                                    null,      // no need to use local data
                                    0          // flags (not currently used, set to 0)
                            );
                            return false;
                        }
                    });
                }
                contactHolder.sandglass.setVisibility(sunglassVisibility);
                contactHolder.checkBox.setChecked(selectedContactsPhoneEmail.containsKey(key));
                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        int id = view.getId();
                        switch (id) {

                            case R.id.checkBoxSelectContact:
                                boolean checked = ((CheckBox) view).isChecked();
                                if (checked) {
                                    addContactToSelectedList(key);
                                } else {
                                    selectedContactsPhoneEmail.remove(key);
                                }
                                break;

                            case R.id.contact_view:
                                Intent intent = new Intent(MainActivity.this, QuickContactActivity.class);
                                intent.putExtra(EXTRA_CONTACT_KEY, key);
                                startActivity(intent);
                                break;

                            case R.id.icon_sandglass:
                                Intent intent3 = new Intent(MainActivity.this, QuickContactActivity.class);
                                intent3.putExtra(EXTRA_CONTACT_KEY, key);
                                startActivity(intent3);
                                break;
                        }

                    }
                };
                contactHolder.itemView.setOnClickListener(onClickListener);
                contactHolder.sandglass.setOnClickListener(onClickListener);
                contactHolder.checkBox.setOnClickListener(onClickListener);
                contactHolder.bindContact(contact_name_date, getDrawable(R.drawable.shape_oval));
            }
        };
        postsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("tag", "onDataChange in recycler");
                restoreLayoutManagerPosition();
                postsQuery.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        mRecycler.setAdapter(recyclerAdapter);
    }

    @Override
    protected void onStart() {
        Log.d("tag", "onStart");
        super.onStart();
        if (savedInstanceState != null)
            selectedContactsPhoneEmail = (HashMap<String, Contact>) savedInstanceState.getSerializable(SAVED_SELECTED_CONTACTS);
        populateRecyclerView();

        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
        if (getIntent().hasExtra(ACTION_TITLE_CHANGED)) {
            final boolean titleChange = getIntent().getBooleanExtra(ACTION_TITLE_CHANGED, false);
            if (titleChange) {
                getSupportActionBar().setTitle(Utils.getSavedTitle(this));
                getIntent().putExtra(ACTION_TITLE_CHANGED, false); //reset
                Log.d("tag MainActivity", "titleChange");
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("tag", "onDestroy");
        if (recyclerAdapter != null) {
            recyclerAdapter.cleanup();
        }
        if (mAdView != null)
            mAdView.destroy();
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
//        this.menu = menu;
//        rebuilStateOfMapsAndCheckboxes();
        Log.d("tag", "onCreateOptionsMenu");
//        menu_item_action_sms = menu.findItem(R.id.action_sms);
//        menu.findItem(R.id.action_email).setVisible(!emailsMap.isEmpty());
//        menu_item_action_sms.setVisible(!phonesMap.isEmpty());
//        menu.findItem(R.id.action_select_none).setVisible(!(selectedCheckBoxes != null && selectedCheckBoxes.isEmpty()));
//        menu.findItem(R.id.action_select_all).setVisible(!selectedCheckBoxes.contains(FLAG_SELECT_ALL));
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
            case R.id.action_logout:
                clearSavedTitle();
                mAuth.signOut();
                startActivity(new Intent(this, ActivitySignIn.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                break;
            case R.id.action_email:
                List<String> emailsList = new ArrayList<>();
                for (Contact contact : selectedContactsPhoneEmail.values()) {
                    if (contact != null && contact.email != null)
                        emailsList.add(contact.phone);
                }
                if (!emailsList.isEmpty())
                    Utils.composeEmail(this, emailsList.toArray(new String[0]));
                break;
            case R.id.action_sms:
                List<String> phonesList = new ArrayList<>();
                for (Contact contact : selectedContactsPhoneEmail.values()) {
                    if (contact != null && contact.phone != null)
                        phonesList.add(contact.phone);
                }
                if (!phonesList.isEmpty())
                    Utils.composeSMS(phonesList.toArray(new String[0]), this);
                else {
                    Log.d("tag", "phonesList  empty");
                }
                break;
            case R.id.action_select_all:
                putAllContactsToMap();
                recyclerAdapter.notifyDataSetChanged();
                break;
            case R.id.action_select_none:
                selectedContactsPhoneEmail.clear();
                recyclerAdapter.notifyDataSetChanged();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void putAllContactsToMap() {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES_EMAILS)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("tag MainActivity", "onDataChange  putALLContactsToSelectedList");
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Contact contact = snapshot.getValue(Contact.class);
                            selectedContactsPhoneEmail.put(snapshot.getKey(), contact);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
        // nu vor fi toate cheile in lista
    }

    private void clearSavedTitle() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String key = getString(R.string.display_title_text_key);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.remove(key);
        editor.apply();
    }

    void addContactToSelectedList(final String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES_EMAILS).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("tag MainActivity", "onDataChange  addContactToSelectedList");
                        Contact contact = dataSnapshot.getValue(Contact.class);
                        selectedContactsPhoneEmail.put(key, contact);
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

    @Override
    public void onStop() {
        super.onStop();
        Log.d("tag", "onStop");
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private void updateLocalDataBase() {
        Log.d("tag", "updateLocalDataBase()");
        Utils.getUserNode().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                Contact contact = dataSnapshot.getValue(Contact.class);
//                        if(contact != null)
//                        updateUI(contact);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onPause() {
        if (mAdView != null)
            mAdView.pause();
        super.onPause();
        Log.d("tag", "onPause");
    }
}
