package com.alboteanu.myapplicationdata.screens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.text.DateFormat;

import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_DATE;
import static com.alboteanu.myapplicationdata.others.Constants.EXTRA_EDIT_NOTE;

public class QuickContactActivity extends BaseDetailsActivity implements View.OnClickListener {
    String key;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Get post key from intent
        key = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        Log.d("tag", "QuickContactActivity  key " + key);
        updateUIfromFirebase(key);
        findViewById(R.id.ic_action_phone).setOnClickListener(this);
        findViewById(R.id.ic_action_message).setOnClickListener(this);
        findViewById(R.id.ic_action_email).setOnClickListener(this);
        findViewById(R.id.ic_action_date).setOnClickListener(this);
        findViewById(R.id.ic_action_note).setOnClickListener(this);

        loadAd();
    }

    public void updateUI(Contact contact) {
        super.updateUI(contact);
        Log.d("tag", "updateUI() in QuickContactActivity");
        getSupportActionBar().setTitle(contact.name);
        ((TextView) findViewById(R.id.nameText)).setText(contact.name);
        ((TextView) findViewById(R.id.phoneText)).setText(contact.phone);
        if(contact.phone != null)
            findViewById(R.id.ic_action_message).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.emailText)).setText(contact.email);
        ((TextView) findViewById(R.id.noteText)).setText(contact.note);
        if(contact.date > 0) {
            String date = DateFormat.getDateInstance().format(contact.date);
            ((TextView) findViewById(R.id.dateText)).setText(date);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quick_contact, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case  R.id.action_delete_contact:
                createDeleteDialogAlert(key);
                return true;
            case R.id.action_edit:
                sendUserToEditActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(@NonNull View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ic_action_phone:
                String phoneNumber = ((TextView) findViewById(R.id.phoneText)).getText().toString();
                if (!phoneNumber.isEmpty()) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
                }
                break;
            case R.id.ic_action_message:
                String phoneMessage = ((TextView) findViewById(R.id.phoneText)).getText().toString();
                if (!phoneMessage.isEmpty()) {
                    Utils.composeSMS( new String[]{phoneMessage} , this);
                }
                break;
            case R.id.ic_action_email:
                String email = ((TextView) findViewById(R.id.emailText)).getText().toString();
                if (!email.isEmpty()) {
                    String[] emails = new String[]{email};
                    Utils.composeEmail(v.getContext(), emails);
                }
                break;
            case R.id.ic_action_date:
                Intent editDateIntent = new Intent(QuickContactActivity.this, EditActivity.class);
                editDateIntent.putExtra(EXTRA_EDIT_DATE, key);
                startActivity(editDateIntent);
                break;
            case R.id.ic_action_note:
                Intent noteIntent = new Intent(QuickContactActivity.this, EditActivity.class);
                noteIntent.putExtra(EXTRA_EDIT_NOTE, key);
                startActivity(noteIntent);
                break;
        }

    }

    private void sendUserToEditActivity(){
        Intent intent = new Intent(QuickContactActivity.this, EditActivity.class);
        intent.putExtra(EXTRA_CONTACT_KEY, key);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
    }

    @Override
    protected void onPause() {
        if (mAdView != null)
            mAdView.pause();
        super.onPause();
        Log.d("tag", "onPause");
    }

    @Override
    protected void onDestroy() {
        if (mAdView != null)
            mAdView.destroy();
        super.onDestroy();
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

}
