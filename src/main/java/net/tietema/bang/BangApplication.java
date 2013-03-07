package net.tietema.bang;

import android.app.Application;
import android.content.Intent;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.squareup.otto.Bus;
import net.tietema.bang.model.Contact;
import net.tietema.bang.model.LocalMessage;

import java.sql.SQLException;
import java.util.Date;

/**
 * @author Jeroen Tietema <jeroen@tietema.net>
 */
public class BangApplication extends Application {

    private DatabaseOpenHelper openHelper;

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


    public DatabaseOpenHelper getOpenHelper() {
        return openHelper;
    }
}
