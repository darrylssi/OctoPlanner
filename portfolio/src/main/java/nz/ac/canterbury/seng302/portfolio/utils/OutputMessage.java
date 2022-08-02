package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Basic class for sending data through a websocket.
 * Any changes here need to be reflected in MessageMappingController.java and webSocketHandler.java.
 */
public class OutputMessage {

    private String from;
    private String text;
    private String time;

    /**
     * Constructor.
     * @param from string, sender of the message
     * @param text string, text of the message
     * @param time string, time of the message
     */
    public OutputMessage(String from, String text, String time) {
        this.from = from;
        this.text = text;
        this.time = time;
    }

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
