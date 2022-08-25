package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Class for sending event updates through websockets.
 * Contains the id of the event that has been updated. Wrapper class so that it can be sent through websockets.
 */
public class EventMessage {

    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
    }
}
