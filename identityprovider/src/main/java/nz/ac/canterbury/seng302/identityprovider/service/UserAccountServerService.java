package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;



@GrpcService
public class UserAccountServerService extends UserAccountServiceGrpc.UserAccountServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountServerService.class);

    private static final BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private ValidationService validator;

    /**
     * Adds a user to the database and returns a UserRegisterResponse to the portfolio
     * @param request An object containing all the details of the user to register
     */
    @Override
    public void register(UserRegisterRequest request, StreamObserver<UserRegisterResponse> responseObserver) {
        logger.info("register() has been called");
        UserRegisterResponse.Builder reply = UserRegisterResponse.newBuilder();
        List<ValidationError> errors = validator.validateRegisterRequest(request);

        if(errors.size() > 0) { // If there are errors in the request

            for (ValidationError error : errors) {
                logger.error(String.format("Register user %s : %s - %s",
                        request.getUsername(), error.getFieldName(), error.getErrorText()));
            }

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
        // All users are initially given a `student` role
        user.addRole(UserRole.STUDENT);

        // Hash password
        String hashedPassword = encoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

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
     * Gets a user from the database by its id and returns it as a UserResponse to the portfolio.
     * 
     * Gives a Status.NOT_FOUND error if the user ID is invalid.
     * @param request An object containing the id of the user to retrieve
     */
    @Override
    public void getUserAccountById(GetUserByIdRequest request, StreamObserver<UserResponse> responseObserver) {
        logger.info("getUserAccountById has been called");
        UserResponse.Builder reply = UserResponse.newBuilder();

        User user = repository.findById(request.getId());
        if (user != null) {
            setUserResponse(user, reply);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }

    }

    @Override
    public void getPaginatedUsers(GetPaginatedUsersRequest request, StreamObserver<PaginatedUsersResponse> responseObserver) {
        logger.info("getPaginatedUsers has been called");
        PaginatedUsersResponse.Builder reply = PaginatedUsersResponse.newBuilder();
        int limit = request.getLimit();
        int offset = request.getOffset();
        String orderBy = request.getOrderBy();
        boolean isAscending = request.getIsAscendingOrder();
        List<User> allUsers = userService.getAllUsers();

        List<User> users;
        if (!orderBy.equals("role")) {
            users = userService.getUsersPaginated(offset, limit, orderBy, isAscending);
        } else {
            users = filterRoles(allUsers, offset, limit, isAscending);
        }

        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users) {
            UserResponse.Builder userResponse = UserResponse.newBuilder();
            setUserResponse(user, userResponse);
            userResponses.add(userResponse.build());
        }

        reply
                .addAllUsers(userResponses)
                .setResultSetSize(allUsers.size());

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    /* Manually sort roles list, return only those that are to be displayed on the page */
    private List<User> filterRoles(List<User> users, int page, int limit, boolean isAscending) {
        List<User> first = new ArrayList<>();
        List<User> middle = new ArrayList<>();
        List<User> last = new ArrayList<>();

        if (isAscending) { /* Take course admins first, then teachers, then students */
            for (User user : users) {
                if (user.getRoles().contains(UserRole.COURSE_ADMINISTRATOR)) {
                    first.add(user);
                } else if (user.getRoles().contains(UserRole.TEACHER)) {
                    middle.add(user);
                } else {
                    last.add(user);
                }
            }
        } else { /* Take students first, then teachers, then course admins */
            for (User user : users) {
                if (user.getRoles().contains(UserRole.STUDENT)) {
                    first.add(user);
                } else if (user.getRoles().contains(UserRole.TEACHER)) {
                    middle.add(user);
                } else {
                    last.add(user);
                }
            }
        }

        List<User> sorted = new ArrayList<>(first);
        sorted.addAll(middle);
        sorted.addAll(last);

        /* Get the sublist that is needed for the page */
        List<User> filtered;
        if ((page + 1) * limit >= sorted.size()) {  // if there are fewer users than needed for the page
            filtered = sorted.subList(page * limit, sorted.size());
        } else {
            filtered = sorted.subList(page * limit, (page + 1) * limit);
        }

        return filtered;
    }


    /**
     * <p>Assigns a role to the given user.</p>
     * 
     * Returns true if the user didn't already have this role.
     * Gives a Status.NOT_FOUND error if the user ID is invalid
     */
    @Override
    public void addRoleToUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("addRoleToUser() has been called");

        UserRoleChangeResponse.Builder reply = UserRoleChangeResponse.newBuilder();
        int userId = request.getUserId();
        UserRole role = request.getRole();
        try {
            // If the user didn't have this role, add it and return true
            // Otherwise does nothing, returning false.
            boolean success = userService.addRoleToUser(userId, role);
            reply.setIsSuccess(success);
            if (success)
                reply.setMessage("Role successfully add");
            else
                reply.setMessage("Couldn't add role: User already had this role.");
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            // The user ID pointing to a non-existent user
            responseObserver.onError(Status.NOT_FOUND.withDescription("User with that ID doesn't exist").asRuntimeException());
        }
    }

    /**
     * <p>Removes a role from the given user.</p>
     * 
     * Returns false if the user didn't already have this role.
     * Gives a Status.NOT_FOUND error if the user ID is invalid
     */
    @Override
    public void removeRoleFromUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("removeRoleFromUser() has been called");

        UserRoleChangeResponse.Builder reply = UserRoleChangeResponse.newBuilder();
        int userId = request.getUserId();
        UserRole role = request.getRole();
        try {
            // If the user had this role, remove it and return true
            // Otherwise does nothing, returning false.
            boolean success = userService.removeRoleFromUser(userId, role);
            reply.setIsSuccess(success);
            if (success)
                reply.setMessage("Role successfully removed");
            else
                reply.setMessage("Couldn't remove role: User didn't have this role");
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            // The user ID pointing to a non-existent user
            responseObserver.onError(Status.NOT_FOUND.withDescription("User with that ID doesn't exist").asRuntimeException());
        }
    }

    /**
     * Changes a users details
     * @param request Contains the details of the user to change, and what to change their details to
     */
    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        logger.info("editUser() has been called");
        EditUserResponse.Builder reply = EditUserResponse.newBuilder();

        User user = repository.findById(request.getUserId()); // Attempts to get the user from the database

        List<ValidationError> errors = validator.validateEditUserRequest(request, user);

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
     * Changes a user's password to a new password if the request is valid
     * @param request Contains the details of the user to change, their current password and their new password
     */
    @Override
    public void changeUserPassword(ChangePasswordRequest request, StreamObserver<ChangePasswordResponse> responseObserver) {
        logger.info("changeUserPassword() has been called");
        ChangePasswordResponse.Builder reply = ChangePasswordResponse.newBuilder();

        User user = repository.findById(request.getUserId()); // Attempts to get the user from the database

        List<ValidationError> errors = validator.validateChangePasswordRequest(request, user);

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

        // Set the user's password to the new password provided in the edit request
        // Hash password
        String hashedPassword = encoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);

        repository.save(user);  // Saves the user object to the database
        reply
                .setIsSuccess(true)
                .setMessage("User's password changed successfully");

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
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPersonalPronouns())
                .setEmail(user.getEmail())
                .setProfileImagePath("/") // TODO Path to users profile image once implemented
                .addAllRoles(user.getRoles())
                .setId(user.getID())
                .setCreated(Timestamp.newBuilder()  // Converts Instant to protobuf.Timestamp
                        .setSeconds(user.getCreated().getEpochSecond())
                        .setNanos(user.getCreated().getNano()));
    }
}
