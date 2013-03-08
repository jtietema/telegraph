package net.tietema.telegraph.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import net.tietema.telegraph.DatabaseOpenHelper;
import net.tietema.telegraph.R;
import net.tietema.telegraph.model.Contact;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

import java.sql.SQLException;
import java.util.List;

/**
 * @author jeroen
 */
@ContentView(R.layout.new_conversation)
public class NewConversationActivity extends RoboSherlockActivity implements AdapterView.OnItemClickListener {

    private DatabaseOpenHelper  databaseOpenHelper;
    private ContactAdapter      contactAdapter;

    @InjectView(R.id.list)
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        databaseOpenHelper = OpenHelperManager.getHelper(this, DatabaseOpenHelper.class);
        contactAdapter = new ContactAdapter();
        listView.setAdapter(contactAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshAdapter();
    }

    private void refreshAdapter() {
        try {
            Dao<Contact, String> contactsDao = databaseOpenHelper.getDao(Contact.class);
            QueryBuilder<Contact, String> qb = contactsDao.queryBuilder();
            qb.orderBy("name", true);
            List<Contact> contacts = qb.query();
            contactAdapter.setContacts(contacts);
        } catch (SQLException e) {
            throw new android.database.SQLException("Error getting contacts", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OpenHelperManager.releaseHelper();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Contact contact = contactAdapter.getItem(position);
        Intent startConversation = new Intent(this, ConversationActivity.class);
        startConversation.putExtra("contact", contact.getEmail());
        startActivity(startConversation);
        finish();
    }
}
