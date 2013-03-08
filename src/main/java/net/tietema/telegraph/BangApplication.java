package net.tietema.telegraph;

import android.app.Application;
import android.content.Intent;
import com.squareup.otto.Bus;

/**
 * @author Jeroen Tietema <jeroen@tietema.net>
 */
public class BangApplication extends Application {

    private Bus bus = new Bus();

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(getApplicationContext(), XmppService.class));
    }

    public void post(Object event){
        bus.post(event);
    }

    public void register(Object object){
        bus.register(object);
    }

    public void unregister(Object object) {
        bus.unregister(object);
    }

}
