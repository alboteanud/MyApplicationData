package com.alboteanu.myapplicationdata;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class BaseActivity extends AppCompatActivity {

    private static FirebaseDatabase mDatabase;

    static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    protected static DatabaseReference getMainNode() {
        final DatabaseReference ref = getDatabase().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        return ref;
    }
//    https://my-application-data.firebaseio.com/XhxrXrPieVPE7qnDHyNachPhgmH2

}
