package net.tietema.bang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import com.google.inject.Inject;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.squareup.otto.Subscribe;
import net.tietema.bang.event.NewIncomingMessageEvent;
import net.tietema.bang.event.NewOutgoingMessageEvent;
import net.tietema.bang.event.SettingsChangedEvent;
import net.tietema.bang.model.Contact;
import net.tietema.bang.model.LocalMessage;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import roboguice.service.RoboService;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * @author jeroen
 */
public class XmppService extends RoboService implements ConnectionListener, ChatManagerListener, MessageListener, RosterListener {

    private static final String TAG = "XmppService";

    @Inject
    private SharedPreferences preferences;

    private BangApplication application;
    private DatabaseOpenHelper databaseOpenHelper;

    private Connection connection;
    private Thread connectionThread;
    private Handler mainThreadHandler = new Handler();

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        application = (BangApplication) getApplication();
        application.register(this);

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

        application.unregister(this);

        OpenHelperManager.releaseHelper();

        if (connection != null && connection.isConnected())
            connection.disconnect();
    }

    private void startConnection() {
        final String username = preferences.getString(Const.EMAIL, "");
        final String password = preferences.getString(Const.PASSWORD, "");

        if (username == null || password == null) {
            return;
        }

        connection = null;

        AndroidConnectionConfiguration connConfig = new AndroidConnectionConfiguration("gabbler.de", 5222, "gabbler.de");
        connConfig.setSASLAuthenticationEnabled(true);
        connConfig.setDebuggerEnabled(true);
        connection = new XMPPConnection(connConfig);

        connectionThread = new Thread(){
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

        // Create a new presence. Pass in false to indicate we're unavailable.
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("Testing BANG!");
        // Send the packet (assume we have a Connection instance called "con").
        connection.sendPacket(presence);
    }

    private void reStartConnection() {
        if (connection != null && connection.isConnected())
            connection.disconnect();

        startConnection();
    }

    @Override
    public void connectionClosed() {
        Log.i(TAG, "connectionClosed");
    }

    @Override
    public void connectionClosedOnError(Exception e) {
        Log.i(TAG, "connectionClosedOnError: " + e.getMessage());
    }

    @Override
    public void reconnectingIn(int seconds) {
        Log.i(TAG, "reconnectingIn: " + seconds + "s");
    }

    @Override
    public void reconnectionSuccessful() {
        Log.i(TAG, "reconnectionSuccessful");
    }

    @Override
    public void reconnectionFailed(Exception e) {
        Log.i(TAG, "reconnectionFailed: " + e.getMessage());
    }

    @Override
    public void chatCreated(Chat chat, boolean createdLocally) {
        Log.i(TAG, "chatCreated");
        if (!createdLocally) // is this needed ? - Jeroen
            chat.addMessageListener(this);
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        Log.i(TAG, "processMessage");

        try {
            Dao<Contact, String> contactsDao = databaseOpenHelper.getDao(Contact.class);

            final Contact contact = contactsDao.queryForId(Contact.getEmailFromParticipant(chat.getParticipant()));

            if (contact == null)
                return; // ignore this unkown user

            LocalMessage lm = new LocalMessage();
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
                    application.post(new NewIncomingMessageEvent(contact.getEmail()));
                }
            });

        } catch (SQLException e) {
            throw new android.database.SQLException(e.getMessage(), e);
        }
    }

    /**
     * This method syncs the entire roster to the database
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
            throw new android.database.SQLException(e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, "Exception caught. (Error in batch task?)");
            throw new RuntimeException(e);
        }

    }

    /**
     * This tries to send out all pending messages
     * @param event
     */
    @Subscribe
    public void sendMessages(NewOutgoingMessageEvent event){
        Log.i(TAG, "sendMessages");
        if (connection == null || !connection.isConnected())
            return;

        // send all pending messages in the datastore
        try {
            Dao<LocalMessage, Integer> messageDao = databaseOpenHelper.getDao(LocalMessage.class);
            List<LocalMessage> messages = messageDao.queryForEq("status", LocalMessage.STATUS_PENDING);
            for (LocalMessage message : messages) {
                // TODO: this generates a new threadID everytime. This may anoy other clients
                Chat chat = connection.getChatManager().createChat(message.getContact().getEmail(), this);
                chat.sendMessage(message.getBody());
                message.setStatus(LocalMessage.STATUS_SENT);
                messageDao.update(message);
            }
            // We should signal an event that the messages are succesfully sent

        } catch (SQLException e) {
            throw new android.database.SQLException("SQL error", e);
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
}
