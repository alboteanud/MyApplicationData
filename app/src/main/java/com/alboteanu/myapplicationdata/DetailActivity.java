package com.alboteanu.myapplicationdata;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;

import com.alboteanu.myapplicationdata.models.PostDetails;
import com.alboteanu.myapplicationdata.viewholder.DetailPostViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "DetailActivity";
    private static final String REQUIRED = "Required";
    public static final String EXTRA_POST_KEY = "post_key";
    private String existingPostKey;
    private ValueEventListener mPostListener;
    private FirebaseRecyclerAdapter<PostDetails, DetailPostViewHolder> firebaseRecyclerAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        findViewById(R.id.save_button).setOnClickListener(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        existingPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(existingPostKey != null)
            populateRecyclerView();
    }

    private void populateRecyclerView() {
        mRecycler = (RecyclerView) findViewById(R.id.detail_posts_list);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<PostDetails, DetailPostViewHolder>(PostDetails.class, R.layout.detail_list_item,
                DetailPostViewHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final DetailPostViewHolder viewHolder, final PostDetails postDetails, final int position) {
                viewHolder.bindToDetailPost(postDetails);
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public Query getQuery() {
        // All my posts
        return getDatabase().getReference().child(getUid()).child(getString(R.string.user_posts_node)).child(existingPostKey);
    }



    private void submitPost() {
//        final String text4 = mText1.getText().toString();
//        final String text2 = mText2.getText().toString();
//
//        // Title is required
//        if (TextUtils.isEmpty(text4)) {
//            mText1.setError(REQUIRED);
//            return;
//        }

//TODO
// create a Post and a PostDetails object and push it to diffrent nodes

        String key;
        if(existingPostKey == null)
            key = getDatabase().getReference().push().getKey();
        else
            key = existingPostKey;

        PostDetails postDetails = new PostDetails("text4", "text2");

        Map<String, Object> postValues = postDetails.toMap();
        Map<String, Object> childUpdates = new HashMap<>();
//        childUpdates.put("/posts/" + key, postValues);
        childUpdates.put(getUid() + "/" + getString(R.string.user_posts_node) + "/" + key, postValues);

        getDatabase().getReference().updateChildren(childUpdates);

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
    public void onDestroy() {
        super.onDestroy();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.cleanup();
        }
    }
}
