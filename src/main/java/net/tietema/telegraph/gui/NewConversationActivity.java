/*
 * Telegraph is an online messaging app with strong focus on privacy
 * Copyright (C) 2013 Jeroen Tietema <jeroen@tietema.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tietema.telegraph.gui;

import com.crittercism.app.Crittercism;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;

import net.tietema.telegraph.Const;
import net.tietema.telegraph.DatabaseOpenHelper;
import net.tietema.telegraph.R;
import net.tietema.telegraph.model.Contact;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.sql.SQLException;
import java.util.List;

import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

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
        Crittercism.init(getApplicationContext(), Const.CRITTERCISM);

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
            android.database.SQLException ex = new android.database.SQLException("Error getting contacts");
            ex.initCause(e);
            throw ex;
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
        startConversation.putExtra(Const.EMAIL, contact.getEmail());
        startActivity(startConversation);
        finish();
    }
}
