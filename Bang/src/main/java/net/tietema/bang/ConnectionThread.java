package net.tietema.bang;

import android.content.Context;
import android.util.Log;
import org.jivesoftware.smack.*;

/**
 * @author jeroen
 */
public class ConnectionThread extends Thread {

    private static final String TAG = "ConnectionThread";

    private boolean running = true;

    private String username, password;

    public ConnectionThread(Context appContext, String username, String password) {
        SmackAndroid.init(appContext);
        this.username   = username;
        this.password   = password;
    }

    @Override
    public void run() {
        Log.i(TAG, "Setting up connection");

        if (username == null || password == null) {
            return;
        }

        Connection connection = null;

        try {
            AndroidConnectionConfiguration connConfig = new AndroidConnectionConfiguration("gabbler.de", 5222, "gabbler.de");
            connConfig.setSASLAuthenticationEnabled(true);
            connConfig.setDebuggerEnabled(true);
            connection = new XMPPConnection(connConfig);

            connection.connect();
            connection.login(username, password);
            while(running) {
                sleep(3000);
                Log.i(TAG, "Sleeping some more");
            }
        } catch (InterruptedException e) {
            Log.i(TAG, "Finishing ...");
        } catch (XMPPException e) {
            Log.e(TAG, "XMPP Exception", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
