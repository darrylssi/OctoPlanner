package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Class for sending sprint updates through websockets.
 * Contains the id of the sprint that was updated.
 */
public class SprintMessage {

    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
