package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.thymeleaf.util.StringUtils;

import java.time.*;
import java.util.ArrayList;

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
            model.addAttribute("fullName", getFullName(
                    user.getFirstName(), user.getMiddleName(),  user.getLastName()));
            model.addAttribute("dateCreated", getDateCreated(user.getCreated()));
        } else {
            errors.add("Invalid ID");
        }
        return "profile";
    }

    /**
     * Combines a user's names into one string
     * @param firstName The user's first name
     * @param middleName The user's middle name
     * @param lastName The user's last name
     * @return A formatted string containing the user's full name, only including the middle name if they have one
     */
    public static String getFullName(String firstName, String middleName, String lastName) {
        if (middleName != null)
            return String.format("%s %s %s", firstName, middleName, lastName);
        else
            return String.format("%s %s", firstName, lastName);
    }

    /**
     * Gets the user's date created in a readable format from the timestamp it was created at
     * @param timestamp The timestamp at which the user's account was created
     * @return A formatted string containing the date the user was created
     */
    public static String getDateCreated(Timestamp timestamp){
        LocalDateTime dateTime = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                .atOffset(ZoneOffset.UTC)
                .toLocalDateTime();
        LocalDate date = dateTime.toLocalDate();

        // Calculate how long ago the user was created
        Period period = Period.between(date, LocalDate.now());

        String timeAgo;
        if (period.getYears() == 0) {   // TODO Refactor away the pyramid of doom
            if (period.getMonths() == 0) {
                if (period.getDays() == 1){
                    timeAgo = "(1 day ago)";
                } else {
                    timeAgo = "(" + period.getDays() + " days ago)";
                }
            } else if (period.getMonths() == 1) {
                timeAgo = "(1 month ago)";
            } else {
                timeAgo = "(" + period.getMonths() + " months ago)";
            }
        } else if (period.getYears() == 1) {
            timeAgo = "(1 year ago)";
        } else {
            timeAgo = "(" + period.getYears() + " years ago)";
        }

        // Convert month field to title case rather than uppercase (e.g. March rather than MARCH)
        String month = StringUtils.capitalize(String.valueOf(date.getMonth()).toLowerCase());

        return String.format("Member since: %s %s %s %s", date.getDayOfMonth(), month, date.getYear(), timeAgo);
    }
}
