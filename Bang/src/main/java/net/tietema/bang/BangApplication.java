package net.tietema.bang;

import android.app.Application;
import android.content.Intent;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.squareup.otto.Bus;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPConnection;

import java.sql.SQLException;
import java.util.Date;

/**
 * @author jeroen
 */
public class BangApplication extends Application {

    private DatabaseOpenHelper openHelper;

    private Bus bus = new Bus();

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(getApplicationContext(), XmppService.class));

        openHelper = OpenHelperManager.getHelper(getApplicationContext(), DatabaseOpenHelper.class);

        // test data
        try {
            Dao<Contact, String> contactDao = openHelper.getDao(Contact.class);

            Contact ronald = new Contact();
            ronald.setEmail("ronald@example.com");
            ronald.setName("Ronald");
            contactDao.create(ronald);

            Contact nikie = new Contact();
            nikie.setEmail("nikie@example.com");
            nikie.setName("Nikie");
            contactDao.create(nikie);

            Dao<Message, Integer> messagesDao = openHelper.getDao(Message.class);

            Message nm = new Message();
            nm.setContact(ronald);
            nm.setBody("Test bericht");
            nm.setTime(new Date());

            messagesDao.create(nm);

        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

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
