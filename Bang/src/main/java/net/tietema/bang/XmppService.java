package net.tietema.bang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;
import com.google.inject.Inject;
import com.squareup.otto.Subscribe;
import roboguice.service.RoboService;
import org.jivesoftware.smackx.ServiceDiscoveryManager;

/**
 * @author jeroen
 */
public class XmppService extends RoboService {

    private static final String TAG = "XmppService";

    @Inject
    private SharedPreferences preferences;

    private BangApplication application;

    private ConnectionThread conn;

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();

        application = (BangApplication) getApplication();
        application.register(this);

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

        if (conn != null && conn.isAlive())
            conn.interrupt();
    }

    private void startConnection(){
        String username = preferences.getString(Const.EMAIL, "");
        String password = preferences.getString(Const.PASSWORD, "");
        conn = new ConnectionThread(getApplicationContext(), username, password);

        conn.start();
    }

    private void reStartConnection() {
        if (conn != null && conn.isAlive())
            conn.interrupt();

        startConnection();
    }
}
