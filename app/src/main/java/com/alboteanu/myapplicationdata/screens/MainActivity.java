package com.alboteanu.myapplicationdata.screens;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
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
import com.alboteanu.myapplicationdata.others.ContactHolder;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.others.MyAnimationListener;
import com.alboteanu.myapplicationdata.others.MyDragShadowBuilder;
import com.alboteanu.myapplicationdata.others.Utils;
import com.alboteanu.myapplicationdata.login.SignInActivity;
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
import java.util.ListIterator;

import static android.R.attr.action;
import static android.R.attr.id;
import static android.R.attr.key;
import static android.R.attr.tag;
import static com.alboteanu.myapplicationdata.R.layout.contact_view;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_EMAILS;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAME;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_NAMES_DATES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_PHONES;
import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_RETURN;

public class MainActivity extends BaseActivity {
    private FirebaseRecyclerAdapter<Contact, ContactHolder> firebaseRecyclerAdapter;
    Toolbar toolbar;
    private Menu menu;
    @NonNull
    HashMap<String, String> phonesMap = new HashMap<>();
    @NonNull
    HashMap<String, String> emailsMap = new HashMap<>();
    private GoogleApiClient client;
    @Nullable
    ArrayList<String> selectedCheckBoxes = new ArrayList<>();
    private static final String FLAG_SELECT_ALL = "select_all";
    LinearLayoutManager mManager;
    RecyclerView mRecycler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, EditActivity.class));
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        if (savedInstanceState != null)
            selectedCheckBoxes = savedInstanceState.getStringArrayList("selectedCheckBoxes");

        String action = getIntent().getAction();
        if (action != null && action.equals(ACTION_UPDATE_LOCAL_CONTACTS)) {
            updateLocalDataBase();

            Utils.saveDefaultTitle(this);
        }

        mManager = new LinearLayoutManager(this);
        mRecycler = (RecyclerView) findViewById(R.id.contact_list);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(mManager);
    }


    private void rebuilStateOfMapsAndCheckboxes() {
        if (selectedCheckBoxes.contains(FLAG_SELECT_ALL)) {
            getAllPhones();
            getAllEmails();
        } else {
            ListIterator<String> iterator = selectedCheckBoxes.listIterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                putPhoneToMap(key);
                putEmailToMap(key);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putStringArrayList("selectedCheckBoxes", selectedCheckBoxes);

        // Save state
        Parcelable recyclerViewState;
        recyclerViewState = mManager.onSaveInstanceState();
        outState.putParcelable("recyclerViewState", recyclerViewState);

//        int firstItem = mManager.findFirstCompletelyVisibleItemPosition();
//        outState.putInt("recyclerOffset", firstItem);

        super.onSaveInstanceState(outState);
        Log.d("tag", "onSaveInstanceStates" );
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final Parcelable recyclerViewState = savedInstanceState.getParcelable("recyclerViewState");
        // Resore state

//        final int firstItemVisible = savedInstanceState.getInt("recyclerOffset");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                mManager.scrollToPosition(firstItemVisible);
                mManager.onRestoreInstanceState(recyclerViewState);
            }
        }, 300);

        rebuilStateOfMapsAndCheckboxes();
        Log.d("tag", "onRestoreInstanceStates");
    }

    private void populateRecyclerView() {
//        Log.d("tag", " populateRecyclerView()");


        Query postsQuery = Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES).orderByChild(FIREBASE_LOCATION_NAME);

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contact, ContactHolder>(Contact.class, contact_view,
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
                    if (Calendar.getInstance().after(calendarReturn)) {
                        sunglassVisibility = View.VISIBLE;
                        Log.d("tag", "position " + position + " retMills " + returnMills + "  VISIBLE");
                    }
                    contactHolder.sandglass.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(@NonNull View view) {
                            // Create a new ClipData.
                            // This is done in two steps to provide clarity. The convenience method
                            // ClipData.newPlainText() can create a plain text ClipData in one step.


                            // Create a new ClipData.Item from the ImageView object's tag
//                            final String tag = view.getTag().toString();
//                            ClipData.Item item = new ClipData.Item(tag);
                            // Create a new ClipData using the tag as a label, the plain text MIME type, and
                            // the already-created item. This will create a new ClipDescription object within the
                            // ClipData, and set its MIME type entry to "text/plain"
//                            ClipData dragData = new ClipData(tag, new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
//                          ClipData dragData = ClipData.newPlainText("", "");

                            // Instantiates the com.alboteanu.myapplicationdata.others.sandGlassDragEventListener shadow builder.
                            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(view);

                            // Starts the com.alboteanu.myapplicationdata.others.sandGlassDragEventListener
                            view.setOnDragListener(new View.OnDragListener() {

                                @Override
                                public boolean onDrag(@NonNull View view, @NonNull DragEvent dragEvent) {
                                    switch (dragEvent.getAction()) {

                                        case DragEvent.ACTION_DRAG_STARTED:
                                            // Determines if this View can accept the dragged data
//                                            if (dragEvent.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                                                ((ImageView) view).setColorFilter(Color.LTGRAY);
                                                view.invalidate();
                                                // returns true to indicate that the View can accept the dragged data.
//                                                return true;

                                            // Returns false. During the current drag and drop operation, this View will
                                            // not receive events again until ACTION_DRAG_ENDED is sent.
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
//                                                Animation animationFadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
//                                                animationFadeOut.setAnimationListener(new MyAnimationListener(view, View.INVISIBLE));
//                                                view.startAnimation(animationFadeOut);
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
                                            // Determines if this View can accept the dragged data
//                                            if (dragEvent.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
//                                                ((ImageView) view).setColorFilter(Color.LTGRAY);
//                                                view.invalidate();
                                                // returns true to indicate that the View can accept the dragged data.
                                                return true;

                                            // Returns false. During the current drag and drop operation, this View will
                                            // not receive events again until ACTION_DRAG_ENDED is sent.
//                                            return false;

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
//                                            ClipData.Item item = dragEvent.getClipData().getItemAt(0);
                                            view.setBackgroundColor(Color.TRANSPARENT);
//                                            view.invalidate();
                                            // Gets the text data from the item.
//                                            CharSequence dragData = item.getText();
//                                            final int position = Integer.valueOf(dragData.toString());
                                            clearReturnDate(position);
                                            return true;


                                        case DragEvent.ACTION_DRAG_ENDED:
                                            Animation animationFadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
                                            animationFadeOut.setAnimationListener(new MyAnimationListener(view, View.GONE));
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
                if (selectedCheckBoxes.contains(FLAG_SELECT_ALL)) {
                    if (!selectedCheckBoxes.contains(key))
                        contactHolder.checkBox.setChecked(true);
                } else if (selectedCheckBoxes.contains(key))
                    contactHolder.checkBox.setChecked(true);
                else
                    contactHolder.checkBox.setChecked(false);


                View.OnClickListener onClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        int id = view.getId();
                        switch (id) {
                            case R.id.checkBoxSelectContact:
                                boolean checked = ((CheckBox) view).isChecked();
//                                final int position = Integer.valueOf(view.getTag().toString());
//                                Log.d("tag", "position " + position);
                                final String firebase_key = firebaseRecyclerAdapter.getRef(position).getKey();
                                if (checked) {
                                    putPhoneToMap(firebase_key);
                                    putEmailToMap(firebase_key);
                                    if (selectedCheckBoxes.contains(FLAG_SELECT_ALL))
                                        selectedCheckBoxes.remove(firebase_key);
                                    else
                                        selectedCheckBoxes.add(firebase_key);
                                    menu.findItem(R.id.action_select_none).setVisible(true);

                                    //todo  alte chestii se pot face aici - la selectare
                                } else {
                                    phonesMap.remove(firebase_key);
                                    emailsMap.remove(firebase_key);
                                    if (selectedCheckBoxes.contains(FLAG_SELECT_ALL))
                                        selectedCheckBoxes.add(firebase_key);
                                    else selectedCheckBoxes.remove(firebase_key);
                                    menu.findItem(R.id.action_email).setVisible(!emailsMap.isEmpty());
                                    menu_item_action_sms.setVisible(!phonesMap.isEmpty());
                                    menu.findItem(R.id.action_select_all).setVisible(true);
                                }
                                break;
                            case R.id.contact_view:
//                                final int position2 = Integer.valueOf(view.getTag().toString());
                                final String firebase_key2 = firebaseRecyclerAdapter.getRef(position).getKey();
                                Intent intent = new Intent(MainActivity.this, QuickContactActivity.class);
                                intent.putExtra(EXTRA_CONTACT_KEY, firebase_key2);
//                                intent.putExtra(EXTRA_CONTACT_COLOR, color);
                                startActivity(intent);
                                break;
                        }

                    }
                };

                contactHolder.itemView.setOnClickListener(onClickListener);
                contactHolder.checkBox.setOnClickListener(onClickListener);
                contactHolder.bindContact(contact_name_date, getDrawable(R.drawable.shape_oval));

            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    protected void onStart() {
        Log.d("tag", "onStart");
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
//        toolbar.setTitle(Utils.getSavedTitle(this));
        toolbar.setTitle(getString(R.string.app_name));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.cleanup();
        }
    }

    MenuItem menu_item_action_sms;

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        this.menu = menu;
//        rebuilStateOfMapsAndCheckboxes();
        Log.d("tag", "onCreateOptionsMenu");
        menu_item_action_sms = menu.findItem(R.id.action_sms);
        menu.findItem(R.id.action_email).setVisible(!emailsMap.isEmpty());
        menu_item_action_sms.setVisible(!phonesMap.isEmpty());
        menu.findItem(R.id.action_select_none).setVisible(!selectedCheckBoxes.isEmpty());
        menu.findItem(R.id.action_select_all).setVisible(!selectedCheckBoxes.contains(FLAG_SELECT_ALL));
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                menu.findItem(R.id.action_select_all).setVisible(false);
                menu.findItem(R.id.action_select_none).setVisible(true);
                break;
            case R.id.action_select_none:
                selectedCheckBoxes.clear();
                phonesMap.clear();
                emailsMap.clear();
                firebaseRecyclerAdapter.notifyDataSetChanged();
                menu.findItem(R.id.action_email).setVisible(!emailsMap.isEmpty());
                menu_item_action_sms.setVisible(!phonesMap.isEmpty());
                menu.findItem(R.id.action_select_all).setVisible(true);
                menu.findItem(R.id.action_select_none).setVisible(false);
                break;
            case R.id.action_email:
                String[] emails = emailsMap.values().toArray(new String[0]);
                if (emails.length > 0)
                    Utils.composeEmail(this, emails, "title");
                break;
            case R.id.action_sms:
                String[] phoneList = phonesMap.values().toArray(new String[0]);
                if (phoneList.length > 0)
                    Utils.composeSMS(phoneList, this);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void clearReturnDate(int position) {
        String firebase_key = firebaseRecyclerAdapter.getRef(position).getKey();
        Utils.getUserNode().child(FIREBASE_LOCATION_NAMES_DATES + "/" + firebase_key).child(FIREBASE_LOCATION_RETURN).removeValue();
//        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS + "/" + firebase_key).child(FIREBASE_LOCATION_RETURN).removeValue();
//        firebaseRecyclerAdapter.notifyItemChanged(position);
//        firebaseRecyclerAdapter.notifyDataSetChanged();
//        firebaseRecyclerAdapter.notifyItemChanged(position);  //OK
//        firebaseRecyclerAdapter.notify();
        Log.d("tag", "clearReturnDate(" + position + ")");
    }

    private void goToSignInActivity() {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void putPhoneToMap(@NonNull String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONES).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("tag", "onDataChange");
                        String phone = (String) dataSnapshot.getValue();
                        if (phone != null)
                            phonesMap.put(dataSnapshot.getKey(), phone);

                        if(menu!=null)
                            menu_item_action_sms.setVisible(!phonesMap.isEmpty());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    private void putEmailToMap(@NonNull String key) {
        Utils.getUserNode().child(FIREBASE_LOCATION_EMAILS).child(key)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String email = (String) dataSnapshot.getValue();
                        if (email != null)
                            emailsMap.put(dataSnapshot.getKey(), email);

                        menu.findItem(R.id.action_email).setVisible(!emailsMap.isEmpty());
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
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        phonesMap = (HashMap<String, String>) dataSnapshot.getValue();

                        //remouving the unselected phones
                        ListIterator<String> iterator = selectedCheckBoxes.listIterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (key != FLAG_SELECT_ALL) {
                                phonesMap.remove(key);
                            }
                        }
                        if(menu!=null)
                            menu_item_action_sms.setVisible(!phonesMap.isEmpty());
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
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        emailsMap = (HashMap<String, String>) dataSnapshot.getValue();

                        //remouving the unselected
                        ListIterator<String> iterator = selectedCheckBoxes.listIterator();
                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            if (key != FLAG_SELECT_ALL) {
                                emailsMap.remove(key);
                            }
                        }
                        if(menu!=null)
                            menu.findItem(R.id.action_email).setVisible(!emailsMap.isEmpty());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @NonNull
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

}
