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
