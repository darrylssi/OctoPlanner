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
import java.util.*;


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
        StringJoiner bld = new StringJoiner(". ");

        if(!errors.isEmpty()) { // If there are errors in the request

            for (ValidationError error : errors) {
                logger.error(String.format("Register user %s : %s - %s",
                        request.getUsername(), error.getFieldName(), error.getErrorText()));
                bld.add(error.getErrorText());
            }

            reply
                    .setIsSuccess(false)
                    .setMessage(bld.toString())
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

        User user = repository.findById(request.getId());
        if (user != null) {
            UserResponse reply = buildUserResponse(user);
            responseObserver.onNext(reply);
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

        List<User> paginatedUsers;
        try {
            if (orderBy.equals("role")) { // Ordering by "role" means order by highest role ({STUDENT, COURSE_ADMIN} =>
                                          // highest is COURSE_ADMIN).
                                          // JPA allows ordering by joined tables, HOWEVER it generates SQL incompatible
                                          // with H2. So, we have
                                          // to bring the entire table into memory & sort here until that's migrated.
                                          // TODO Andrew: Once we're on MariaDB, remove this function.
                paginatedUsers = paginatedUsersOrderedByRole(offset, limit, isAscending);
            } else {
                paginatedUsers = userService.getUsersPaginated(offset, limit, orderBy, isAscending);
            }
        } catch (IllegalArgumentException e) { // `orderBy` wasn't a valid value.
            Throwable statusError = Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
            responseObserver.onError(statusError);
            return;
        }

        List<UserResponse> userResponses = paginatedUsers.stream().map(UserAccountServerService::buildUserResponse).toList();
        int numUsersInDatabase = (int) repository.count();
        reply
            .addAllUsers(userResponses)
            .setResultSetSize(numUsersInDatabase);

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }


    /**
     * Returns a paginated view of the database's users, order by each user's highest role.
     * <p>
     * In an ideal world we wouldn't need this, but as mentioned the JPA generated
     * isn't valid H2, so until that's fixed we need this.
     * </p>
     *
     * @param page What "page" of the users you want. Affected by the ordering and page size
     * @param limit How many items are in a page
     * @param isAscending Is the list in ascending or descending order
     * @return A list of users from that "page"
     */
    private List<User> paginatedUsersOrderedByRole(int page, int limit, boolean isAscending) {
        ArrayList<User> users = new ArrayList<User>(userService.getAllUsers());
        if (!isAscending)
            users.sort((a, b) -> a.highestRole().getNumber() - b.highestRole().getNumber());
        else
            users.sort((a, b) -> b.highestRole().getNumber() - a.highestRole().getNumber());

        /* Get the sublist that is needed for the page */
        int fromIndex = page * limit;
        int toIndex = (page+1) * limit;
        if (toIndex > users.size()) {    // If there are fewer users than needed for the page
            toIndex = users.size();
        }

        return users.subList(fromIndex, toIndex);
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
     * Returns a UserResponse builder object from a User model.
     *
     * @param user The user object to extract fields from
     * @return A gRPC-ready response object with the user's fields copied in.
     */
    private static UserResponse buildUserResponse(User user) {
        ArrayList<UserRole> sortedRoles = new ArrayList<UserRole>(user.getRoles());
        sortedRoles.sort(Comparator.naturalOrder());

        UserResponse.Builder userResponse = UserResponse
            .newBuilder()
                .setId(user.getID())
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
                .setCreated(Timestamp.newBuilder()  // Converts Instant to protobuf.Timestamp
                    .setSeconds(user.getCreated().getEpochSecond())
                    .setNanos(user.getCreated().getNano()));

        return userResponse.build();
    }
}
