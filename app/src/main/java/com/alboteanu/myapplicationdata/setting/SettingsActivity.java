package com.alboteanu.myapplicationdata.setting;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import com.alboteanu.myapplicationdata.R;

public class SettingsActivity extends AppCompatPreferenceActivity
    implements GeneralPreferenceFragment.OnTitleChangeListener{
    public static final String ACTION_TITLE_CHANGED = "title_changed";
    private boolean titleChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Log.d("tag SettingsActivity", "onCreate()");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
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
                    // navigate up to the logical parent activity.
                    NavUtils.navigateUpTo(this, upIntent);
                    Log.d("tag", "NavUtils.navigateUpTo(this, upIntent)  ");
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTitleChanged() {
        titleChanged = true;
    }
}
