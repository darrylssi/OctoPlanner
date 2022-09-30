package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Class for sending project updates through websockets.
 * Contains the id of the project that was updated.
 */
public class ProjectMessage {

    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }
}
