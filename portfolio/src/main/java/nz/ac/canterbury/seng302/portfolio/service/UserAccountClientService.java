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

    /**
     * Gets a paginated list of users from the identity provider
     * @param offset How many results to skip (offset of 0 means start at beginning, i.e page 1)
     * @param limit Max results to get - "results per page"
     * @param orderBy How to sort the results
     * @return A PaginatedUserResponse with a list of users and the total number of users in the response
     */
    public PaginatedUsersResponse getPaginatedUsers(final int offset, final int limit, final String orderBy, final String dir) {
        GetPaginatedUsersRequest paginatedUsersRequest = GetPaginatedUsersRequest.newBuilder()
                .setOffset(offset)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setDir(dir)
                .build();
        return userAccountStub.getPaginatedUsers(paginatedUsersRequest);
    }

    /**
     * Sends an EditUserRequest to the identity provider
     * @param userId The id of the user to edit
     * @param firstName The edited first name of the user
     * @param middleName The edited middle name of the user
     * @param lastName The edited last name of the user
     * @param nickname The edited nickname of the user
     * @param bio The edited bio of the user
     * @param personalPronouns The edited personal pronouns of the user
     * @param email The edited email of the user
     * @return An EditUserResponse containing the success of the request
     */
    public EditUserResponse editUser(final int userId, final String firstName, final String middleName,
                                     final String lastName, final String nickname, final String bio,
                                     final String personalPronouns, final String email) {
        EditUserRequest editUserRequest = EditUserRequest.newBuilder()
                .setUserId(userId)
                .setFirstName(firstName)
                .setMiddleName(middleName)
                .setLastName(lastName)
                .setNickname(nickname)
                .setBio(bio)
                .setPersonalPronouns(personalPronouns)
                .setEmail(email)
                .build();
        return userAccountStub.editUser(editUserRequest);
    }

    /**
     * Sends a ChangePasswordRequest to the identity provider
     * @param userId The id of the user to edit
     * @param currentPassword The user's current password
     * @param newPassword The user's new password to be changed to
     * @return A ChangePasswordResponse containing the success of the request
     */
    public ChangePasswordResponse changeUserPassword(final int userId, final String currentPassword,
                                                     final String newPassword) {
        ChangePasswordRequest changePasswordRequest = ChangePasswordRequest.newBuilder()
                .setUserId(userId)
                .setCurrentPassword(currentPassword)
                .setNewPassword(newPassword)
                .build();
        return userAccountStub.changeUserPassword(changePasswordRequest);
    }
}
