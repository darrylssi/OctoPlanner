package nz.ac.canterbury.seng302.portfolio.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import org.springframework.stereotype.Service;

@Service
public class UserAccountClientService {

    @GrpcClient("identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    /**
     * Sends a UserRegisterRequest to the identity provider
     * @param username The new user's username
     * @param password The new user's password
     * @param firstName The new user's first name
     * @param middleName The new user's middle name
     * @param lastName The new user's last name
     * @param nickname The new user's nickname
     * @param bio The new user's bio
     * @param personalPronouns The new user's personal pronouns
     * @param email The new user's email
     * @return A UserRegisterResponse, containing the success of the request and the new user's id
     */
    public UserRegisterResponse register(final String username, final String password,
                                         final String firstName, final String middleName,
                                         final String lastName, final String nickname,
                                         final String bio, final String personalPronouns,
                                         final String email) {
        UserRegisterRequest registerRequest = UserRegisterRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setFirstName(firstName)
                .setMiddleName(middleName)
                .setLastName(lastName)
                .setNickname(nickname)
                .setBio(bio)
                .setPersonalPronouns(personalPronouns)
                .setEmail(email)
                .build();
        return userAccountStub.register(registerRequest);
    }

    /**
     * Gets a user account from the identity provider with the specified id
     * @param id The id of the user account to get
     * @return A UserResponse with the attributes of the requested user account
     */
    public UserResponse getUserAccountById(final int id) {
        GetUserByIdRequest userRequest = GetUserByIdRequest.newBuilder()
                .setId(id)
                .build();
        return userAccountStub.getUserAccountById(userRequest);
    }

}