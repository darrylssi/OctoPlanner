package nz.ac.canterbury.seng302.portfolio.service;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.PaginationRequestOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class UserAccountClientService {

    @GrpcClient("identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceBlockingStub userAccountStub;

    @GrpcClient("identity-provider-grpc-server")
    private UserAccountServiceGrpc.UserAccountServiceStub userAccountServiceStub;

    private static final Logger logger = LoggerFactory.getLogger(UserAccountClientService.class);

    /**
     * Sends a request to the identity provider to register a new user
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
     * @return A UserResponse with the attributes of the requested user account,
     * or <code>null</code> if the user doesn't exist
     */
    public UserResponse getUserAccountById(final int id) {
        GetUserByIdRequest userRequest = GetUserByIdRequest.newBuilder()
                .setId(id)
                .build();
        try {
            return userAccountStub.getUserAccountById(userRequest);
        } catch (StatusRuntimeException e) {
            return null;
        }
    }

    /**
     * Gets a paginated list of users from the identity provider
     * @param offset What "page" of the users you want. Affected by the ordering and page size
     * @param limit How many items you want from
     * @param orderBy How the list is ordered.
     *                Your options are:
     *                  <ul>
     *                    <li><code>"name"</code> - Ordered by their first, middle, and last name alphabetically</li>
     *                    <li><code>"username"</code> - Ordered by their username alphabetically</li>
     *                    <li><code>"nickname"</code> - Ordered by their nickname alphabetically</li>
     *                    <li><code>"role"</code> - Ordered by their highest permission role</li>
     *                  </ul>
     * @param isAscending Is the list in ascending or descending order
     * @return A list of users from that "page"
     * @throws IllegalArgumentException Thrown if the provided orderBy string isn't one of the valid options
     */
    public PaginatedUsersResponse getPaginatedUsers(final int offset, final int limit, final String orderBy, final boolean isAscending) throws IllegalArgumentException {
        PaginationRequestOptions requestOptions = PaginationRequestOptions.newBuilder()
                .setOffset(offset)
                .setLimit(limit)
                .setOrderBy(orderBy)
                .setIsAscendingOrder(isAscending)
                .build();

        GetPaginatedUsersRequest paginatedUsersRequest = GetPaginatedUsersRequest.newBuilder()
                .setPaginationRequestOptions(requestOptions)
                .build();
        try {
            return userAccountStub.getPaginatedUsers(paginatedUsersRequest);
        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.INVALID_ARGUMENT.getCode()) {
                // Didn't order by a valid column
                throw new IllegalArgumentException(e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Assigns a role to the given user.
     * 
     * @param userId The user's ID
     * @param role The enum object of the role getting added.
     * @return <code>true</code> if the user didn't have this role before, but does now.
     * @throws StatusException <code>NOT_FOUND</code> if the user ID points to no one.
     */
    public boolean addRoleToUser(final int userId, final UserRole role) throws StatusException {
        ModifyRoleOfUserRequest addRoleRequest = ModifyRoleOfUserRequest.newBuilder()
            .setUserId(userId)
            .setRole(role)
            .build();
        try {
            return userAccountStub.addRoleToUser(addRoleRequest).getIsSuccess();
        } catch (StatusRuntimeException e) {
            // Convert to a forced-to-catch exception
            throw Status.fromThrowable(e).asException();
        }
    }

    /**
     * Removes a role from the given user.
     * 
     * @param userId The user's ID
     * @param role The enum object of the role getting removed.
     * @return <code>true</code> if the user had this role, but doesn't any longer.
     * @throws StatusException <code>NOT_FOUND</code> if the user ID points to no one.
     */
    public boolean removeRoleFromUser(final int userId, final UserRole role) throws StatusException {
        ModifyRoleOfUserRequest removeRoleRequest = ModifyRoleOfUserRequest.newBuilder()
            .setUserId(userId)
            .setRole(role)
            .build();
        try {
            return userAccountStub.removeRoleFromUser(removeRoleRequest).getIsSuccess();
        } catch (StatusRuntimeException e) {
            // Convert to a forced-to-catch exception
            throw Status.fromThrowable(e).asException();
        }
    }
    /**
     * Sends a request to the identity provider to edit a user's details
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
     * Sends a request to the identity provider to change a user's password
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


    /**
     * Sends a request to the identity provider to upload a profile photo for a user.
     * This returns the upload observer because it is required for the client to know that the photo
     * was successfully uploaded - without it, they cannot possibly know, and thus could load the profile page
     * before their photo is saved, wrongly showing the default picture.
     * @param userId ID of the user to upload the profile photo to
     * @param file Image file to be uploaded
     * @return FileUploadObserver that the controller can query to check if the photo has been uploaded
     * @throws IOException When there is an error with reading file
     */
    public FileUploadObserver uploadUserProfilePhoto(int userId, MultipartFile file) throws IOException {
        FileUploadObserver fileUploadObserver = new FileUploadObserver();
        StreamObserver<UploadUserProfilePhotoRequest> streamObserver = userAccountServiceStub.uploadUserProfilePhoto(fileUploadObserver);

        String filetype = file.getContentType();
        if (filetype != null) {
            filetype = filetype.split("/")[1];
        }

        logger.info("Uploading profile photo with filetype: {} ", filetype);
        UploadUserProfilePhotoRequest metadata = UploadUserProfilePhotoRequest.newBuilder()
                .setMetaData(ProfilePhotoUploadMetadata.newBuilder()
                        .setUserId(userId)
                        .setFileType(filetype)
                        .build())
                .build();
        streamObserver.onNext(metadata);

        // upload file as chunk
        InputStream inputStream = file.getInputStream();
        byte[] bytes = new byte[4096];
        int size;
        while ((size = inputStream.read(bytes)) > 0) {
            UploadUserProfilePhotoRequest uploadRequest = UploadUserProfilePhotoRequest.newBuilder()
                    .setFileContent(ByteString.copyFrom(bytes, 0 , size))
                    .build();
            streamObserver.onNext(uploadRequest);
        }

        // close the stream
        inputStream.close();
        streamObserver.onCompleted();

        return fileUploadObserver;
    }

    /**
     * Sends a request to the identity provider to delete a user's profile photo
     * @param userId The id of the user to remove the photo from
     * @return A DeleteUserProfilePhotoResponse containing the success (or failure message) of the request
     */
    public DeleteUserProfilePhotoResponse deleteUserProfilePhoto(final int userId) throws StatusRuntimeException {
        DeleteUserProfilePhotoRequest deletePhotoRequest = DeleteUserProfilePhotoRequest.newBuilder()
                .setUserId(userId)
                .build();
        return userAccountStub.deleteUserProfilePhoto(deletePhotoRequest);
    }
}
