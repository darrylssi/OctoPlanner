package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.*;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class EditUserController {

    @Autowired
    private UserAccountClientService userAccountClientService;

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
        return "editUser";
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
            return "editUser";
        }
        try {
            editReply = userAccountClientService.editUser(id, firstName, middleName,
                    lastName, nickname, bio, personalPronouns, email);

            if (editReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return "redirect:../" + id;
            } else {
                ValidationError err = editReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("editErrorMessage", "Unknown error updating details");
            return "editUser";
        }

        model.addAttribute("editMessage", editReply.getMessage());
        return "editUser";
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
            return "editUser";
        }
        if(!newPassword.equals(confirmPassword)) {
            model.addAttribute("error_PasswordsEqual", "New and confirm passwords do not match");
            return "editUser";
        }
        try {
            changeReply = userAccountClientService.changeUserPassword(id, oldPassword, newPassword);

            if (changeReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return "redirect:../" + id;
            } else {
                ValidationError err = changeReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("pwErrorMessage", "Unknown error changing password");
            return "editUser";
        }

        model.addAttribute("pwMessage", changeReply.getMessage());
        return "editUser";
    }

    /**
     * Post request for uploading a selected image file.
     * @param id ID of the user to be edited
     * @param file Image file to be uploaded
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Profile page of the user
     * @throws IOException When there is an error uploading the photo
     */
    @PostMapping(value = "/users/{id}/upload")
    public String uploadPhoto(
            @PathVariable int id,
            @RequestParam("file") MultipartFile file,
            Model model
    ) throws IOException {
        model.addAttribute("file", file);
        if (isValidImageFile(file)) {
            userAccountClientService.uploadUserProfilePhoto(id, file);
        }
        return "redirect:../" + id;
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

}
