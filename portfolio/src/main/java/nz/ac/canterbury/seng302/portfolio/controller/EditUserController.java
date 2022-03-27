package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EditUserController {

    @Autowired
    private UserAccountClientService userAccountClientService;

    @GetMapping("/users/{id}/edit")
    public String edit(
            @PathVariable int id) {

        // TODO replace with something other than a test user
        if(id == 1){
            userAccountClientService.editUser(id, "editFirst", "editMiddle",
                    "editLast", "editNick", "editBio",
                    "edit/edit", "edit@Email");
            userAccountClientService.changeUserPassword(id, "abc1234", "changed");
        }else if(id == 2){
            userAccountClientService.editUser(id, "e", "e",
                    "e", "e", "e",
                    "e", "editEmail");
            userAccountClientService.changeUserPassword(id, "somethingwrong", "changed");
        }

        return "editUser";
    }

    @PostMapping("/user/{id}/edit")
    public String edit(
            User user,
            @PathVariable int id,
            @RequestParam(name="firstName") String firstName,
            @RequestParam(name="middleName") String middleName,
            @RequestParam(name="lastName") String lastName,
            @RequestParam(name="nickname") String nickname,
            @RequestParam(name="bio") String bio,
            @RequestParam(name="personalPronouns") String personalPronouns,
            @RequestParam(name="email") String email,
            Model model
    ) {
        return "editUser";
    }
}
