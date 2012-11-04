package net.tietema.bang;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import roboguice.service.RoboService;

import javax.persistence.TableGenerator;
import java.lang.Thread;

/**
 * @author jeroen
 */
public class XmppService extends RoboService {

    private static final String TAG = "XmppService";

    private ConnectionThread conn = new ConnectionThread();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        conn.start();
    }

    private class ConnectionThread extends Thread {

        @Override
        public void run() {
            try {
                while(true) {
                    sleep(3000);
                    Log.d(TAG, "Sleeping some more");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }
}
