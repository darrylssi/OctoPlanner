package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.StatusRuntimeException;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthenticateResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRegisterResponse;
import nz.ac.canterbury.seng302.shared.util.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 *
 */
@Controller
public class RegisterController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    @Autowired
    private AuthenticateClientService authenticateClientService;

    @Autowired LoginController loginController;

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
            @Valid User user,
            BindingResult result,
            HttpServletRequest request,
            HttpServletResponse response,
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
        if (result.hasErrors()) {
            return "register";
        }
        try {
            registerReply = userAccountClientService.register(username, password, firstName,
                    middleName, lastName, nickname, bio, personalPronouns, email);

            if (registerReply.getIsSuccess()) {
                AuthenticateResponse loginReply = authenticateClientService.authenticate(username, password);
                loginController.createCookie(request, response, loginReply);
                return "redirect:./users/" + loginReply.getUserId();
            } else {
                ValidationError err = registerReply.getValidationErrors(0);
                model.addAttribute("error_" + err.getFieldName(), err.getErrorText());
            }
        } catch (StatusRuntimeException e){
            model.addAttribute("IdPErrorMessage", "Error connecting to Identity Provider...");
            return "register";
        }

        model.addAttribute("registerMessage", registerReply.getMessage());
        return "register";
    }
}
