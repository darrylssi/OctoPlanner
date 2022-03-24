package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;


@GrpcService
public class UserAccountServerService extends UserAccountServiceGrpc.UserAccountServiceImplBase {

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
            // All users are initially given a `student` role
            user.addRole(UserRole.STUDENT);
            
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

        User user = repository.findById(request.getId());
        if (user != null) {
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

    @Override
    public void addRoleToUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("addRoleToUser() has been called");

        UserRoleChangeResponse.Builder reply = UserRoleChangeResponse.newBuilder();
        int userId = request.getUserId();
        UserRole role = request.getRole();
        try {
            // If the user didn't already have this role
            if (userService.addRoleToUser(userId, role)) {
                reply.setIsSuccess(true);
            } else {
                reply.setIsSuccess(false);
            }
        } catch (NoSuchElementException e) {
            // The user ID pointing to a non-existent user
            reply.setIsSuccess(false);
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    @Override
    public void removeRoleFromUser(ModifyRoleOfUserRequest request, StreamObserver<UserRoleChangeResponse> responseObserver) {
        logger.info("removeRoleFromUser() has been called");

        UserRoleChangeResponse.Builder reply = UserRoleChangeResponse.newBuilder();
        int userId = request.getUserId();
        UserRole role = request.getRole();
        try {
            // If the user had this role
            if (userService.removeRoleFromUser(userId, role)) {
                reply.setIsSuccess(true);
            } else {
                reply.setIsSuccess(false);
            }
        } catch (NoSuchElementException e) {
            // The user ID pointing to a non-existent user
            reply.setIsSuccess(false);
        }

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
                .addAllRoles(user.getRoles())
                .setCreated(Timestamp.newBuilder()  // Converts Instant to protobuf.Timestamp
                        .setSeconds(user.getCreated().getEpochSecond())
                        .setNanos(user.getCreated().getNano()));
    }
}
