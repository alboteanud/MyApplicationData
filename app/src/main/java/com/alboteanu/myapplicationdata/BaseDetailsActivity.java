package com.alboteanu.myapplicationdata;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.alboteanu.myapplicationdata.models.ContactLong;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import static com.alboteanu.myapplicationdata.Constants.EXTRA_CONTACT_KEY;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_CONTACT_S;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_EMAIL;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_PHONE;
import static com.alboteanu.myapplicationdata.Constants.FIREBASE_LOCATION_RETURN_DATES;

public class BaseDetailsActivity extends BaseActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    String contactKey, name, phone;
    long date;
    ContactLong contactLong;
    ContactShort contactShort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
        if(getIntent().hasExtra(FIREBASE_LOCATION_CONTACT)) {
            contactLong = (ContactLong) getIntent().getExtras().getSerializable(FIREBASE_LOCATION_CONTACT);
            updateLocalFields(contactLong);
        }
    }

    private void updateLocalFields(ContactLong contactLong) {
        name = contactLong.name;
        phone = contactLong.phone;
        date = contactLong.date;
    }

    public void sendUserToMainActivity() {
        /* Move user to LoginActivity, and remove the backstack */
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void deleteContact(String contactKey) {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT_S + "/" + contactKey).removeValue();
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT + "/" + contactKey).removeValue();
        Utils.getUserNode().child(FIREBASE_LOCATION_EMAIL + "/" + contactKey).removeValue();
        Utils.getUserNode().child(FIREBASE_LOCATION_PHONE + "/" + contactKey).removeValue();
        Utils.getUserNode().child(FIREBASE_LOCATION_RETURN_DATES + "/" + contactKey).removeValue();
    }

    public void createDeleteDialogAlert(final String contactKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation_dialog_delete))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        deleteContact(contactKey);
                        Intent intent = new Intent(BaseDetailsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void showDatePickerDialog() {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getFragmentManager(), "datePicker");
    }

    public void getContactFromFirebaseAndUpdateUI() {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACT).child(contactKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        ContactLong receivedContactLong = dataSnapshot.getValue(ContactLong.class);
                        if (receivedContactLong != null) {
                            contactLong = receivedContactLong;
                            updateUI();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }

    public void updateUI() {

    }

}
