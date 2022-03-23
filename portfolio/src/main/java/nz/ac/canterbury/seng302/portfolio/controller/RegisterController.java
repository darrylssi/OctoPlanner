package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 *
 */
@Controller
public class RegisterController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    /**
     * Displays the Registration form page
     * @return Registration page
     */
    @GetMapping("/register")
    public String register(User user) {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            User user,
            @RequestParam(name="username") String username,
            @RequestParam(name="password") String password,
            @RequestParam(name="firstName") String firstName,
            @RequestParam(name="middleName") String middleName,
            @RequestParam(name="lastName") String lastName,
            @RequestParam(name="nickname") String nickname,
            @RequestParam(name="bio") String bio,
            @RequestParam(name="personalPronouns") String personalPronouns,
            @RequestParam(name="email") String email,
            Model model
    ) {

        UserRegisterResponse registerReply;
        try {
            registerReply = userAccountClientService.register(username, password, firstName,
                    middleName, lastName, nickname, bio, personalPronouns, email);
        } catch (StatusRuntimeException e){
            model.addAttribute("loginMessage", "Error connecting to Identity Provider...");
            return "register";
        }
/*
        if (registerReply.getIsSuccess()) {
            return "redirect:/greeting?name=" + username;   // Go back to login page? Or user profile page?
        }
 */
        model.addAttribute("registerMessage", registerReply.getMessage());
        return "register";
    }
}