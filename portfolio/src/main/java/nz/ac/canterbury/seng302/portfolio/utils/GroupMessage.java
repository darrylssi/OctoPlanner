package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Class just holding a group's ID; used in websockets to ask for an update
 */
public class GroupMessage {
    private int id;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}
