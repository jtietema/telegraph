/*
 * Telegraph is an online messaging app with strong focus on privacy
 * Copyright (C) 2013 Jeroen Tietema <jeroen@tietema.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tietema.telegraph.gui;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.tietema.telegraph.Const;
import net.tietema.telegraph.DatabaseOpenHelper;
import net.tietema.telegraph.R;
import net.tietema.telegraph.XmppService;
import net.tietema.telegraph.event.NewIncomingMessageEvent;
import net.tietema.telegraph.event.NewOutgoingMessageEvent;
import net.tietema.telegraph.model.Contact;
import net.tietema.telegraph.model.LocalMessage;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.main)
public class MainActivity extends RoboSherlockActivity implements AdapterView.OnItemClickListener {

    private static String TAG = "MainActivity";

    @InjectView(R.id.list)
    private ListView listView;

    private DatabaseOpenHelper databaseOpenHelper;
    @Inject
    private Bus eventBus;
    private ConversationListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), Const.CRITTERCISM);
        Log.i(TAG, "onCreate");

        adapter = new ConversationListAdapter();

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        eventBus.register(this);
        refreshAdapter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        eventBus.unregister(this);
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
    public void onNewMessage(NewOutgoingMessageEvent event) {
        refreshAdapter();
    }

    @Subscribe
    public void onNewMessage(NewIncomingMessageEvent event) {
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
        if (item.getItemId() == R.id.menu_save){
            startActivity(new Intent(this, SettingsActivity.class));
        } else if (item.getItemId() == R.id.menu_new) {
            startActivity(new Intent(this, NewConversationActivity.class));
        } else if (item.getItemId() == R.id.menu_toggle_connection) {
            stopService(new Intent(this, XmppService.class));
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent openThread = new Intent(this, ConversationActivity.class);
        openThread.putExtra(Const.EMAIL, ((Contact) parent.getItemAtPosition(position)).getEmail());
        startActivity(openThread);
    }

    private void refreshAdapter() {
        try {
            // we only select contacts with messages
            Dao<Contact, String> contactDao = databaseOpenHelper.getDao(Contact.class);
            Dao<LocalMessage, Integer> messagesDao = databaseOpenHelper.getDao(LocalMessage.class);

            QueryBuilder<LocalMessage, Integer> messagesQb = messagesDao.queryBuilder();
            messagesQb.groupBy("contact_id");
            messagesQb.orderBy("time", false);
            QueryBuilder<Contact, String> contactQb = contactDao.queryBuilder();
            List<Contact> contacts = contactQb.join(messagesQb).query();


            adapter.setContacts(contacts);

        } catch (SQLException e) {
            android.database.SQLException ex = new android.database.SQLException("Error getting conversations");
            ex.initCause(e);
            throw ex;
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
            LocalMessage[] messages = contact.getMessages().toArray(new LocalMessage[contact.getMessages().size()]);

            // Date
            TextView time = (TextView) convertView.findViewById(R.id.time);
            time.setText(DateUtils.getRelativeDateTimeString(MainActivity.this, messages[0].getTime().getTime(),
                    DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));

            // Message
            TextView message = (TextView) convertView.findViewById(R.id.message);

            message.setText(messages[0].getBody());

            return convertView;
        }
    }

}

