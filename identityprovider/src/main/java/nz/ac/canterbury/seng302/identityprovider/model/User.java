package nz.ac.canterbury.seng302.identityprovider.model;

import org.hibernate.annotations.CreationTimestamp;
import javax.persistence.*;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * Represents a user object.
 */
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String firstName;
    private String middleName;
    private String lastName;
    private String nickname;
    private String bio;
    private String personalPronouns;
    @Column(nullable = false, unique = true)
    private String email;
    @CreationTimestamp
    private Instant created;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated
    private Set<UserRole> roles;

    @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER)
    private Set<Group> groups = new HashSet<>();

    protected User() {
    }

    public User(String username, String password, String firstName,
            String middleName, String lastName, String nickname,
            String bio, String personalPronouns, String email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.bio = bio;
        this.personalPronouns = personalPronouns;
        this.email = email;
        this.roles = new HashSet<>();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return this.middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getBio() {
        return this.bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPersonalPronouns() {
        return this.personalPronouns;
    }

    public void setPersonalPronouns(String personalPronouns) {
        this.personalPronouns = personalPronouns;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Set<UserRole> getRoles() {
        return roles;
    }

    /**
     * Add a role to this user
     * 
     * @param role The role enum type to be added
     * @return <code>true</code> if the user didn't already have this role
     */
    public boolean addRole(UserRole role) {
        return roles.add(role);
    }

    /**
     * Remove a role from this user
     * 
     * @param role The role enum type to be removed
     * @return <code>true</code> if the user had this item removed
     */
    public boolean removeRole(UserRole role) {
        if(roles.size() == 1) {
            return false;
        } else {
            return roles.remove(role);
        }
    }

    /**
     * Combines the users first name, middle name (if they have one) and their last name into one String
     * @return A String containing the user's full name
     */
    public String getFullName() {
        if (this.middleName == null) {
            return this.firstName + " " + this.lastName;
        } else {
            return this.firstName + " " + this.middleName + " " + this.lastName;
        }
    }

    public UserRole highestRole() {
        return roles.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    /**
     * Adds a group to the user's set of joined groups
     * This does not add the user to the corresponding group's set of members
     * @param group The group to add
     */
    public void joinGroup(Group group) {
        this.groups.add(group);
    }

    /**
     * Removes a group from the user's set of joined groups
     * This does not remove the user from the corresponding group's set of members
     * @param group The group to remove
     */
    public void leaveGroup(Group group) {
        this.groups.remove(group);
    }
}
