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

import com.alboteanu.myapplicationdata.models.FixedPost;
import com.alboteanu.myapplicationdata.models.Post;
import com.alboteanu.myapplicationdata.models.PostDetails;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "DetailActivity";
    private static final String REQUIRED = "Required";
    public static final String EXTRA_POST_KEY = "post_key";
    private String postKey;
    EditText editText1, editText2, editText3, editText4, editText5, editText6;
    Menu mMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.save_button).setOnClickListener(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        editText1 = ((EditText) findViewById(R.id.edit_text1));
        editText2 = ((EditText) findViewById(R.id.edit_text2));
        editText3 = ((EditText) findViewById(R.id.edit_text3));
        editText4 = ((EditText) findViewById(R.id.edit_text4));
        editText5 = ((EditText) findViewById(R.id.edit_text5));
        editText6 = ((EditText) findViewById(R.id.edit_text6));

        updateFixedFields();
        postKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(postKey == null){

        }else {
            updateVariableFields();
        }
        editText2.requestFocus();
    }

    private void updateFixedFields() {
        getDatabase().getReference().child(getUid()).child(getString(R.string.fixed_posts))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        FixedPost fixedPost = dataSnapshot.getValue(FixedPost.class);
                        if(fixedPost != null){
                            editText1.setText(fixedPost.text1);
                            editText3.setText(fixedPost.text3);
                            editText5.setText(fixedPost.text5);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, databaseError.getMessage());
                    }
                });
    }

    private void updateVariableFields() {
        getDatabase().getReference().child(getUid()).child(getString(R.string.posts_details)).child(postKey)
                .addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               PostDetails postDetails = dataSnapshot.getValue(PostDetails.class);
               if(postDetails != null){
                   editText2.setText(postDetails.text2);
                   editText4.setText(postDetails.text4);
                   editText6.setText(postDetails.text6);

                   editText2.setSelection(editText2.getText().length());
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
               Log.e(TAG, databaseError.getMessage());
           }
       });
    }


    private void submitPost() {
        final String text1 = editText1.getText().toString();
        final String text2 = editText2.getText().toString();
        final String text3 = editText3.getText().toString();
        final String text4 = editText4.getText().toString();
        final String text5 = editText5.getText().toString();
        final String text6 = editText6.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(text2)) {
            ((EditText) findViewById(R.id.edit_text2)).setError(REQUIRED);
            return;
        }

        if(postKey == null)
            postKey = getDatabase().getReference().push().getKey();

        Post post = new Post(text2, text4);
        PostDetails postDetails = new PostDetails(text2, text4, text6);
        FixedPost fixedPost = new FixedPost(text1, text3, text5);

        Map<String, Object> postMap = post.toMap();
        Map<String, Object> detailMap = postDetails.toMap();
        Map<String, Object> fixedMap = fixedPost.toMap();

        Map<String, Object> updates = new HashMap<>();
        updates.put(getUid()+ "/" + getString(R.string.posts) + "/" + postKey, postMap);
        updates.put(getUid()+ "/" + getString(R.string.posts_details) + "/" + postKey, detailMap);
        updates.put(getUid()+ "/" + getString(R.string.fixed_posts), fixedMap);

        getDatabase().getReference().updateChildren(updates);

        finish();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.save_button:
                submitPost();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        if (postKey == null){
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
        getDatabase().getReference().child(getUid()+ "/" + getString(R.string.posts) + "/" + postKey).removeValue();
        getDatabase().getReference().child(getUid()+ "/" + getString(R.string.posts_details) + "/" + postKey).removeValue();
    }
}
