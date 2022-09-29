package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.shared.identityprovider.GroupDetailsResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing all the information about a group; used as an update with websockets.
 */
public class GroupMessageOutput {

    /**
     * Used to serialise users for sending through websockets
     */
    private static class SmallUser {
        private String fullName;
        private int id;

        public SmallUser(int id, String fullName) {
            this.id = id;
            this.fullName = fullName;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }

    private int id;
    private String shortName;
    private String longName;
    private List<SmallUser> members;

    /**
     * Constructor used for a nonexistent group
     * Lack of names will indicate that the group can be deleted
     */
    public GroupMessageOutput(int id) {
        this.id = id;
    }

    /**
     * Constructor
     * @param id id of the group the update is being sent for
     * @param groupDetails response from the IDP with group information
     */
    public GroupMessageOutput(int id, GroupDetailsResponse groupDetails) {
        this.id = id;
        this.shortName = groupDetails.getShortName();
        this.longName = groupDetails.getLongName();
        this.members = new ArrayList<>();
        for (UserResponse user: groupDetails.getMembersList()) {
            if (user.getMiddleName().isBlank()) { // so there is no extra space if they have no middle name
                this.members.add(new SmallUser(user.getId(), String.format("%s %s", user.getFirstName(), user.getLastName())));
            } else {
                this.members.add(new SmallUser(user.getId(), String.format("%s %s %s", user.getFirstName(), user.getLastName(), user.getLastName())));
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }

    public List<SmallUser> getMembers() {
        return members;
    }

    public void setMembers(List<SmallUser> members) {
        this.members = members;
    }
}
