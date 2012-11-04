package net.tietema.bang;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.squareup.otto.Subscribe;
import roboguice.inject.InjectView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends RoboSherlockActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "MainActivity";

    @InjectView(R.id.list)
    private ListView listView;

    private DatabaseOpenHelper databaseOpenHelper;
    private BangApplication application;
    private ConversationListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.main);

        application = (BangApplication) getApplication();

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);

        adapter = new ConversationListAdapter();
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        refreshAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        application.register(this);
        refreshAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        application.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm");

    /**
     * Adapter for displaying a list of conversations. There is one conversation per Contact.
     * @author  Jeroen Tietema <jeroen@tietema.net>
     * @author  Mattijs Hoitink <mattijs@monkeyandmachine.com>
     */
    private class ConversationListAdapter extends BaseAdapter {

        private List<Contact> contacts = new ArrayList<Contact>();

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return contacts.size();
        }

        @Override
        public Contact getItem(int position) {
            return contacts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_list_item, null);
            }

            // Get the current contact
            Contact contact = getItem(position);

            // Contact Name
            TextView name = (TextView) convertView.findViewById(R.id.contact_name);
            name.setText(contact.getName());

            // We  assume  all the selected contacts have at least one message
            Message[] messages = contact.getMessages().toArray(new Message[contact.getMessages().size()]);

            // Date
            TextView time = (TextView) convertView.findViewById(R.id.time);
            time.setText(timeFormatter.format(messages[0].getTime()));

            // Message
            TextView message = (TextView) convertView.findViewById(R.id.message);
            message.setText(messages[0].getBody());

            return convertView;
        }
    }

}

