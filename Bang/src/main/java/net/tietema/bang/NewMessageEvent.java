package net.tietema.bang;

/**
 * @author jeroen
 */
public class NewMessageEvent {

    private String email;

    public NewMessageEvent(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
