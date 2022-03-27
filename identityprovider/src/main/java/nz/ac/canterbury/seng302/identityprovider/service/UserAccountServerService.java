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
import java.util.Optional;

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
        List<ValidationError> errors = validateRegisterRequest(request);

        if(errors.size() > 0) { // If there are errors in the request
            reply
                    .setIsSuccess(false)
                    .setMessage("User could not be created")
                    .addAllValidationErrors(errors);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
            return;
        }

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

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

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

        if (request.getFirstName().isBlank()) { // Checks that the first name field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("FirstName")
                    .setErrorText("FirstName cannot be empty")
                    .build();
            errors.add(error);
        }

        if (request.getLastName().isBlank()) {  // Checks that the last name field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("LastName")
                    .setErrorText("LastName cannot be empty")
                    .build();
            errors.add(error);
        }

        if (request.getEmail().isBlank()) { // Checks that the email field isn't empty
            ValidationError error = ValidationError.newBuilder()
                    .setFieldName("Email")
                    .setErrorText("Email cannot be empty")
                    .build();
            errors.add(error);
        }

        return errors;
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
                .setNickname(user.getNickName())
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
