package net.tietema.bang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.squareup.otto.Subscribe;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.sql.SQLException;
import java.util.List;

@ContentView(R.layout.main)
public class MainActivity extends RoboSherlockActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "MainActivity";

    @InjectView(R.id.list)
    private ListView listView;

    private DatabaseOpenHelper databaseOpenHelper;
    private BangApplication application;
    @Inject
    private ConversationListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");

        application = (BangApplication) getApplication();

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        refreshAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        application.register(this);
        refreshAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        application.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (databaseOpenHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseOpenHelper = null;
        }
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent event) {
        refreshAdapter();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent openThread = new Intent(this, ConversationActivity.class);
        openThread.putExtra("contact", ((Contact) parent.getItemAtPosition(position)).getEmail());
        startActivity(openThread);
    }

    private void refreshAdapter() {
        try {
            // we only select contacts with messages
            Dao<Contact, String> contactDao = databaseOpenHelper.getDao(Contact.class);
            Dao<Message, Integer> messagesDao = databaseOpenHelper.getDao(Message.class);

            QueryBuilder<Message, Integer> messagesQb = messagesDao.queryBuilder();
            messagesQb.groupBy("contact_id");
            QueryBuilder<Contact, String> contactQb = contactDao.queryBuilder();
            List<Contact> contacts = contactQb.join(messagesQb).query();


            adapter.setContacts(contacts);

        } catch (SQLException e) {
            throw new android.database.SQLException("Error getting conversations", e);
        }
    }

}

