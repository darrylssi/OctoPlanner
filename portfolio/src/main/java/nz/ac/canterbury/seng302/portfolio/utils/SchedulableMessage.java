package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Class for sending schedulable updates through websockets.
 * Contains the id of the schedulable that has been updated. Wrapper class so that it can be sent through websockets.
 */
public class SchedulableMessage {

    private int id;
    private String type;

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
