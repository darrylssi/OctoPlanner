package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.EditUserResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.ChangePasswordResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import nz.ac.canterbury.seng302.portfolio.controller.ProfilePageController;

@Controller
public class EditUserController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    @GetMapping("/users/{id}/edit")
    public String edit(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            Model model
    ) {
        UserResponse user = userAccountClientService.getUserAccountById(id);

        ArrayList<String> errors = new ArrayList<>();
        model.addAttribute("errors", errors);

        String currentUserId = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");
        model.addAttribute("isCurrentUser", (currentUserId.equals(Integer.toString(id)) && !currentUserId.equals("NOT FOUND")));

        if(user.hasCreated()) {
            model.addAttribute("profileInfo", user);
            model.addAttribute("userExists", true);
            model.addAttribute("fullName", ProfilePageController.getFullName(
                    user.getFirstName(), user.getMiddleName(),  user.getLastName()));
            model.addAttribute("userId", Integer.toString(id));
            model.addAttribute("dateCreated",
                    ProfilePageController.getDateCreated(user.getCreated()));
        } else {
            errors.add("Invalid ID");
        }
        return "editUser";
    }

    @PostMapping("/users/{id}/edit")
    public String edit(
            User user,
            @PathVariable int id,
            BindingResult result,
            @RequestParam(name="firstName") String firstName,
            @RequestParam(name="middleName", required=false) String middleName,
            @RequestParam(name="lastName") String lastName,
            @RequestParam(name="nickname", required=false) String nickname,
            @RequestParam(name="bio", required=false) String bio,
            @RequestParam(name="pronouns", required=false) String personalPronouns,
            @RequestParam(name="email") String email,
            Model model
    ) {
        
        /* Set (new) user details to the corresponding user */
        EditUserResponse editReply;
        if (result.hasErrors()) {
            return "/users/" + id + "/edit";
        }
        try {
            editReply = userAccountClientService.editUser(id, firstName, middleName,
                    lastName, nickname, bio, personalPronouns, email);

            if (editReply.getIsSuccess()) {
                /* Redirect to profile page when done */
                return "redirect:/users/" + id;
            }
        } catch (StatusRuntimeException e){
            //TODO: Handle errors consistently on this page. add error attr here
            return "redirect:/users/" + id + "/edit";
        }

        //TODO: handle errors consistently. add error attr here
        return "redirect:/users/" + id + "/edit";
    }

    @PostMapping("/users/{id}/change-password")
    public String changePassword(
            User user,
            @PathVariable int id,
            BindingResult result,
            @RequestParam(name="oldPw") String oldPassword,
            @RequestParam(name="newPw") String newPassword,
            @RequestParam(name="confirmPw") String confirmPassword,
            Model model
    ) {
        
        /* Set (new) user details to the corresponding user */
        ChangePasswordResponse changeReply;
        if (result.hasErrors()) {
            return "/users/" + id + "/edit";
        }
        if(oldPassword == newPassword) {
            try {
                changeReply = userAccountClientService.changeUserPassword(id, oldPassword, newPassword);

                if (changeReply.getIsSuccess()) {
                    /* Redirect to profile page when done */
                    return "redirect:/users/" + id;
                }
            } catch (StatusRuntimeException e){
                //TODO: Handle errors consistently across this page. add error attr here
                return "redirect:/users/" + id + "/edit";
            }
        }

        //TODO: handle errors consistently. add error attr here
        return "redirect:/users/" + id + "/edit";
    }
}
