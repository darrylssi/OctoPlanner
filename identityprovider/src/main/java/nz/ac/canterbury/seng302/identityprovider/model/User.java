package nz.ac.canterbury.seng302.identityprovider.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table (name = "Users")
public class User {
    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO) //TODO should this go here?
    private int ID;
    @NotBlank(message = "Username must not be empty and not already in use")
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    @NotEmpty(message = "Password must not be empty") // NotEmpty allows for leading/trailing spaces
    private String password;
    @NotBlank(message = "First name must not be empty")
    private String firstName;
    private String middleName;
    @NotBlank(message = "Last name must not be empty")
    private String lastName;
    private String nickName;
    private String bio;
    private String personalPronouns;
    @Column(nullable = false, unique = true)
    @NotBlank(message = "Email address must not be empty")
    @Email(message = "Email address must be valid and not already in use")
    private String email;
    @Column(nullable = false)
    private Date registerDate;

    public int getID() {
        return this.ID;
    }

    public void setID(int ID) {
        this.ID = ID;
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

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
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

    public Date getRegisterDate() {
        return this.registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getFullName() {
        if (this.middleName == null){
            return this.firstName + " " + this.lastName;
        }else {
            return this.firstName + " " + this.middleName + " " + this.lastName;
        }
    }
}
