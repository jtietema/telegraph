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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.crittercism.app.Crittercism;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.tietema.telegraph.Const;
import net.tietema.telegraph.DatabaseOpenHelper;
import net.tietema.telegraph.R;
import net.tietema.telegraph.event.NewIncomingMessageEvent;
import net.tietema.telegraph.event.NewOutgoingMessageEvent;
import net.tietema.telegraph.model.Contact;
import net.tietema.telegraph.model.LocalMessage;

import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

/**
 * @author jeroen
 */
@ContentView(R.layout.conversation)
public class ConversationActivity extends RoboSherlockActivity implements View.OnClickListener {

    private Contact contact;
    private DatabaseOpenHelper databaseOpenHelper;
    @Inject
    private Bus eventBus;
    private ConversationAdapter adapter;

    @InjectView(R.id.list)      private ListView    listView;
    @InjectView(R.id.compose)   private TextView    message;
    @InjectView(R.id.send)      private ImageButton send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crittercism.initialize(getApplicationContext(), Const.CRITTERCISM);

        Intent intent = getIntent();
        String email = intent.getStringExtra(Const.EMAIL);

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);
        try {
            Dao<Contact, String> contactDao = databaseOpenHelper.getDao(Contact.class);
            contact = contactDao.queryForId(email);

            // Set contact name in the main title bar
            ActionBar ab = getSupportActionBar();
            ab.setTitle(contact.getName());
            ab.setDisplayHomeAsUpEnabled(true);

            // Set the view Adapter
            adapter = new ConversationAdapter(contact);
            listView.setAdapter(adapter);
            listView.setSelection(adapter.getCount() - 1);
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        } catch (SQLException e) {
            android.database.SQLException ex = new android.database.SQLException("Error retrieving contact");
            ex.initCause(e);
            throw ex;
        }

        send.setOnClickListener(this);

        // prevent the keyboard from popping up automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OpenHelperManager.releaseHelper();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent goHome = new Intent(this, MainActivity.class);
            goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(goHome);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void newMessages(NewOutgoingMessageEvent event) {
        if (event.getEmail().equals(contact.getEmail())) {
            // message belongs to current conversation, so update UI
            refreshAdapter();
        }
    }

    @Subscribe
    public void newMessages(NewIncomingMessageEvent event) {
        if (event.getEmail().equals(contact.getEmail())) {
            // message belongs to current conversation, so update UI
            refreshAdapter();
        }
    }

    private void refreshAdapter() {
        try {
            Dao<Contact, String> contactDao = databaseOpenHelper.getDao(Contact.class);
            contactDao.refresh(contact);
            adapter.setMessages(contact);
            //listView.setSelection(adapter.getCount() - 1);
        } catch (SQLException e) {
            android.database.SQLException ex = new android.database.SQLException("Error refreshing contact");
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public void onClick(View v) {
        if (message.getText().length() < 1) {
            Toast.makeText(this, R.string.empty_message, Toast.LENGTH_LONG).show();
            return;
        }

        LocalMessage newMessage = new LocalMessage();
        newMessage.setContact(contact);
        newMessage.setStatus(LocalMessage.STATUS_PENDING);
        newMessage.setBody(message.getText().toString());
        newMessage.setTime(new Date());
        try {
            Dao<LocalMessage, Integer> messageDao = databaseOpenHelper.getDao(LocalMessage.class);
            messageDao.create(newMessage);
            eventBus.post(new NewOutgoingMessageEvent(contact.getEmail()));
            message.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
        } catch (SQLException e) {
            android.database.SQLException ex = new android.database.SQLException("Exception saving message");
            ex.initCause(e);
            throw ex;
        }


    }

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm");

    private class ConversationAdapter extends BaseAdapter {

        private LocalMessage[] messages;

        public ConversationAdapter(Contact contact) {
            setMessages(contact);
        }

        public void setMessages(Contact contact) {
            messages = contact.getMessages().toArray(new LocalMessage[contact.getMessages().size()]);
            ArrayUtils.reverse(messages);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return contact.getMessages().size();
        }

        @Override
        public LocalMessage getItem(int position) {
            return messages[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(R.layout.conversation_message_list_item, null);
            }

            // The current message we are displaying
            LocalMessage currentMessage = getItem(position);

            // Contact picture
            // @TODO Get picture from contact

            // Time
            TextView time = (TextView) convertView.findViewById(R.id.time);
            time.setText(timeFormatter.format(currentMessage.getTime()));

            // Message
            TextView message = (TextView) convertView.findViewById(R.id.message);
            message.setText(currentMessage.getBody());

            // Contact picture
            ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
            if (messages.length > 0 && position > 0) {
                // Show/Hide the contact picture based on the previous message
                LocalMessage prevMessage = this.messages[position - 1];
                if ((prevMessage.isMine() && currentMessage.isMine())
                        || (!prevMessage.isMine() && !currentMessage.isMine())) {
                    // Hide icon

                    pic.setVisibility(View.INVISIBLE);
                } else {
                    // Show icon
                    pic.setVisibility(View.VISIBLE);
                }
            } else {
                pic.setVisibility(View.VISIBLE);
            }

            return convertView;
        }
    }
}
