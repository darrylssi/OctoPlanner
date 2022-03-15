package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.AuthenticateClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;

/**
 * InnerProfilePageController
 */
class TestInfo {

    static String username = "ConcreteDesk";
    static String firstName = "Tony";
    static String middleName = null;
    static String lastName = "Asus";
    static String email = "i-eat-cement@ucmail.gov";
    static String pronouns = null;//"He/him";
    static String bio = null;/* "The owner of a lonely heart!\n\n"
                        + "Owner of a lonely heart!\n"
                        + "(Much better than a-)\n"
                        + "Owner of a broken heart!";*/

    public static String getUsername() {
        return username;
    }

    public static String getFirstName() {
        return firstName;
    }

    public static String getMiddleName() {
        return middleName;
    }

    public static String getLastName() {
        return lastName;
    }

    public static String getEmail() {
        return email;
    }

    public static String getPronouns() {
        return pronouns;
    }

    public static String getBio() {
        return bio;
    }

    public static String getFullName() {
        if (middleName != null)
            return String.format("%s %s %s", firstName, middleName, lastName);
        else
            return String.format("%s %s", firstName, lastName);
    }


}

/**
 * Controller class for the profile page.
 *
 * @since 4th March 2022
 */
@Controller
public class ProfilePageController {

    @Autowired
    private AuthenticateClientService authenticateClientService;

    /**
     * Generates the profile page for the requested user
     *
     * Example URL:
     * <code>/profile?id=123</code>
     * @param id The ID associated with a given user
     * @author Andrew Hall <amh284@uclive.ac.nz>
     */
    @GetMapping("/profile/{id}")
    public String GetProfile(
            @PathVariable("id") int id,
            Model model
    ) {
        ArrayList<String> errors = new ArrayList<>();
        model.addAttribute("errors", errors);
        var userInfo = new TestInfo();      // TODO: Link into the GetUser service layer once that's made
        if (id > 5) {                       // A placeholder for 'invalid ID'
            model.addAttribute("profileInfo", userInfo);
            model.addAttribute("userExists", true);
        } else {
            errors.add("Invalid ID");
        }
        return "profile";
    }

}
