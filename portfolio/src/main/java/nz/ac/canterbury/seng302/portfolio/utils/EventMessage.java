package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Class for sending event updates through websockets.
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
