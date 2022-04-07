package nz.ac.canterbury.seng302.identityprovider.service;

import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    @Autowired
    private UserRepository repository;

    private static final BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();

    /**
     * Validates the fields in a register request
     * @param request The register request to validate
     * @return A list of validation errors in the register request
     */
    public List<ValidationError> validateRegisterRequest(UserRegisterRequest request) {
        List<ValidationError> errors = new ArrayList<>();

        if (request.getUsername().isBlank()) {  // Checks that the username field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Username")
                    .setErrorText("Username cannot be empty")
                    .build();
            errors.add(error);
        } else if (request.getUsername().length() < 2 ||  // First name isn't too short
            request.getUsername().length() > 15) { // First name isn't too long
        ValidationError error = ValidationError.newBuilder()
                .setFieldName("Username")
                .setErrorText("Username must be between 2 to 15 characters")
                .build();
        errors.add(error);
        }
        // Checks that the username isn't already in the database
        else if (repository.findByUsername(request.getUsername()) != null) {
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Username")
                    .setErrorText("Username is already in use")
                    .build();
            errors.add(error);
        }

        if (request.getPassword().isBlank()) {  // Checks that the password field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Password")
                    .setErrorText("Password cannot be empty")
                    .build();
            errors.add(error);
        }

        return getValidationErrors(errors, request.getFirstName(), request.getMiddleName(), request.getLastName(),
                request.getNickname(), request.getBio(), request.getPersonalPronouns(), request.getEmail());
    }

    /**
     * Validates the fields in an edit user request
     * @param request The edit user request to validate
     * @return A list of validation errors in the edit user request
     */
    public List<ValidationError> validateEditUserRequest(EditUserRequest request, User user) {
        List<ValidationError> errors = new ArrayList<>();

        if (user == null) {    // Check that the user exists in the database
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("UserId")
                    .setErrorText("User does not exist")
                    .build();
            errors.add(error);
            return errors;
        }

        return getValidationErrors(errors, request.getFirstName(), request.getMiddleName(), request.getLastName(),
                request.getNickname(), request.getBio(), request.getPersonalPronouns(), request.getEmail());
    }

    /**
     * Validates several fields in a request that contains user's fields
     * @param errors The list of errors in the request to be added to
     * @param firstName The first name field in the request - cannot be blank, too short or too long
     * @param middleName The middle name field in the request - cannot be too long
     * @param lastName The last name field in the request - cannot be blank, too short or too long
     * @param nickname The nickname field in the request - cannot be too long
     * @param bio The bio field in the request - cannot be too long
     * @param personalPronouns The personal pronouns field in the request - must be formatted correctly
     * @param email The email field in the request - cannot be blank and must be formatted correctly
     * @return A list of validation errors found in the parameters
     */
    private List<ValidationError> getValidationErrors(List<ValidationError> errors, String firstName,
                                                      String middleName, String lastName, String nickname,
                                                      String bio, String personalPronouns, String email) {

        if (firstName.isBlank()) { // First name field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("FirstName")
                    .setErrorText("First name cannot be empty")
                    .build();
            errors.add(error);
        } else if (firstName.length() < 2 ||  // First name isn't too short
                firstName.length() > 20) { // First name isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("FirstName")
                    .setErrorText("First name must be between 2 to 20 characters")
                    .build();
            errors.add(error);
        }

        if (middleName.length() > 20) { // Middle name isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("MiddleName")
                    .setErrorText("Middle name must have less than 20 characters")
                    .build();
            errors.add(error);
        }

        if (lastName.isBlank()) {  // Last name field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("LastName")
                    .setErrorText("Last name cannot be empty")
                    .build();
            errors.add(error);
        } else if (lastName.length() < 2 ||   // Last name isn't too short
                lastName.length() > 20) { // Last name isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("LastName")
                    .setErrorText("Last name must be between 2 to 20 characters")
                    .build();
            errors.add(error);
        }

        if (nickname.length() > 20) { // Nickname isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Nickname")
                    .setErrorText("Nickname must have less than 20 characters")
                    .build();
            errors.add(error);
        }

        if (bio.length() > 200) { // Bio isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Bio")
                    .setErrorText("Bio must have less than 200 characters")
                    .build();
            errors.add(error);
        }

        if (validatePronouns(personalPronouns) != null) {    // Checks that pronouns are valid
            errors.add(validatePronouns(personalPronouns));
        }

        if (validateEmail(email) != null) {    // Checks that email is valid
            errors.add(validateEmail(email));
        }

        return errors;
    }

    /**
     * Validates the fields in a change password request
     * @param request The change password request to validate
     * @return A list of validation errors in the change password request
     */
    public List<ValidationError> validateChangePasswordRequest(ChangePasswordRequest request, User user) {
        List<ValidationError> errors = new ArrayList<>();

        if (user == null) {    // Check that the user exists in the database
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("UserId")
                    .setErrorText("User does not exist")
                    .build();
            errors.add(error);
            return errors;
        }

        if(request.getCurrentPassword().isBlank()) {    // Current password field isn't blank
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("CurrentPassword")
                    .setErrorText("Current password cannot be empty")
                    .build();
            errors.add(error);
        } else if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {   // Passwords don't match
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("CurrentPassword")
                    .setErrorText("Current password does not match password in database")
                    .build();
            errors.add(error);
        }

        if(request.getNewPassword().isBlank()) {    // New password field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("NewPassword")
                    .setErrorText("New password cannot be empty")
                    .build();
            errors.add(error);
        }else if (request.getNewPassword().length() < 7 ||   // New password isn't too short
                request.getNewPassword().length() > 20) { // New password isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("NewPassword")
                    .setErrorText("New password must be between 7 to 20 characters")
                    .build();
            errors.add(error);
        }

        return errors;
    }

    /**
     * Checks that an email is valid using very simple regex
     * Only checks that the email contains an @ simple with text on either side
     * @param email A string containing the email to validate
     * @return True or false whether the email is valid
     */
    private ValidationError validateEmail(String email) {
        if (email.isBlank()) { // Email field is empty
            return ValidationError.newBuilder()
                    .setFieldName("Email")
                    .setErrorText("Email cannot be empty")
                    .build();
        }

        String regex = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-.]+$";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        if(!matcher.matches()) {    // If email doesn't match regex
            return ValidationError.newBuilder()
                    .setFieldName("Email")
                    .setErrorText("Email must be valid")
                    .build();
        }
        return null;
    }

    /**
     * Checks that pronouns contain a "/" using regex
     * @param pronouns A string containing the pronouns to validate
     * @return True or false whether a "/" is found in the string
     */
    private ValidationError validatePronouns(String pronouns) {
        if (pronouns.length() > 20) { // Personal pronouns are too long
            return ValidationError.newBuilder()
                    .setFieldName("PersonalPronouns")
                    .setErrorText("Personal pronouns must have less than 20 characters")
                    .build();
        }

        //String regex = "^(.+)/(.+)(,(.+)/(.+))$";
        String regex = "^([a-zA-Z]+/)+[a-zA-Z]+(,\s*([a-zA-Z]+/)+[a-zA-Z]+)*$|^$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pronouns);

        if(!matcher.matches()) {    // If pronouns don't match regex
            return ValidationError.newBuilder()
                    .setFieldName("PersonalPronouns")
                    .setErrorText("Personal pronouns must be in the format \"pronoun/pronoun\"")
                    .build();
        }
        return null;
    }
}
