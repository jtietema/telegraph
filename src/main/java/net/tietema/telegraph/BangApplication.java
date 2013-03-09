package net.tietema.telegraph;

import android.app.Application;
import android.content.Intent;
import com.squareup.otto.Bus;

/**
 * @author Jeroen Tietema <jeroen@tietema.net>
 */
public class BangApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(getApplicationContext(), XmppService.class));
    }

}
