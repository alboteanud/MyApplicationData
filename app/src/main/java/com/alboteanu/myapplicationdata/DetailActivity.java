package com.alboteanu.myapplicationdata;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.alboteanu.myapplicationdata.models.PostFixed;
import com.alboteanu.myapplicationdata.models.Post;
import com.alboteanu.myapplicationdata.models.PostDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.alboteanu.myapplicationdata.R.id.edit_text2;

public class DetailActivity extends BaseActivity {
    private static final String TAG = "DetailActivity";
    private static final String REQUIRED = "Required";
    private static final String INVALID_EMAIL = "Invalid email";
    public static final String EXTRA_POST_KEY = "post_key";
//    public static final String EXTRA_POST_TEXT2 = "post_text2";
//    public static final String EXTRA_POST_TEXT4 = "post_text4";
    private String existingPostKey;
    EditText editText1, editText2, editText3, editText4, editText5, editText6, editText7, editText8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initViews();

        linkFixedFieldsToFirebase();
        existingPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
//        editText2.setText(getIntent().getStringExtra(EXTRA_POST_TEXT2));
//        editText4.setText(getIntent().getStringExtra(EXTRA_POST_TEXT4));
        if(existingPostKey != null){
            linkVariableFieldsToFirebase();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);            //hide keyboard
        }
        editText2.requestFocus();
        (findViewById(R.id.save_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tryToSubmitPost())
                    finish();
            }
        });
    }

    private void initViews() {
        editText1 = ((EditText) findViewById(R.id.edit_text1));
        editText2 = ((EditText) findViewById(edit_text2));
        editText3 = ((EditText) findViewById(R.id.edit_text3));
        editText4 = ((EditText) findViewById(R.id.edit_text4));
        editText5 = ((EditText) findViewById(R.id.edit_text5));
        editText6 = ((EditText) findViewById(R.id.edit_text6));
        editText7 = ((EditText) findViewById(R.id.edit_text7));
        editText8 = ((EditText) findViewById(R.id.edit_text8));
    }

    private void linkFixedFieldsToFirebase() {
        Utils.getUserNode().child(getString(R.string.posts_fixed))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        PostFixed postFixed = dataSnapshot.getValue(PostFixed.class);
                        if(postFixed != null){
                            editText1.setText(postFixed.text1);
                            editText3.setText(postFixed.text3);
                            editText5.setText(postFixed.text5);
                            editText7.setText(postFixed.text7);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getMessage());
                    }
                });
    }

    private void linkVariableFieldsToFirebase() {
        Utils.getUserNode().child(getString(R.string.posts_details)).child(existingPostKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               PostDetails postDetails = dataSnapshot.getValue(PostDetails.class);
               if(postDetails != null){
                   editText2.setText(postDetails.text2);
                   editText4.setText(postDetails.text4);
                   editText6.setText(postDetails.text6);
                   editText8.setText(postDetails.text8);

                   editText2.setSelection(editText2.getText().length());
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
               Log.e(TAG, databaseError.getMessage());
           }
       });
    }

    private boolean tryToSubmitPost() {
        final String text2 = editText2.getText().toString();  //Name
        // Name is required
        if (TextUtils.isEmpty(text2)) {
            editText2.setError(REQUIRED);
            return false;
        }

        Map<String, Object> updates = new HashMap<>();
        final String text6 = editText6.getText().toString();  // email field
        if(!TextUtils.isEmpty(text6)){
            if(!Utils.isValidEmail(text6)){
                ((EditText) findViewById(R.id.edit_text6)).setError(INVALID_EMAIL);
                return false;
            }else {
                updates.put(getString(R.string.posts_emails) + "/" + existingPostKey, text6);
            }
        }

        final String text1 = editText1.getText().toString();  //Fixed NAME
        final String text3 = editText3.getText().toString();   //Fixed PHONE
        final String text4 = editText4.getText().toString();  // phone
        final String text5 = editText5.getText().toString();    // Fixed EMAIL
        final String text7 = editText7.getText().toString();   // Fixed Other
        final String text8 = editText8.getText().toString();    // other

        if(existingPostKey == null)
            existingPostKey = Utils.getUserNode().child(getString(R.string.posts_title)).push().getKey();  //generate new key

        Post post = new Post(text2, text4);
        PostDetails postDetails = new PostDetails(text2, text4, text6, text8);
        PostFixed postFixed = new PostFixed(text1, text3, text5, text7);

        Map<String, Object> postMap = post.toMap();
        Map<String, Object> detailMap = postDetails.toMap();
        Map<String, Object> fixedMap = postFixed.toMap();

        updates.put(getString(R.string.posts_title) + "/" + existingPostKey, postMap);
        updates.put( getString(R.string.posts_details) + "/" + existingPostKey, detailMap);
        updates.put(getString(R.string.posts_fixed), fixedMap);

        Utils.getUserNode().updateChildren(updates);
        return true;
    }



    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        if (existingPostKey == null){
            menu.findItem(R.id.action_delete_post).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_post) {
            deletePost();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deletePost() {
        Utils.getUserNode().child(getString(R.string.posts_title) + "/" + existingPostKey).removeValue();
        Utils.getUserNode().child( getString(R.string.posts_details) + "/" + existingPostKey).removeValue();
        Utils.getUserNode().child( getString(R.string.posts_emails) + "/" + existingPostKey).removeValue();
    }

}
