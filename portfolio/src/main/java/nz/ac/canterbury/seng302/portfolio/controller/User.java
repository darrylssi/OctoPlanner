package nz.ac.canterbury.seng302.portfolio.controller;


import javax.validation.constraints.*;

/**
 *
 */
public class User {

    @Size(min = 2, max = 15, message = "Username must be between 2 to 15 characters")
    private String userName;

    @Size(min = 2, max = 20, message = "First name must be between 2 to 20 characters")
    private String firstName;

    @Size(max = 20, message = "Middle name must have less than 20 characters")
    private String middleName;

    @Size(min = 2, max = 20, message = "Last name must be between 2 to 20 characters")
    private String lastName;

    @Size(max = 20, message = "Nickname must have less than 20 characters")
    private String nickName;

    private String pronouns;

    @Size(min = 7, max = 20, message = "Password must be between 7 to 20 characters")
    private String password;

    @Size(min = 7, max = 20, message = "Password must be between 7 to 20 characters")
    private String confirmPassword;

    private boolean passwordsEqual;

    @Email(message = "Email should be valid")
    private String email;

    @Size(max = 200, message = "Bio must have less than 200 characters")
    private String bio;

    public User() {}

    public User(String userName, String firstName, String middleName, String lastName, String nickName, String pronouns,
                String password, String confirmPassword, String email, String bio) {
        this.userName = userName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.nickName = nickName;
        this.pronouns = pronouns;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.email = email;
        this.bio = bio;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPronouns() {
        return pronouns;
    }

    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
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

    public void setPasswordsEqual(boolean passwordsEqual) {
        this.passwordsEqual = passwordsEqual;
    }

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordsEqual() {
        return (password == null) ? false : password.equals(confirmPassword);
    }


}

