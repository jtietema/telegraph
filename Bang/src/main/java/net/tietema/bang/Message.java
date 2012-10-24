package net.tietema.bang;

import com.j256.ormlite.field.DatabaseField;

import javax.persistence.*;
import java.util.Date;

/**
 * @author jeroen
 */
@Entity
public class Message {
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
    private int status;

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
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
