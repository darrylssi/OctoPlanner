package nz.ac.canterbury.seng302.identityprovider.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a group object.
 * Group objects are stored in a table called Groups, as it is an @Entity.
 */
@Entity
@Table(name = "Groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> members = new HashSet<>();

    @NotNull
    @Size(min = 2, max = 32)
    private String shortName;

    @Size(max = 128)
    private String longName;

    protected Group() {
    }

    /**
     * Default constructor for a group
     * @param shortName The short name for a group
     * @param longName The long name for a group
     */
    public Group(String shortName, String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }

    /**
     * Adds a user to this group
     * Also adds this group to the user's set of joined groups
     * @param user The user to add to the group
     */
    public void addMember(User user) {
        members.add(user);
        user.joinGroup(this);
    }

    /**
     * Removes a user from this group
     * Also removes this group from the user's set of joined groups
     * @param user The user to remove from this group
     */
    public void removeMember(User user) {
        members.remove(user);
        user.leaveGroup(this);
    }

    /**
     * Removes all members from this group
     * Also removes this group from all members sets of joined groups
     * This should be used for removing this group from all members list of groups before deleting the group
     */
    public void removeAllMembers() {
        for (User user : members) {
            user.leaveGroup(this);
        }
        members.clear();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<User> getMembers() {
        return members;
    }

    public void setMembers(Set<User> members) {
        this.members = members;
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
}
