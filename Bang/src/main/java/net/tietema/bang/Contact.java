package net.tietema.bang;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.ForeignCollectionField;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

/**
 * @author jeroen
 */
@Entity
public class Contact {

    @Column
    private String name;

    @Id
    private String email;

    @ForeignCollectionField
    private ForeignCollection<Message> messages;

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

    public ForeignCollection<Message> getMessages() {
        return messages;
    }

    public void setMessages(ForeignCollection<Message> messages) {
        this.messages = messages;
    }
}
