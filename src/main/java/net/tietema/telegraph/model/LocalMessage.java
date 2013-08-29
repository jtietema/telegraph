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

import com.j256.ormlite.field.DatabaseField;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author jeroen
 */
@Entity
public class LocalMessage {
    // message status codes
    public static final int STATUS_PENDING    = 0;
    public static final int STATUS_SENT       = 1;
    public static final int STATUS_RECEIVED   = 2;

    @Id
    @GeneratedValue
    private long id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "contact_id")
    private Contact contact;

    @Column
    private String body;

    @Column
    private Date time;

    @Column
    private int status = STATUS_RECEIVED;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getTime() {
        return (Date) time.clone();
    }

    public void setTime(Date time) {
        this.time = (Date) time.clone();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Boolean isMine() {
        return this.status == STATUS_PENDING || this.status == STATUS_SENT;
    }
}
