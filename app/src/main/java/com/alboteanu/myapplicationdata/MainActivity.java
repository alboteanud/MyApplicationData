package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.alboteanu.myapplicationdata.models.Post;
import com.alboteanu.myapplicationdata.viewholder.PostHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class MainActivity extends BaseActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private FirebaseRecyclerAdapter<Post, PostHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, DetailActivity.class));
            }
        });

        populateRecyclerView();
    }

    private void populateRecyclerView() {
        RecyclerView mRecycler = (RecyclerView) findViewById(R.id.posts_list);
        mRecycler.setHasFixedSize(true);

        // Set up Layout Manager, reverse layout
        LinearLayoutManager mManager = new LinearLayoutManager(this);
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query postsQuery = getQuery();
        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Post, PostHolder>(Post.class, R.layout.post_item,
                PostHolder.class, postsQuery) {
            @Override
            protected void populateViewHolder(final PostHolder viewHolder, final Post post, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch DetailActivity
                        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);
                    }
                });

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(post);
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    public Query getQuery() {
        return getDatabase().getReference()
                .child(getUid()).child(getString(R.string.posts));
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.cleanup();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
