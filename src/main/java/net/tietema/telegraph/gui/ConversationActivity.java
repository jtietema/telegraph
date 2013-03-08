package net.tietema.telegraph.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.squareup.otto.Subscribe;
import net.tietema.telegraph.BangApplication;
import net.tietema.telegraph.DatabaseOpenHelper;
import net.tietema.telegraph.R;
import net.tietema.telegraph.event.NewIncomingMessageEvent;
import net.tietema.telegraph.event.NewOutgoingMessageEvent;
import net.tietema.telegraph.model.Contact;
import net.tietema.telegraph.model.LocalMessage;
import org.apache.commons.lang3.ArrayUtils;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author jeroen
 */
@ContentView(R.layout.conversation)
public class ConversationActivity extends RoboSherlockActivity implements View.OnClickListener {

    private Contact contact;
    private DatabaseOpenHelper databaseOpenHelper;
    private BangApplication application;
    private ConversationAdapter adapter;

    @InjectView(R.id.list)      private ListView listView;
    @InjectView(R.id.compose)   private TextView message;
    @InjectView(R.id.send)      private Button send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String email = intent.getStringExtra("contact");

        application = (BangApplication) getApplication();

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
            listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        } catch (SQLException e) {
            throw new android.database.SQLException("Error retrieving contact", e);
        }

        send.setOnClickListener(this);

        // prevent the keyboard from popping up automatically
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    @Override
    protected void onResume() {
        super.onResume();
        application.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        application.unregister(this);
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
        } catch (SQLException e) {
            throw new android.database.SQLException("Error refreshing contact", e);
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
            application.post(new NewOutgoingMessageEvent(contact.getEmail()));
            message.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(message.getWindowToken(), 0);
        } catch (SQLException e) {
            throw new android.database.SQLException("Exception saving message", e);
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
            if (messages.length > 0 && position > 0) {
                // Show/Hide the contact picture based on the previous message
                LocalMessage prevMessage = this.messages[position - 1];
                if (prevMessage.getContact().equals(currentMessage.getContact())) {
                    // Hide icon
                    ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
                    pic.setVisibility(View.INVISIBLE);
                } else {
                    // Show icon
                    ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
                    pic.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }
    }
}
