package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Basic class for sending data through a websocket.
 * Any changes here need to be reflected in MessageMappingController.java and webSocketHandler.js.
 */
public class Message {

    private String from;
    private String content;

    public Message(String from, String content) {
        this.from = from;
        this.content = content;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
