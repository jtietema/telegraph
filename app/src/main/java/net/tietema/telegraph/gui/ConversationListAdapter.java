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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.tietema.telegraph.R;
import net.tietema.telegraph.model.Contact;
import net.tietema.telegraph.model.LocalMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of conversations. There is one conversation per Contact.
 * @author  Jeroen Tietema <jeroen@tietema.net>
 * @author  Mattijs Hoitink <mattijs@monkeyandmachine.com>
 */
public class ConversationListAdapter extends BaseAdapter {

    private List<Contact> contacts = new ArrayList<Contact>();
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm");

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Contact getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_list_item, null);
        }

        // Get the current contact
        Contact contact = getItem(position);

        // Contact Name
        TextView name = (TextView) convertView.findViewById(R.id.contact_name);
        name.setText(contact.getName());

        // We  assume  all the selected contacts have at least one message
        LocalMessage[] messages = contact.getMessages().toArray(new LocalMessage[contact.getMessages().size()]);

        // Date
        TextView time = (TextView) convertView.findViewById(R.id.time);
        time.setText(timeFormatter.format(messages[0].getTime()));

        // Message
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText(messages[0].getBody());

        return convertView;
    }
}

