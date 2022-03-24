package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 *
 */
@Controller
public class RegisterController {

    /**
     * Displays the Registration form page
     * @return Registration page
     */
    @GetMapping("/register")
    public String showRegistrationForm() {
        return "register";
    }

    /**
     *
     * @param model
     * @return
     */
    @PostMapping("/register")
    public String submitRegistrationForm(BindingResult result, Model model) {

        if (result.hasErrors()) {
            return "register";
        }

//        userRepository.save(user);
        return "greeting";
    }

}