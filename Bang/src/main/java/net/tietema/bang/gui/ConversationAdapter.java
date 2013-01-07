package net.tietema.bang.gui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import net.tietema.bang.model.Contact;
import net.tietema.bang.R;
import net.tietema.bang.model.LocalMessage;
import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;

public class ConversationAdapter extends BaseAdapter {

    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm");
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.conversation_message_list_item, null);
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
            if (prevMessage.getContact().equals(currentMessage.getContact())) {
                // Hide icon
                ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
                pic.setVisibility(View.INVISIBLE);
            }
            else {
                // Show icon
                ImageView pic = (ImageView) convertView.findViewById(R.id.contact_picture);
                pic.setVisibility(View.VISIBLE);
            }
        }

        return convertView;
    }
}
