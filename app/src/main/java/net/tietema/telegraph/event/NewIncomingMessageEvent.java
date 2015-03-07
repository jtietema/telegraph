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

package net.tietema.telegraph.event;

import net.tietema.telegraph.model.LocalMessage;

/**
 * @author jeroen
 */
public class NewIncomingMessageEvent {

    private String email;
    private LocalMessage message;

    public NewIncomingMessageEvent(String email, LocalMessage message) {
        this.email = email;
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public LocalMessage getMessage() {
        return message;
    }

}
