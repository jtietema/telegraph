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

package net.tietema.telegraph;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.crittercism.app.Crittercism;
import com.google.inject.Inject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.tietema.telegraph.event.NewIncomingMessageEvent;
import net.tietema.telegraph.event.NewOutgoingMessageEvent;
import net.tietema.telegraph.event.SettingsChangedEvent;
import net.tietema.telegraph.model.Contact;
import net.tietema.telegraph.model.LocalMessage;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import roboguice.service.RoboService;
import roboguice.util.Ln;

/**
 * @author jeroen
 */
public class XmppService extends RoboService
        implements ConnectionListener, ChatManagerListener, MessageListener, RosterListener {

    private static final String TAG = "XmppService";
    private static final int STATUS_NOTIFICATION = 1337;
    private static final long CONNECTION_CHECK_INTERVAL = 20 * 1000L;

    private static final int DEFAULT_PORT = 5222;
    private static final int GOOGLE_MESSAGE_PROIRITY = 24;

    @Inject
    private SharedPreferences preferences;
    @Inject
    private NotificationManager notificationManager;

    @Inject
    private Bus eventBus;
    private DatabaseOpenHelper databaseOpenHelper;

    private Connection connection;
    private Thread connectionThread;
    private Handler mainThreadHandler = new Handler();

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        Crittercism.initialize(getApplicationContext(), Const.CRITTERCISM);

        eventBus.register(this);
        updateNotification(false);

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);

        SmackConfiguration.setKeepAliveInterval(-1);

        startConnection();
    }

    @Subscribe
    public void onSettingsChanged(SettingsChangedEvent event) {
        Log.i(TAG, "SettingsChanged");
        reStartConnection();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        eventBus.unregister(this);

        OpenHelperManager.releaseHelper();

        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }

        notificationManager.cancel(STATUS_NOTIFICATION);
    }

    private void startConnection() {
        final String username = preferences.getString(Const.EMAIL, null);
        final String password = preferences.getString(Const.PASSWORD, null);

        if (username == null || password == null) {
            return;
        }

        connection = null;

        AndroidConnectionConfiguration connConfig =
            new AndroidConnectionConfiguration("talk.google.com", DEFAULT_PORT, "gmail.com");
        connConfig.setDebuggerEnabled(true);
        connConfig.setReconnectionAllowed(true);
        connection = new XMPPConnection(connConfig);

        connectionThread = new Thread() {
            @Override
            public void run() {
                try {
                    connection.connect();
                    connection.login(username, password);
                    finishConnecting();

                } catch (XMPPException e) {
                    Log.e(TAG, "XMPP Exception", e);
                    if (connection != null) {
                        connection.disconnect();
                    }
                }
            }
        };

        connectionThread.start();
    }

    private void finishConnecting() {
        Log.i(TAG, "finishConnecting");
        connection.addConnectionListener(this);
        connection.getChatManager().addChatListener(this);
        connection.getRoster().addRosterListener(this);

        syncContacts();

        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Testing Telegraph!");
        presence.setPriority(GOOGLE_MESSAGE_PROIRITY); // Google uses prio 24 on clients
        connection.sendPacket(presence);

        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                updateNotification(true);
            }
        });
        mainThreadHandler.postDelayed(new ConnectionChecker(), CONNECTION_CHECK_INTERVAL);
    }

    private void reStartConnection() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
        }

        startConnection();
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "connectionClosed");
        updateNotification(false);
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.i(TAG, "connectionClosedOnError: " + e.getMessage());
        updateNotification(false);
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.i(TAG, "reconnectingIn: " + seconds + "s");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(TAG, "reconnectionSuccessful");
        updateNotification(true);
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.i(TAG, "reconnectionFailed: " + e.getMessage());
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.i(TAG, "chatCreated");
        if (!createdLocally) { // is this needed ? - Jeroen
            chat.addMessageListener(this);
        }
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.i(TAG, "processMessage");
        if (TextUtils.isEmpty(message.getBody())) {
            return; // ignore empty messages
        }

        try {
            Dao<Contact, String> contactsDao = databaseOpenHelper.getDao(Contact.class);

            final String email = Contact.getEmailFromParticipant(chat.getParticipant());
            Ln.i("Contact email: " + email);
            final Contact contact = contactsDao.queryForId(email);

            if (contact == null) {
                Ln.e("Unkown user. How is this possible???");
                return; // ignore this unknown user
            }

            final LocalMessage lm = new LocalMessage();
            lm.setContact(contact);
            lm.setBody(message.getBody());
            lm.setStatus(LocalMessage.STATUS_RECEIVED);
            lm.setTime(new Date());

            Dao<LocalMessage, Integer> messagesDao = databaseOpenHelper.getDao(LocalMessage.class);
            messagesDao.create(lm);

            // this callback isn´t called from the mainThread so make sure the event is posted there
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    eventBus.post(new NewIncomingMessageEvent(contact.getEmail(), lm));
                }
            });

        } catch (SQLException e) {
            android.database.SQLException ex = new android.database.SQLException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }
    }

    /**
     * This method syncs the entire roster to the database.
     */
    private void syncContacts() {
        try {
            final Dao<Contact, Integer> contactDao = databaseOpenHelper.getDao(Contact.class);

            // retrieve the contacts
            List<Contact> contactsFromDatabase = contactDao.queryForAll();

            // index the contacts by id for easy retrieval
            Map<String, Contact> indexedContacts = new HashMap<String, Contact>(contactsFromDatabase.size());
            for (Contact c : contactsFromDatabase) {
                indexedContacts.put(c.getEmail(), c);
            }

            // keep track of whats new, updated or removed
            final List<Contact> newContacts     = new ArrayList<Contact>();
            final List<Contact> updatedContacts = new ArrayList<Contact>();
            final List<Contact> removedContacts = contactsFromDatabase; // we move them to updated once we touch them.

            // update existing contacts or add new ones
            for (RosterEntry entry : connection.getRoster().getEntries()) {
                Contact contact = indexedContacts.get(entry.getUser());

                if (contact == null) {
                    // contact is new
                    contact = new Contact();
                    contact.setEmail(entry.getUser());
                    contact.setName(entry.getName());
                    newContacts.add(contact);
                } else {
                    // contact exists
                    contact.setName(entry.getName());
                    removedContacts.remove(contact); // make sure it is not removed at the end of sync
                    updatedContacts.add(contact);
                }
            }

            // execute all updates, inserts and deletes in a batch
            contactDao.callBatchTasks(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    for (Contact contact : newContacts) {
                        contactDao.create(contact);
                    }
                    for (Contact contact : updatedContacts) {
                        contactDao.update(contact);
                    }
                    contactDao.delete(removedContacts);
                    return null;
                }
            });

        } catch (SQLException e) {
          RuntimeException ex = new android.database.SQLException(e.getMessage());
          ex.initCause(e);
          throw ex;
        } catch (Exception e) {
            Log.e(TAG, "Exception caught. (Error in batch task?)");
            throw new RuntimeException(e);
        }

    }

    /**
     * Tries to send out all pending messages.
     * @param event
     */
    @Subscribe
    public void sendMessages(NewOutgoingMessageEvent event) {
        Log.i(TAG, "sendMessages");
        if (connection == null || !connection.isConnected()) {
            return;
        }

        // send all pending messages in the datastore
        try {
            Dao<LocalMessage, Integer> messageDao = databaseOpenHelper.getDao(LocalMessage.class);
            List<LocalMessage> messages = messageDao.queryForEq("status", LocalMessage.STATUS_PENDING);
            for (LocalMessage message : messages) {
                // FIXME: this generates a new threadID everytime. This may anoy other clients
                Chat chat = connection.getChatManager().createChat(message.getContact().getEmail(), this);
                chat.sendMessage(message.getBody());
                message.setStatus(LocalMessage.STATUS_SENT);
                messageDao.update(message);
            }
            // TODO: We should signal an event that the messages are succesfully sent

        } catch (SQLException e) {
            android.database.SQLException ex = new android.database.SQLException("SQL error");
            ex.initCause(e);
            throw ex;
        } catch (XMPPException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void entriesAdded(Collection<String> addresses) {
        Log.i(TAG, "entriesAdded");
    }

    @Override
    public void entriesUpdated(Collection<String> addresses) {
        Log.i(TAG, "entriesUpdated");
    }

    @Override
    public void entriesDeleted(Collection<String> addresses) {
        Log.i(TAG, "entriesDeleted");
    }

    @Override
    public void presenceChanged(Presence presence) {
        Log.i(TAG, "presenceChanged");
    }

  /**
   * Update the connection status notification with the current status.
   * @param connected
   */
    private void updateNotification(boolean connected) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setOngoing(true);
        builder.setContentTitle("Telegraph");
        if (connected) {
            builder.setSmallIcon(R.drawable.rating_good);
            builder.setTicker("Connected");
            builder.setContentText("Connected");
        } else {
            builder.setSmallIcon(R.drawable.rating_bad);
            builder.setTicker("Disconnected");
            builder.setContentText("Disconnected");
        }
        Notification notification = builder.getNotification();
        notificationManager.notify(STATUS_NOTIFICATION, notification);
    }


    class ConnectionChecker implements Runnable {

        @Override
        public void run() {
            if (connection == null || !connection.isConnected()) {
                Ln.i("Connection is dead, trying to reconnect...");
                reStartConnection();
                mainThreadHandler.postDelayed(this, CONNECTION_CHECK_INTERVAL);
            }
        }
    }
}
