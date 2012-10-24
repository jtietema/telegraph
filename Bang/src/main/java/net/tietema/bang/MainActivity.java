package net.tietema.bang;

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
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.stmt.QueryBuilder;
import roboguice.inject.InjectView;

import java.sql.SQLException;
import java.util.List;

public class MainActivity extends RoboSherlockActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "MainActivity";

    @InjectView(R.id.list)
    private ListView listView;

    private DatabaseOpenHelper databaseOpenHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
        setContentView(R.layout.main);

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);

        try {
            // we only select contacts with messages
            Dao<Contact, String> contactDao = databaseOpenHelper.getDao(Contact.class);
            Dao<Message, Integer> messagesDao = databaseOpenHelper.getDao(Message.class);

            QueryBuilder<Message, Integer> messagesQb = messagesDao.queryBuilder();
            messagesQb.groupBy("contact_id");
            QueryBuilder<Contact, String> contactQb = contactDao.queryBuilder();
            List<Contact> contacts = contactQb.join(messagesQb).query();

            listView.setAdapter(new MessageThreadsAdapter(contacts));
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        listView.setOnItemClickListener(this);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseOpenHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseOpenHelper = null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private class MessageThreadsAdapter extends BaseAdapter {

        private List<Contact> contacts;

        public MessageThreadsAdapter(List<Contact> contacts) {
            this.contacts = contacts;
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
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.thread_list_item, null);
            }
            Contact contact = getItem(position);
            TextView name = (TextView) convertView.findViewById(R.id.contact_name);
            TextView message = (TextView) convertView.findViewById(R.id.message);
            name.setText(contact.getName());
            // we  assume  all the selected contacts have at least one message
            Message[] messages = contact.getMessages().toArray(new Message[contact.getMessages().size()]);
            message.setText(messages[0].getBody());
            return convertView;
        }
    }

}

