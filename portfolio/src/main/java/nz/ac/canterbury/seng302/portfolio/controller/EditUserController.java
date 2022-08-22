package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.utils.Base64DecodedMultipartFile;
import nz.ac.canterbury.seng302.portfolio.model.ErrorType;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.FileUploadObserver;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Objects;

/**
 * Controller to handle requests for editing a user.
 */
@Controller
public class EditUserController extends PageController{

    private static final Logger logger = LoggerFactory.getLogger(EditUserController.class);

    @Autowired
    private UserAccountClientService userAccountClientService;

    private static final String EDIT_USER_TEMPLATE = "editUser";
    private static final String REDIRECT_TO_PROFILE = "redirect:../";
    private static final String DEFAULT_PROFILE_PICTURE_NAME = "default-pfp.jpg";

    /**
     * Check that the current user has sufficient permissions, then populate the page accordingly
     */
    private void editHandler(Model model, int id, AuthState principal) {
        UserResponse userResponse = userAccountClientService.getUserAccountById(id);

        String currentUserId = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");

        boolean isCurrentUser = (currentUserId.equals(Integer.toString(id)) &&
                !currentUserId.equals("NOT FOUND"));
        model.addAttribute("isCurrentUser", isCurrentUser);

        if(userResponse == null) {
            configureError(model, ErrorType.NOT_FOUND, "/users" + id + EDIT_USER_TEMPLATE);
        } else if(!isCurrentUser) {
            configureError(model, ErrorType.ACCESS_DENIED, "/users" + id + EDIT_USER_TEMPLATE);
        } else {
            // Gets the current user's username
            model.addAttribute("profileInfo", userResponse);
            // True if user has their own pfp, false if they don't. Used to render the button to delete your pfp or not.
            model.addAttribute("userHasProfilePhoto", !userResponse.getProfileImagePath().contains(DEFAULT_PROFILE_PICTURE_NAME));
            model.addAttribute("userExists", true);
            model.addAttribute("fullName", ProfilePageController.getFullName(
                    userResponse.getFirstName(), userResponse.getMiddleName(),
                    userResponse.getLastName()));
            model.addAttribute("userId", Integer.toString(id));
            model.addAttribute("dateCreated",
                    ProfilePageController.getDateCreated(userResponse.getCreated()));
        }
    }

    /**
     * Get user details from the database
     * @param principal Authenticated user
     * @param id ID of the user to be edited
     * @param user User object
     * @param model Parameters sent to thymeleaf
     * @return Edit user template
     */
    @GetMapping("/users/{id}/edit")
    public String edit(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            User user,
            Model model
    ) {
        editHandler(model, id, principal);
        return EDIT_USER_TEMPLATE;
    }

    /**
     * Post request to edit a user.
     * @param user User object
     * @param principal Authenticated user
     * @param id ID of the use to be edited
     * @param result Holds validation errors
     * @param firstName (New) First name
     * @param middleName (New) Middle name
     * @param lastName (New) Last name
     * @param nickname (New) Nickname
     * @param bio (New) Bio
     * @param personalPronouns (New) Personal pronouns
     * @param email (New) Email
     * @param model Parameters sent to the thymeleaf template
     * @return User profile if all details are valid, otherwise edit user template
     */
    @PostMapping(value = "/users/{id}/edit", params = {"firstName", "middleName", "lastName",
            "nickname", "bio", "personalPronouns", "email"})
    public String edit(
            User user,
            @AuthenticationPrincipal AuthState principal,
            @PathVariable int id,
            BindingResult result,
            @RequestParam(name="firstName") String firstName,
            @RequestParam(name="middleName", required=false) String middleName,
            @RequestParam(name="lastName") String lastName,
            @RequestParam(name="nickname", required=false) String nickname,
            @RequestParam(name="bio", required=false) String bio,
            @RequestParam(name="personalPronouns", required=false) String personalPronouns,
            @RequestParam(name="email") String email,
            Model model
    ) {
        editHandler(model, id, principal);

        /* Set (new) user details to the corresponding user */
        EditUserResponse editReply;
        if (result.hasErrors()) {
            return EDIT_USER_TEMPLATE;
        }
        try {
            editReply = userAccountClientService.editUser(id, firstName, middleName,
                    lastName, nickname, bio, personalPronouns, email);

            if (editReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return REDIRECT_TO_PROFILE + id;
            } else {
                ValidationError err = editReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("editErrorMessage", "Unknown error updating details");
            return EDIT_USER_TEMPLATE;
        }

        model.addAttribute("editMessage", editReply.getMessage());
        return EDIT_USER_TEMPLATE;
    }

    /**
     * Post request to change a user's password
     * @param user User object
     * @param id ID of the user to change password of
     * @param principal Authenticated user
     * @param result Holds validation errors
     * @param oldPassword Old password of the user
     * @param newPassword New password of the user
     * @param confirmPassword To confirm new password, should be the same as new password.
     * @param model Parameters sent to thymeleaf template
     * @return User profile if no errors, otherwise edit user template
     */
    @PostMapping(value = "/users/{id}/edit", params = {"oldPassword", "password",
            "confirmPassword"})
    public String changePassword(
            User user,
            @PathVariable int id,
            @AuthenticationPrincipal AuthState principal,
            BindingResult result,
            @RequestParam(name="oldPassword") String oldPassword,
            @RequestParam(name="password") String newPassword,
            @RequestParam(name="confirmPassword") String confirmPassword,
            Model model
    ) {
        editHandler(model, id, principal);

        /* Set (new) user details to the corresponding user */
        ChangePasswordResponse changeReply;
        if (result.hasErrors()) {
            return EDIT_USER_TEMPLATE;
        }
        if(!newPassword.equals(confirmPassword)) {
            model.addAttribute("error_PasswordsEqual", "New and confirm passwords do not match");
            return EDIT_USER_TEMPLATE;
        }
        try {
            changeReply = userAccountClientService.changeUserPassword(id, oldPassword, newPassword);

            if (changeReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return REDIRECT_TO_PROFILE + id;
            } else {
                ValidationError err = changeReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("pwErrorMessage", "Unknown error changing password");
            return EDIT_USER_TEMPLATE;
        }

        model.addAttribute("pwMessage", changeReply.getMessage());
        return EDIT_USER_TEMPLATE;
    }

    /**
     * A post request for uploading a base64 string of an image file (a cropped profile photo in this case).
     * This is just getting a string representing the cropped image from a hidden form input
     * https://stackoverflow.com/questions/18381928/how-to-convert-byte-array-to-multipartfile
     * @param user User object to be edited
     * @param id ID of said user
     * @param principal Authenticated user
     * @param result Holds the validation result
     * @param base64ImageString The base64 string representation of the image
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Profile page of the user if photo is valid, otherwise, edit user page
     * @throws IOException When there is an error uploading the photo
     */
    @PostMapping(value = "/users/{id}/edit", params = {"imageString"})
    public String editImage(
            User user,
            @PathVariable int id,
            @AuthenticationPrincipal AuthState principal,
            BindingResult result,
            @RequestParam(name="imageString") String base64ImageString,
            Model model
    ) throws IOException, InterruptedException {
        editHandler(model, id, principal);
        MultipartFile file = new Base64DecodedMultipartFile(base64ImageString);

        model.addAttribute("file", file);
        if (isValidImageFile(file) && file.getSize() > 0) {
            FileUploadObserver fileUploadObserver = userAccountClientService.uploadUserProfilePhoto(id, file);
            waitForPhotoResponse(fileUploadObserver);
            if (Boolean.TRUE.equals(fileUploadObserver.isUploadSuccessful())) { // Sonarlint wanted this
                return REDIRECT_TO_PROFILE + id;
            } else {
                model.addAttribute("error_InvalidPhoto", "Something went wrong uploading your photo: " + fileUploadObserver.getUploadMessage());
                return EDIT_USER_TEMPLATE;
            }
        } else {
            model.addAttribute("error_InvalidPhoto", "Invalid file. Profile photos must be of type .jpeg, .jpg, or .png, and must not be empty.");
            return EDIT_USER_TEMPLATE;
        }
    }

    /**
     * Waits until the uploaded image has been saved / rejected / caused an error, or 500ms have passed.
     * @param observer the FileUploadObserver for this upload
     * @throws InterruptedException if a thread interrupts the current one (?)
     */
    private synchronized void waitForPhotoResponse(FileUploadObserver observer) throws InterruptedException {
        int count = 0;
        while ((observer.isUploadSuccessful() == null || Objects.equals(observer.getUploadMessage(), "")) && count < 500) {
            count++;
            wait(1);
        }
        logger.debug("EditUserController waited for {} ms to receive a photo response.", count);
    }

    /**
     * Checks whether the provided MultipartFile has a content type of image/jpeg or image/png.
     * @param file the MultipartFile in question
     * @return true or false
     */
    private static boolean isValidImageFile(MultipartFile file) {
        String mimeType = file.getContentType();
        return (mimeType != null && (mimeType.equalsIgnoreCase("image/jpeg") ||
                mimeType.equalsIgnoreCase("image/png")));
    }

    /**
     * The post request for removing an uploaded image file.
     * @param user User object to be edited
     * @param id ID of the user to be edited
     * @param principal Authenticated user
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Profile page of the user if photo is successfully removed, otherwise, edit user page
     */
    @PostMapping(value = "/users/{id}/edit")
    public String removeUpload(
            User user,
            @PathVariable int id,
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        DeleteUserProfilePhotoResponse deleteReply;
        // Check the user is authorised, then send a request to the UserAccountClientService
        editHandler(model, id, principal);

        try {
            deleteReply = userAccountClientService.deleteUserProfilePhoto(id);

            if (deleteReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return REDIRECT_TO_PROFILE + id;
            } else {
                model.addAttribute("error_DeletePhoto", deleteReply.getMessage());
            }
        } catch (StatusRuntimeException e) {
            model.addAttribute("error_DeletePhoto", "Unknown error deleting profile photo");
        }
        return EDIT_USER_TEMPLATE;
    }

}
