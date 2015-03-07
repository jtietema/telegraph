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

package net.tietema.telegraph.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author jeroen
 */
@Entity
public class Contact {

    @Column
    private String name;

    @Id
    private String email;

    @ForeignCollectionField(orderColumnName = "time", orderAscending = false)
    private ForeignCollection<LocalMessage> messages;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ForeignCollection<LocalMessage> getMessages() {
        return messages;
    }

    public void setMessages(ForeignCollection<LocalMessage> messages) {
        this.messages = messages;
    }

    /**
     * The participant string containts the email adress with the resource id added to the end (joe@example.com/Ab34fd)
     * 
     * @param participant
     * @return
     */
    public static String getEmailFromParticipant(String participant) {
        return participant.substring(0, participant.indexOf("/"));
    }
}
