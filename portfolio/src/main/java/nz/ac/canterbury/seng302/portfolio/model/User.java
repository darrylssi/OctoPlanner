package nz.ac.canterbury.seng302.portfolio.model;

import javax.validation.constraints.*;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


/**
 * Represents a user object.
 */
public class User {

    @Size(min = MIN_NAME_LENGTH, max = MAX_USERNAME_LENGTH,
            message = "Username must be between " + MIN_NAME_LENGTH + " to " + MAX_USERNAME_LENGTH + " characters.")
    private String username;

    @Size(min = MIN_NAME_LENGTH, max = MAX_DESC_LENGTH,
            message = "First name must be between " + MIN_NAME_LENGTH + " to " + MAX_NAME_LENGTH + " characters.")
    private String firstName;

    @Size(max = MAX_NAME_LENGTH, message = "Middle name must have less than " + MAX_NAME_LENGTH + " characters.")
    private String middleName;

    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH,
            message = "Last name must be between " + MIN_NAME_LENGTH + " to " + MAX_NAME_LENGTH + " characters.")
    private String lastName;

    @Size(max = MAX_NAME_LENGTH, message = "Nickname must have less than " + MAX_NAME_LENGTH + " characters.")
    private String nickname;

    @Size(max = MAX_NAME_LENGTH, message = "Personal pronouns must have less than " + MAX_NAME_LENGTH + " characters.")
    private String personalPronouns;

    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH,
            message = "Password must be between " + MIN_PASSWORD_LENGTH + " to " + MAX_PASSWORD_LENGTH + " characters.")
    private String password;

    @Size(min = MIN_PASSWORD_LENGTH, max = MAX_PASSWORD_LENGTH,
            message = "Password must be between " + MIN_PASSWORD_LENGTH + " to " + MAX_PASSWORD_LENGTH + " characters")
    private String confirmPassword;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = MAX_DESC_LENGTH, message = "Bio must have less than 200 characters")
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
        return password != null && password.equals(confirmPassword);
    }



}

