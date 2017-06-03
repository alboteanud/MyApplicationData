package com.alboteanu.myapplicationdata.screens;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.alboteanu.myapplicationdata.BaseActivity;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.models.Contact;
import com.alboteanu.myapplicationdata.others.DatePickerFragment;
import com.alboteanu.myapplicationdata.others.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import static com.alboteanu.myapplicationdata.others.Constants.FIREBASE_LOCATION_CONTACTS;

public class BaseDetailsActivity extends BaseActivity {

    public static final String ACTION_CONTACT_DELETED = "action_contact_deleted";
    private static final String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void deleteContact(String contactKey) {
        Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS + "/" + contactKey).removeValue();
    }

    void createDeleteDialogAlert(final String contactKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.confirmation_dialog_delete))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        deleteContact(contactKey);
                        Intent intent = new Intent(BaseDetailsActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(ACTION_CONTACT_DELETED, contactKey);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(@NonNull DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    void showDatePickerDialog() {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getFragmentManager(), "datePicker");
    }

    void updateUIfromFirebase(@NonNull final String contactKey) {
        final DatabaseReference ref = Utils.getUserNode().child(FIREBASE_LOCATION_CONTACTS).child(contactKey);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Contact contact = dataSnapshot.getValue(Contact.class);
                Log.d(TAG, "OnDataChange()");
                if(contact != null){
                    updateUI(contact);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void updateUI(Contact contact) {
        Log.d(TAG, "updateUI()");
    }

}
