package com.alboteanu.myapplicationdata.setting;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import com.alboteanu.myapplicationdata.R;
import com.alboteanu.myapplicationdata.screens.BaseDetailsActivity;
import com.alboteanu.myapplicationdata.screens.MainActivity;

public class SettingsActivity extends AppCompatPreferenceActivity
                implements GeneralPreferenceFragment.OnTitleChangeListener{
    public static final String ACTION_TITLE_CHANGED = "title_changed";
    private static final String TAG= "SettingsActivity";
    Intent upIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d(TAG, "onCreate()");
        upIntent = NavUtils.getParentActivityIntent(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
/*                Intent upIntent = NavUtils.getParentActivityIntent(this);
                upIntent.putExtra(ACTION_TITLE_CHANGED, titleChanged);
//                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                if (titleChanged) {
                    titleChanged = false;  //reset
                    // This activity is NOT part of this app's task, so create a new task
                    // when navigating up, with a synthesized back stack.
                    TaskStackBuilder.create(this)
                            // Add all of this activity's parents to the back stack
                            .addNextIntentWithParentStack(upIntent)
                            // Navigate up to the closest parent
                            .startActivities();
                } else {
                    // This activity is part of this app's task, so simply
                    // navigate up to the logical parent activity.*/
                Log.d(TAG, "case android.R.id.home: ");
                NavUtils.navigateUpTo(this, upIntent);
//                upIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(upIntent);
//                finish();

//                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTitleChanged() {
//        titleChanged = true;
//        upIntent.putExtra(ACTION_TITLE_CHANGED, titleChanged);
        upIntent.setAction(ACTION_TITLE_CHANGED);
        Log.d(TAG, "onTitleChange");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }
}
