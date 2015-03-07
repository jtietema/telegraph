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

package net.tietema.telegraph;

import com.google.inject.Inject;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.tietema.telegraph.event.NewIncomingMessageEvent;
import net.tietema.telegraph.gui.ConversationActivity;

import android.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import roboguice.RoboGuice;

/**
 * This class listens on the event bus and generates GUI notifications for the appropriate events
 *
 * @author jeroen
 */
public class NotificationManager {

    @Inject
    private android.app.NotificationManager notificationManager;

    @Inject
    private Bus eventBus;

    private Context context;

    public NotificationManager(Context context) {
        RoboGuice.injectMembers(context, this);
        eventBus.register(this);
        this.context = context;
    }

    @Subscribe
    public void onNewIncomingMessage(NewIncomingMessageEvent event) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("New message");
        builder.setContentText(event.getMessage().getContact().getName() + " : "
                + event.getMessage().getBody());
        builder.setSmallIcon(R.drawable.ic_menu_send);
        builder.setAutoCancel(true);
        Intent intent = new Intent(context, ConversationActivity.class);
        intent.putExtra(Const.EMAIL, event.getEmail());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        notificationManager.notify(2000 + Long.valueOf(event.getMessage().getId()).intValue(),
                builder.getNotification());
    }

}
