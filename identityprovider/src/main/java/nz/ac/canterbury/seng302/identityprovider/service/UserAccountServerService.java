package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static nz.ac.canterbury.seng302.shared.identityprovider.UserRole.*;

@GrpcService
public class UserAccountServerService extends UserAccountServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountServerService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService userService;

    /**
     * Adds a user to the database and returns a UserRegisterResponse to the portfolio
     * @param request An object containing all the details of the user to register
     */
    @Override
    public void register(UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {
        logger.info("register() has been called");
        UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();

        if (repository.findByUsername(request.getUsername()) == null) {

            // Creates a user object from the parameters in the request
            User user = new User(request.getUsername(), request.getPassword(), request.getFirstName(),
                    request.getMiddleName(), request.getLastName(), request.getNickname(),
                    request.getBio(), request.getPersonalPronouns(), request.getEmail());

            // Sets the current time as the users register date
            long millis = System.currentTimeMillis();
            Timestamp timestamp = Timestamp.newBuilder().setSeconds(millis / 1000)
                    .setNanos((int) ((millis % 1000) * 1000000)).build();
            user.setCreated(Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()));

            repository.save(user);  // Saves the user object to the database

            reply
                    .setIsSuccess(true)
                    .setNewUserId(user.getID())
                    .setMessage("User created successfully");
        } else {
            reply
                    .setIsSuccess(false)
                    .setMessage("That username is taken! Choose another");
        }
        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Gets a user from the database by its id and returns it as a UserResponse to the portfolio
     * @param request An object containing the id of the user to retrieve
     */
    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        logger.info("getUserAccountById has been called");
        UserResponse.Builder reply = UserResponse.newBuilder();

        Optional<User> userResponse = repository.findById(request.getId());
        User user;
        if (userResponse.isPresent()) {
            user = userResponse.get();
            setUserResponse(user, reply);
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getPaginatedUsers(GetPaginatedUsersRequest request, StreamObserver<PaginatedUsersResponse> responseObserver) {
        logger.info("getPaginatedUsers has been called");
        PaginatedUsersResponse.Builder reply = PaginatedUsersResponse.newBuilder();

        List<User> users;
        users = userService.getAllUsers();

        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users) {
            UserResponse.Builder userResponse = UserResponse.newBuilder();
            setUserResponse(user, userResponse);
            userResponses.add(userResponse.build());
        }

        reply
                .addAllUsers(userResponses)
                .setResultSetSize(users.size());

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Changes a users details
     * @param request Contains the details of the user to change, and what to change their details to
     */
    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        logger.info("editUser has been called");
        EditUserResponse.Builder reply = EditUserResponse.newBuilder();

        Optional<User> userResponse = repository.findById(request.getUserId()); // Attempts to get the user from the database

        List<ValidationError> errors = validateEditUserRequest(request, userResponse);

        if(errors.size() > 0) { // If there are errors in the request

            for (ValidationError error : errors) {
                logger.error(String.format("Edit user %s : %s - %s", request.getUserId(), error.getFieldName(), error.getErrorText()));
            }

            reply
                    .setIsSuccess(false)
                    .setMessage("User could not be edited")
                    .addAllValidationErrors(errors);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

        User user = userResponse.get(); // isPresent() check occurs in validateEditRequest()

        // Set the user's details to the details provided in the edit request
        user.setFirstName(request.getFirstName());
        user.setMiddleName(request.getMiddleName());
        user.setLastName(request.getLastName());
        user.setNickname(request.getNickname());
        user.setBio(request.getBio());
        user.setPersonalPronouns(request.getPersonalPronouns());
        user.setEmail(request.getEmail());

        repository.save(user);  // Saves the user object to the database
        reply
                .setIsSuccess(true)
                .setMessage("User edited successfully");

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Validates the fields in an edit user request
     * @param request The edit user request to validate
     * @return A list of validation errors in the edit user request
     */
    public List<ValidationError> validateEditUserRequest(EditUserRequest request, Optional<User> user) {
        List<ValidationError> errors = new ArrayList<>();

        if (user.isEmpty()) {    // Check that the user exists in the database
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("UserId")
                    .setErrorText("User does not exist")
                    .build();
            errors.add(error);
            return errors;
        }

        if (request.getFirstName().isBlank()) { // First name field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("FirstName")
                    .setErrorText("First name cannot be empty")
                    .build();
            errors.add(error);
        } else if (request.getFirstName().length() < 2 ||  // First name isn't too short
                request.getFirstName().length() > 20) { // First name isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("FirstName")
                    .setErrorText("First name must be between 2 to 20 characters")
                    .build();
            errors.add(error);
        }

        if (request.getMiddleName().length() > 20) { // Middle name isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("MiddleName")
                    .setErrorText("Middle name must have less than 20 characters")
                    .build();
            errors.add(error);
        }

        if (request.getLastName().isBlank()) {  // Last name field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("LastName")
                    .setErrorText("Last name cannot be empty")
                    .build();
            errors.add(error);
        } else if (request.getLastName().length() < 2 ||   // Last name isn't too short
                request.getLastName().length() > 20) { // Last name isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("LastName")
                    .setErrorText("Last name must be between 2 to 20 characters")
                    .build();
            errors.add(error);
        }

        if (request.getNickname().length() > 20) { // Nickname isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Nickname")
                    .setErrorText("Nickname must have less than 20 characters")
                    .build();
            errors.add(error);
        }

        if (request.getBio().length() > 200) { // Bio isn't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Bio")
                    .setErrorText("Bio must have less than 200 characters")
                    .build();
            errors.add(error);
        }

        if (request.getPersonalPronouns().length() > 20) { // Personal pronouns aren't too long
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("PersonalPronouns")
                    .setErrorText("Personal pronouns must have less than 20 characters")
                    .build();
            errors.add(error);
        }

        if (!validatePronouns(request.getPersonalPronouns())) {   // Check that personal pronouns contain a "/"
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("PersonalPronouns")
                    .setErrorText("Personal pronouns must contain a \"/\"")
                    .build();
            errors.add(error);
        }

        if (request.getEmail().isBlank()) { // Email field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Email")
                    .setErrorText("Email cannot be empty")
                    .build();
            errors.add(error);
        } else if (!validateEmail(request.getEmail())) {   // Check that email is valid
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Email")
                    .setErrorText("Email must be valid")
                    .build();
            errors.add(error);
        }

        return errors;
    }

    /**
     * Changes a user's password to a new password if the request is valid
     * @param request Contains the details of the user to change, their current password and their new password
     */
    @Override
    public void changeUserPassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
        logger.info("changeUserPassword has been called");
        ChangePasswordResponse.Builder reply = ChangePasswordResponse.newBuilder();

        Optional<User> userResponse = repository.findById(request.getUserId()); // Attempts to get the user from the database

        List<ValidationError> errors = validateChangePasswordRequest(request, userResponse);

        if(errors.size() > 0) { // If there are errors in the request

            for (ValidationError error : errors) {
                logger.error(String.format("Change password of user %s : %s - %s", request.getUserId(),
                        error.getFieldName(), error.getErrorText()));
            }

            reply
                    .setIsSuccess(false)
                    .setMessage("User's password could not be changed")
                    .addAllValidationErrors(errors);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

        User user = userResponse.get(); // isPresent() check occurs in validateChangePasswordRequest()

        // Set the user's password to the new password provided in the edit request

        // TODO use this instead once merged
        /*
        // Hash password
        String hashedPassword = new BCryptPasswordEncoder().encode(user.getPassword());
        user.setPassword(hashedPassword);
        */

        user.setPassword(request.getNewPassword());

        repository.save(user);  // Saves the user object to the database
        reply
                .setIsSuccess(true)
                .setMessage("User's password changed successfully");

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Validates the fields in a change password request
     * @param request The change password request to validate
     * @return A list of validation errors in the change password request
     */
    public List<ValidationError> validateChangePasswordRequest(ChangePasswordRequest request, Optional<User> user) {
        List<ValidationError> errors = new ArrayList<>();

        if (user.isEmpty()) {    // Check that the user exists in the database
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
            // TODO change to hashed password check once merged
        } else if (!Objects.equals(user.get().getPassword(), request.getCurrentPassword())) {   // Passwords match
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
    private Boolean validateEmail(String email) {
        String regex = "^(.+)@(.+)$";   // This regex can be changed to be more complex for more in-depth validation
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Checks that pronouns contain a "/" using regex
     * @param pronouns A string containing the pronouns to validate
     * @return True or false whether a "/" is found in the string
     */
    private Boolean validatePronouns(String pronouns) {
        String regex = "^(.+)/(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(pronouns);
        return matcher.matches();
    }

    /**
     * Sets the field in a userResponse object using a user object
     * @param user The user object to extract fields from
     * @param userResponse The userResponse builder to set fields in
     */
    private void setUserResponse(User user, UserResponse.Builder userResponse) {
        userResponse
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPersonalPronouns())
                .setEmail(user.getEmail())
                .setProfileImagePath("/") // TODO Path to users profile image once implemented
                .addRoles(STUDENT)           // TODO Get role(s) from database once implemented
                .setCreated(Timestamp.newBuilder()  // Converts Instant to protobuf.Timestamp
                        .setSeconds(user.getCreated().getEpochSecond())
                        .setNanos(user.getCreated().getNano()));
    }
}
