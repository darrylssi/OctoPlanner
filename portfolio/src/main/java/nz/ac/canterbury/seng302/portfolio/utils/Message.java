package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Basic class for sending data through a websocket.
 * Any changes here need to be reflected in MessageMappingController.java and webSocketHandler.java.
 */
public class Message {

    private String from;
    private String text;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
