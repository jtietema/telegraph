package net.tietema.bang.gui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.tietema.bang.R;
import net.tietema.bang.model.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jeroen
 */
public class ContactAdapter extends BaseAdapter {

    private List<Contact> contacts = new ArrayList<Contact>();

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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item, null);
        }

        Contact contact = getItem(position);

        TextView nameView = (TextView) convertView.findViewById(R.id.name);
        String name = contact.getName();
        if (name == null || name.equals("")) {
            name = contact.getEmail();
        }
        nameView.setText(name);
        return convertView;
    }
}
