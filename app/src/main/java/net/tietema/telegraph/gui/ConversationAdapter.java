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
import android.widget.ImageView;
import android.widget.TextView;

import net.tietema.telegraph.R;
import net.tietema.telegraph.model.Contact;
import net.tietema.telegraph.model.LocalMessage;

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;

public class ConversationAdapter extends BaseAdapter {

    private SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm");
    private LocalMessage[] messages;

    public ConversationAdapter(Contact contact) {
        setMessages(contact);
    }

    public void setMessages(Contact contact) {
        messages = contact.getMessages().toArray(new LocalMessage[contact.getMessages().size()]);
        ArrayUtils.reverse(messages);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.length;
    }

    @Override
    public LocalMessage getItem(int position) {
        return messages[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.conversation_message_list_item, parent, false);
        }

        // The current message we are displaying
        LocalMessage currentMessage = getItem(position);

        // Contact picture
        // @TODO Get picture from contact

        // Time
        TextView time = (TextView) convertView.findViewById(R.id.time);
        time.setText(timeFormatter.format(currentMessage.getTime()));

        // Message
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText(currentMessage.getBody());

        // Contact picture
        if (messages.length > 0 && position > 0) {
            // Show/Hide the contact picture based on the previous message
            LocalMessage prevMessage = this.messages[position - 1];
            if (prevMessage.getStatus() == currentMessage.getStatus()) {
                // Hide icon
                ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
                pic.setVisibility(View.INVISIBLE);
            } else {
                // Show icon
                ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
                pic.setVisibility(View.VISIBLE);
            }
        } else {
            // Show icon
            ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
            pic.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
