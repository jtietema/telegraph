package net.tietema.telegraph.event;

/**
 * @author jeroen
 */
public class NewOutgoingMessageEvent {

    private String email;

    public NewOutgoingMessageEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
