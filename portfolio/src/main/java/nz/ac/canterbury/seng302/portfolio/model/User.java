package nz.ac.canterbury.seng302.portfolio.model;


import javax.validation.constraints.*;

/**
 *
 */
public class User {

    @Size(min = 2, max = 15, message = "Username must be between 2 to 15 characters")
    private String username;

    @Size(min = 2, max = 20, message = "First name must be between 2 to 20 characters")
    private String firstName;

    @Size(max = 20, message = "Middle name must have less than 20 characters")
    private String middleName;

    @Size(min = 2, max = 20, message = "Last name must be between 2 to 20 characters")
    private String lastName;

    @Size(max = 20, message = "Nickname must have less than 20 characters")
    private String nickname;

    @Size(max = 20, message = "Personal pronouns must have less than 20 characters")
    private String personalPronouns;

    @Size(min = 7, max = 20, message = "Password must be between 7 to 20 characters")
    private String password;

    @Size(min = 7, max = 20, message = "Password must be between 7 to 20 characters")
    private String confirmPassword;

    @Email(message = "Email should be valid")
    @Pattern(regexp="^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$", message="Must be like something@someserver.com")
    private String email;

    @Size(max = 200, message = "Bio must have less than 200 characters")
    private String bio;

    public User() {}

    public User(String username, String firstName, String middleName, String lastName, String nickname, String personalPronouns,
                String password, String confirmPassword, String email, String bio) {
        this.username = username;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.personalPronouns = personalPronouns;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPersonalPronouns() {
        return personalPronouns;
    }

    public void setPersonalPronouns(String personalPronouns) {
        this.personalPronouns = personalPronouns;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordsEqual() {
        return (password == null) ? false : password.equals(confirmPassword);
    }


}

