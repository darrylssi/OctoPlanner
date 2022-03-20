package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.GetUserByIdRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserAccountServiceGrpc.UserAccountServiceImplBase;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterRequest;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Optional;

@GrpcService
public class UserAccountServerService extends UserAccountServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountServerService.class);

    @Autowired
    private UserRepository repository;

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
        User user = null;
        if (userResponse.isPresent()) {
            user = userResponse.get();
        }

        reply
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickName())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPersonalPronouns())
                .setEmail(user.getEmail())
                .setCreated(com.google.protobuf.Timestamp.newBuilder()  // Converts Instant to protobuf.Timestamp
                        .setSeconds(user.getCreated().getEpochSecond())
                        .setNanos(user.getCreated().getNano()));
        //      .setProfileImagePath("...") Path to users profile image once implemented
        //      .setRoles(...)              Users roles once implemented

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

}
