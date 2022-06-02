package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class EditUserController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    private static final String EDIT_USER = "editUser";
    private static final String REDIRECT = "redirect:../";

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

        if(!userResponse.hasCreated()) {
            //TODO: send to error page
            model.addAttribute("editErrorMessage", "Invalid id");
        } else if(!isCurrentUser) {
            //TODO: send to error page
            model.addAttribute("editErrorMessage", "You may not edit other users");
        } else {
            model.addAttribute("profileInfo", userResponse);
            model.addAttribute("userExists", true);
            model.addAttribute("fullName", ProfilePageController.getFullName(
                    userResponse.getFirstName(), userResponse.getMiddleName(),
                    userResponse.getLastName()));
            model.addAttribute("userId", Integer.toString(id));
            model.addAttribute("dateCreated",
                    ProfilePageController.getDateCreated(userResponse.getCreated()));
        }
    }

    @GetMapping("/users/{id}/edit")
    public String edit(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            User user,
            Model model
    ) {
        editHandler(model, id, principal);
        return EDIT_USER;
    }

    @PostMapping("/users/{id}/edit")
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
            return EDIT_USER;
        }
        try {
            editReply = userAccountClientService.editUser(id, firstName, middleName,
                    lastName, nickname, bio, personalPronouns, email);

            if (editReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return REDIRECT + id;
            } else {
                ValidationError err = editReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("editErrorMessage", "Unknown error updating details");
            return EDIT_USER;
        }

        model.addAttribute("editMessage", editReply.getMessage());
        return EDIT_USER;
    }

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
            return EDIT_USER;
        }
        if(!newPassword.equals(confirmPassword)) {
            model.addAttribute("error_PasswordsEqual", "New and confirm passwords do not match");
            return EDIT_USER;
        }
        try {
            changeReply = userAccountClientService.changeUserPassword(id, oldPassword, newPassword);

            if (changeReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return REDIRECT + id;
            } else {
                ValidationError err = changeReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("pwErrorMessage", "Unknown error changing password");
            return EDIT_USER;
        }

        model.addAttribute("pwMessage", changeReply.getMessage());
        return EDIT_USER;
    }

    /**
     * Post request for uploading a selected image file.
     * @param user User object to be edited
     * @param id ID of the user to be edited
     * @param principal Authenticated user
     * @param result Holds the validation result
     * @param file Image file to be uploaded
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Profile page of the user if photo is valid, otherwise, edit user page
     * @throws IOException When there is an error uploading the photo
     */
    @PostMapping(value = "/users/{id}/edit", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public String uploadPhoto(
            User user,
            @PathVariable int id,
            @AuthenticationPrincipal AuthState principal,
            BindingResult result,
            @RequestPart(name="file") MultipartFile file,
            Model model
    ) throws IOException {
        editHandler(model, id, principal);
        model.addAttribute("file", file);
        if (isValidImageFile(file) && file.getSize() > 0) {
            userAccountClientService.uploadUserProfilePhoto(id, file);
            return REDIRECT + id;
        } else {
            model.addAttribute("error_InvalidPhoto", "Invalid file. Profile photos must be of type .jpeg, .jpg, or .png, and must not be empty.");
            return EDIT_USER;
        }
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

    @PostMapping(value = "/users/{id}/delete-profile-photo")
    public String removeUpload(
            User user,
            @PathVariable int id,
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        DeleteUserProfilePhotoResponse deleteReply;

        /** Check the user is authorised, then send a request to the UserAccountCLientService */
        editHandler(model, id, principal);

        try {
            deleteReply = userAccountClientService.deleteUserProfilePhoto(id);

            if (deleteReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return REDIRECT + id;
            } else {
                model.addAttribute("error_DeletePhoto", deleteReply.getMessage());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("error_DeletePhoto", "Unknown error deleting profile photo");
        }

        return EDIT_USER;
    }

}
