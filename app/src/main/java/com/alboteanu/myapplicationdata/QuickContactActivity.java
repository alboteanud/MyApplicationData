package com.alboteanu.myapplicationdata;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import static com.alboteanu.myapplicationdata.ContactEditorActivity.EXTRA_CONTACT_KEY;

public class QuickContactActivity extends BaseActivity {
    String contactKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        contactKey = getIntent().getStringExtra(EXTRA_CONTACT_KEY);
//        initViews();
//        linkFixedFieldsToFirebase();
//        linkVariableFieldsToFirebase();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_quick_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_contact) {
            createDeleteDialogAlert(contactKey);
            return true;
        }else if (id == R.id.action_edit) {
            goToEditActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToEditActivity() {
        Intent intent = new Intent(QuickContactActivity.this, ContactEditorActivity.class);
        intent.putExtra(ContactEditorActivity.EXTRA_CONTACT_KEY, contactKey);
        startActivity(intent);
    }


}
