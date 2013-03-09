package net.tietema.telegraph.model;

import com.j256.ormlite.field.DatabaseField;

import javax.persistence.*;
import java.util.Date;

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
