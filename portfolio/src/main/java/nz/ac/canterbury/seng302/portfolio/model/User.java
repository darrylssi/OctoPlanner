package nz.ac.canterbury.seng302.portfolio.model;


import javax.validation.constraints.*;

/**
 *
 */
public class User {

   // private Date registrationDate;

//    @NotEmpty(message = "Username can't be empty")
    @Size(min = 2, max = 15, message = "The length must be in range from 2 to 15")
    private String userName;

//    @NotEmpty(message = "First name can't be empty")
    @Size(min = 2, max = 20, message = "The length must be in range from 2 to 20")
    private String firstName;

    private String middleName;

//    @NotBlank(message = "Last name can't be empty")
    @Size(min = 2, max = 20, message = "The length must be in range from 2 to 20")
    private String lastName;

    private String nickName;

//    @Size(min = 2, max = 20, message = "The length must be in range from 2 to 15")
    private String pronouns;

//    @NotBlank(message = "Password can't be empty")
    @Size(min = 7, message = "The length of password must be 7")
    private String password;

//    @NotBlank(message = "Confirm Password can't be empty")
    @Size(min = 7, message = "The length of password must be in range from 7 to 20")
    private String confirmPassword;

    private boolean passwordsEqual;

//    @NotBlank(message = "Email can't be empty")
    @Email(message = "Email should be valid")
    private String email;

//    @Size(min = 10, max = 200, message = "The length must be in range from 10 to 200")
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

    @AssertTrue(message = "Password do not match")
    public boolean isPasswordsEqual() {
        return (password == null) ? false : password.equals(confirmPassword);
    }


}

