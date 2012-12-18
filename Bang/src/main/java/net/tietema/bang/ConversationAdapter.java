package net.tietema.bang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;

public class ConversationAdapter extends BaseAdapter {

    private Message[] messages;
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("dd-MM-yy HH:mm");

    public ConversationAdapter(Contact contact) {
        setMessages(contact);
    }

    public void setMessages(Contact contact) {
        messages = contact.getMessages().toArray(new Message[contact.getMessages().size()]);
        ArrayUtils.reverse(messages);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.length;
    }

    @Override
    public Message getItem(int position) {
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

        Message m = getItem(position);

        // Contact picture
        // @TODO Get picture from contact

        // Time
        TextView time = (TextView) convertView.findViewById(R.id.time);
        time.setText(timeFormatter.format(m.getTime()));

        // Message
        TextView message = (TextView) convertView.findViewById(R.id.message);
        message.setText(m.getBody());

        return convertView;
    }
}
