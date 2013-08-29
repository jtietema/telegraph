package net.tietema.telegraph;

import android.app.Application;
import android.content.Intent;

/**
 * @author Jeroen Tietema <jeroen@tietema.net>
 */
public class BangApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // this object registers on the event bus so we don't need a reference to it
        new NotificationManager(getApplicationContext());

        // fire off the connection service
        startService(new Intent(getApplicationContext(), XmppService.class));
    }

}
