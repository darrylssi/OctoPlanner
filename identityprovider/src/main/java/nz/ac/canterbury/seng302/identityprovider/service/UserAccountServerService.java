package nz.ac.canterbury.seng302.identityprovider.service;

import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import nz.ac.canterbury.seng302.identityprovider.model.User;
import nz.ac.canterbury.seng302.identityprovider.repository.UserRepository;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatus;
import nz.ac.canterbury.seng302.shared.util.FileUploadStatusResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.List;

/**
 * This class contains server-side methods for dealing with user accounts in the IDP, such as
 * methods dealing with profile photos, registering, roles, paginated users, etc.
 */
@GrpcService
public class UserAccountServerService extends UserAccountServiceGrpc.UserAccountServiceImplBase {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountServerService.class);

    private static final String USER_PHOTO_FILENAME = "_photo.";
    private static final String USER_PHOTO_FORMAT = "jpg";
    private static final String USER_PHOTO_SUFFIX = USER_PHOTO_FILENAME + USER_PHOTO_FORMAT;
    private static final int USER_PHOTO_DIMENSIONS = 200;

    private static final BCryptPasswordEncoder encoder =  new BCryptPasswordEncoder();

    @Value("${profile-image-folder}")
    private Path profileImageFolder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProfilePhotoService profilePhotoService;

    @Autowired
    private ValidationService validator;

    /**
     * Creates a request to upload a profile photo for a user, following this tutorial:
     * https://www.vinsguru.com/grpc-file-upload-client-streaming/ so far it's just copied from this one
     * @param responseObserver Observable stream of messages
     * @return FileUploadStatusResponse with the status of the upload
     */
    @Override
    public StreamObserver<UploadUserProfilePhotoRequest> uploadUserProfilePhoto(StreamObserver<FileUploadStatusResponse> responseObserver) {
        return new StreamObserver<>() {
            // declare upload context variables
            ByteArrayOutputStream byteWriter = new ByteArrayOutputStream();
            FileUploadStatus status = FileUploadStatus.IN_PROGRESS; // different to the tutorial
            String fileName; // file name of the image, e.g. "5_photo."
            String fileExtension; // this is the type of file uploaded - we only save JPGs, so don't use this to save!
            String filePath; // the actual path to save the image to, including the file name and type (.JPG)

            /**
             * Processes a file upload request, and saves the file contents to a ByteArrayOutputStream
             * so that it can be verified before being saved.
             * @param userProfilePhotoUploadRequest the file upload request object
             */
            @Override
            public void onNext(UploadUserProfilePhotoRequest userProfilePhotoUploadRequest) {
                try {
                    if (userProfilePhotoUploadRequest.hasMetaData()) {
                        fileName = userProfilePhotoUploadRequest.getMetaData().getUserId() + USER_PHOTO_FILENAME;
                        fileExtension = userProfilePhotoUploadRequest.getMetaData().getFileType().strip();
                        filePath = getFilePath(userProfilePhotoUploadRequest);
                        logger.info("Got upload profile request for user with id {}, filetype of {}", userProfilePhotoUploadRequest.getMetaData().getUserId(), fileExtension);
                    } else {
                        writeFile(byteWriter, userProfilePhotoUploadRequest.getFileContent());
                    }
                } catch (IOException e) {
                    this.onError(e);
                }
            }

            /**
             * Called when an error is encountered while uploading an image.
             * Sets the status of the upload to failed and calls onCompleted.
             * @param t the thrown exception which caused the error
             */
            @Override
            public void onError(Throwable t) {
                logger.error("Error uploading profile photo: {}", t.getMessage());
                status = FileUploadStatus.FAILED;
                this.onCompleted();
            }

            /**
             * Called when the file upload stops, either because it completed or an error was encountered.
             * Checks the validity of the image and saves it to a file if it is valid.
             * If the image can't be read, the upload fails.
             * Creates a file upload response to send back to the user with a success/fail message.
             */
            @Override
            public void onCompleted() {
                FileUploadStatusResponse.Builder response = FileUploadStatusResponse.newBuilder();

                ByteArrayInputStream inputStream = new ByteArrayInputStream(byteWriter.toByteArray());

                try {
                    BufferedImage image = ImageIO.read(inputStream);

                    if (image.getWidth() == USER_PHOTO_DIMENSIONS && image.getHeight() == USER_PHOTO_DIMENSIONS) {
                        ImageIO.write(image, "jpg", new File(filePath));
                        logger.info("Saved profile image {} with dimensions {} x {}", filePath, image.getWidth(), image.getHeight());
                        status = FileUploadStatus.SUCCESS;
                    } else { // invalid
                        logger.info("Image {} has invalid dimensions {} x {} and was not saved", filePath, image.getWidth(), image.getHeight());
                        response.setMessage(String.format("Image has invalid dimensions %d x %d when they must be %d x %<d", image.getWidth(), image.getHeight(), USER_PHOTO_DIMENSIONS));
                        status = FileUploadStatus.FAILED;
                    }
                } catch (IOException | NullPointerException e) { // thrown by ImageIO.read and .write - will happen if the file isn't an image
                    logger.error(String.format("Error reading or writing uploaded image: %s", Arrays.toString(e.getStackTrace())));
                    response.setMessage("Error saving image: could not read image from file. Make sure the image is not corrupted.");
                    status = FileUploadStatus.FAILED;
                }

                closeFile(byteWriter);

                status = FileUploadStatus.IN_PROGRESS.equals(status) ? FileUploadStatus.SUCCESS : status;

                if (status == FileUploadStatus.SUCCESS) {
                    deleteIncorrectPhotoFileType(fileName, fileExtension);
                    response.setMessage("Successfully uploaded profile photo.");
                }

                response.setStatus(status);
                responseObserver.onNext(response.build());
                responseObserver.onCompleted();
            }
        };
    }

    /**
     * Attempts to delete any uploaded file that was somehow saved that is not a jpg.
     * @param fileName the saved file name WITHOUT extension, e.g. "5_photo."
     * @param originalFileExtension the original filetype of the upload - could be png, zip, pdf, jpg, or anything else
     */
    private void deleteIncorrectPhotoFileType(String fileName, String originalFileExtension) {
        // JPEG, not JPG, is what the file type is set as in the upload
        if (!originalFileExtension.equalsIgnoreCase("jpeg")) {
            try {
                Boolean success = Files.deleteIfExists(profileImageFolder.resolve(fileName + originalFileExtension));
                logger.info("Attempted to delete potentially invalid file \"{}{}\". File detected and deleted: {}",
                        fileName, originalFileExtension, success);
            } catch (IOException e) {
                logger.info("Error deleting invalid file \"{}\": {}", fileName + originalFileExtension, e.getMessage());
            }
        }
    }

    /**
     * Unlinks the user from their profile photo and deletes it from storage.
     * @param request A request object containing the user ID
     */
    @Override
    public void deleteUserProfilePhoto(DeleteUserProfilePhotoRequest request, StreamObserver<DeleteUserProfilePhotoResponse> responseObserver) {
        DeleteUserProfilePhotoResponse.Builder reply = DeleteUserProfilePhotoResponse.newBuilder();
        String filename = request.getUserId() + USER_PHOTO_SUFFIX;

        try {
            if (Files.exists(profileImageFolder.resolve(filename))) {
                // Left as a deleteIfExists in case of two nearly simultaneous requests
                Files.deleteIfExists(profileImageFolder.resolve(filename));
                reply
                        .setIsSuccess(true)
                        .setMessage("User photo deleted successfully");
                logger.info("Deleted profile photo for user {}", request.getUserId());
            } else {
                reply
                        .setIsSuccess(false)
                        .setMessage("User does not have a profile photo uploaded");
                logger.info("Didn't delete profile photo for user {} as they do not have one", request.getUserId());
            }
        } catch (IOException err) {
            reply
                    .setIsSuccess(false)
                    .setMessage("Unable to delete user photo: " + err.getMessage());
            logger.error("Error deleting photo for user {}: {}", request.getUserId(), err.getStackTrace());
        }

        responseObserver.onNext(reply.build());
        responseObserver.onCompleted();
    }

    /**
     * Gets the path, in the user profile photos folder, for the photo in the provided upload request.
     * The file type is always set to JPG, per USER_PHOTO_FORMAT, regardless of what type the initial file is.
     * @param request A request object containing the user ID and the file's information
     * @return a string object representing the path to the photo in the request
     */
    private String getFilePath(UploadUserProfilePhotoRequest request) {
        String fileName = request.getMetaData().getUserId() + USER_PHOTO_SUFFIX;
        return profileImageFolder.resolve(fileName).toString();
    }

    /**
     * Writes the file content to the OutputStream. Copied from tutorial; see uploadUserProfilePhoto.
     * @param writer Output stream
     * @param content File content
     * @throws IOException When there is an error writing the content to the output stream
     */
    private void writeFile(OutputStream writer, ByteString content) throws IOException {
        writer.write(content.toByteArray());
        writer.flush();
    }

    /**
     * Closes the output stream. Copied from tutorial; see uploadUserProfilePhoto.
     * @param writer Output stream
     */
    private void closeFile(OutputStream writer) {
        try {
            writer.close();
        } catch (Exception e) {
            logger.error("Error closing writer during photo upload: {}", (Object) e.getStackTrace());
        }
    }

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
                logger.error("Register user {} : {} - {}",
                        request.getUsername(), error.getFieldName(), error.getErrorText());
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

        userRepository.save(user);  // Saves the user object to the database

        reply
                .setIsSuccess(true)
                .setNewUserId(user.getId())
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

        User user = userRepository.findById(request.getId());
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
            if (orderBy.equals("role")) {
                    // Ordering by "role" means order by highest role ({STUDENT, COURSE_ADMIN} =>
                    // highest is COURSE_ADMIN).
                    // JPA allows ordering by joined tables, HOWEVER it generates SQL incompatible
                    // with H2. So, we have
                    // to bring the entire table into memory & sort here until that's migrated.
                    // Update: MariaDB is used on the VMs yeah, but we still use H2 when developing
                    // on our machines. So this stays.
                paginatedUsers = paginatedUsersOrderedByRole(offset, limit, isAscending);
            } else {
                paginatedUsers = userService.getUsersPaginated(offset, limit, orderBy, isAscending);
            }
        } catch (IllegalArgumentException e) { // `orderBy` wasn't a valid value.
            Throwable statusError = Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException();
            responseObserver.onError(statusError);
            return;
        }

        List<UserResponse> userResponses = paginatedUsers.stream().map(this::buildUserResponse).toList();
        int numUsersInDatabase = (int) userRepository.count();
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
        ArrayList<User> users = new ArrayList<>(userService.getAllUsers());
        if (isAscending)
            users.sort(Comparator.comparingInt(a -> a.highestRole().getNumber()));
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
     * <p>Holds shared functionality for adding/deleting user roles.</p>
     *
     * Sends a successful reply if the operation was successful
     * Gives a Status.NOT_FOUND error if the user ID is invalid
     */
    private void modifyUserRole(
            ModifyRoleOfUserRequest request,
            StreamObserver<UserRoleChangeResponse> responseObserver,
            boolean delete) {
        UserRoleChangeResponse.Builder reply = UserRoleChangeResponse.newBuilder();
        int userId = request.getUserId();
        UserRole role = request.getRole();

        try {
            boolean success;
            String successMessage = "Role successfully added";
            String failMessage = "Couldn't add role: User already had this role.";

            if (delete) {
                success = userService.removeRoleFromUser(userId, role);
                successMessage = "Role successfully removed";
                failMessage = "Couldn't remove role: User didn't have this role";
            } else {
                success = userService.addRoleToUser(userId, role);
            }

            reply.setIsSuccess(success);
            if (success)
                reply.setMessage(successMessage);
            else
                reply.setMessage(failMessage);
            responseObserver.onNext(reply.build());
            responseObserver.onCompleted();
        } catch (NoSuchElementException e) {
            // The user ID pointing to a non-existent user
            responseObserver.onError(Status.NOT_FOUND.withDescription("User with that ID doesn't exist").asRuntimeException());
        }
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
        modifyUserRole(request, responseObserver, false);
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
        modifyUserRole(request, responseObserver, true);
    }

    /**
     * Changes a users details
     * @param request Contains the details of the user to change, and what to change their details to
     */
    @Override
    public void editUser(EditUserRequest request, StreamObserver<EditUserResponse> responseObserver) {
        logger.info("editUser() has been called");
        EditUserResponse.Builder reply = EditUserResponse.newBuilder();

        User user = userRepository.findById(request.getUserId()); // Attempts to get the user from the database

        List<ValidationError> errors = validator.validateEditUserRequest(request, user);

        if(!errors.isEmpty()) { // If there are errors in the request

            for (ValidationError error : errors) {
                logger.error("Edit user {} : {} - {}",
                        request.getUserId(), error.getFieldName(), error.getErrorText());
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

        userRepository.save(user);  // Saves the user object to the database
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

        User user = userRepository.findById(request.getUserId()); // Attempts to get the user from the database

        List<ValidationError> errors = validator.validateChangePasswordRequest(request, user);

        if(!errors.isEmpty()) { // If there are errors in the request

            for (ValidationError error : errors) {
                logger.error("Change password of user {} : {} - {}",
                        request.getUserId(), error.getFieldName(), error.getErrorText());
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

        userRepository.save(user);  // Saves the user object to the database
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
    private UserResponse buildUserResponse(User user) {
        int id = user.getId();
        ArrayList<UserRole> sortedRoles = new ArrayList<>(user.getRoles());
        sortedRoles.sort(Comparator.naturalOrder());

        UserResponse.Builder userResponse = UserResponse
            .newBuilder()
                .setId(id)
                .setUsername(user.getUsername())
                .setFirstName(user.getFirstName())
                .setMiddleName(user.getMiddleName())
                .setLastName(user.getLastName())
                .setNickname(user.getNickname())
                .setBio(user.getBio())
                .setPersonalPronouns(user.getPersonalPronouns())
                .setEmail(user.getEmail())
                .setProfileImagePath(profilePhotoService.getUserProfileImageUrl(id))
                .addAllRoles(user.getRoles())
                .setCreated(Timestamp.newBuilder()  // Converts Instant to protobuf.Timestamp
                    .setSeconds(user.getCreated().getEpochSecond())
                    .setNanos(user.getCreated().getNano()));

        return userResponse.build();
    }
}
