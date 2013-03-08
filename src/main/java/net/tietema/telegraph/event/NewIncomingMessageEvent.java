package net.tietema.telegraph.event;

/**
 * @author jeroen
 */
public class NewIncomingMessageEvent {

    private String email;

    public NewIncomingMessageEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
