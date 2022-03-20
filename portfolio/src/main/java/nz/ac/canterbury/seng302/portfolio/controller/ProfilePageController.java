package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.*;
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
    private UserAccountClientService userAccountClientService;

    /**
     * Generates the profile page for the requested user
     *
     * Example URL:
     * <code>/users/123</code>
     * @param id The ID associated with a given user
     * @author Andrew Hall <amh284@uclive.ac.nz>
     */
    @GetMapping("/users/{id}")
    public String GetProfile(
            @PathVariable("id") int id,
            Model model
    ) {
        UserResponse user = userAccountClientService.getUserAccountById(id);

        ArrayList<String> errors = new ArrayList<>();
        model.addAttribute("errors", errors);

        if(user.isInitialized()) {
            model.addAttribute("profileInfo", user);
            model.addAttribute("userExists", true);
        } else {
            errors.add("Invalid ID");
        }

        // JANKY CODE ALERT!!! But it DOES display the date the user was created.
        // TODO Find a better way to display this, probably in the html file
        errors.add(getDateCreated(user.getCreated()));

        /*
        var userInfo = new TestInfo();      // TODO: Link into the GetUser service layer once that's made
        if (id < 5) {                       // A placeholder for 'invalid ID'
            model.addAttribute("profileInfo", user);
            model.addAttribute("userExists", true);
        } else {
            errors.add("Invalid ID");
        }
        */
        return "profile";
    }

    /**
     * Gets the user's date created in a readable format from the timestamp it was created at
     * @param timestamp The timestamp at which the user's account was created
     * @return A formatted string containing the date the user was created
     */
    public  String getDateCreated(Timestamp timestamp){
        LocalDateTime date = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
        // TODO Calculate how long ago the user was created and add "(X months ago)" to the returned string
        return String.format("Member since: %s %s %s", date.getDayOfMonth(), date.getMonth(), date.getYear());
    }

}
