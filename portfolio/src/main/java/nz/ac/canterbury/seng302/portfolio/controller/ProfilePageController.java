package nz.ac.canterbury.seng302.portfolio.controller;

import com.google.protobuf.Timestamp;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/users/current")
    public String profileRedirect(@AuthenticationPrincipal AuthState principal) {

        String id = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("0");

        return "redirect:/users/" + id;
    }

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
            model.addAttribute("fullName", getFullName(
                    user.getFirstName(), user.getMiddleName(),  user.getLastName()));
            model.addAttribute("id", id);
            model.addAttribute("userName", user.getUsername());
            model.addAttribute("dateCreated", getDateCreated(user.getCreated()));
            String roles = "";
            for (int i = 0; i < user.getRolesCount(); i++) {
                String roleString = user.getRoles(i).toString();
                roleString = roleString.replace("_", " ");
                roles += roleString.substring(0, 1).toUpperCase() + roleString.substring(1).toLowerCase() + ", ";
            }
            if (roles.length() > 2) {
                roles = roles.substring(0, roles.length() - 2);
            }
            model.addAttribute("roles", roles);
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
        Integer unit;
        
        // Format string has 2 parts: The number, and the plural 's'
        if (period.getYears() > 0) {            // If over a year has passed
            timeAgo = "(%d year%s ago)";
            unit = period.getYears();

        } else if (period.getMonths() > 0) {    // If over a month has passed
            timeAgo = "(%d month%s ago)";
            unit = period.getMonths();
            
        } else if (period.getDays() > 0) {      // If over a day has passed
            timeAgo = "(%d day%s ago)";
            unit = period.getDays();

        } else {                                // Account was created today
            timeAgo = "(Today)";
            unit = null;
        }

        if (unit != null) {
            // Plural only applies on quantities other than 1 (0 days, 1 day, 2 days)
            timeAgo = String.format(timeAgo, unit, (unit != 1) ? "s" : "");
        }


        // Convert's the month field from all upper "MARCH" to sane "March"
        String month = StringUtils.capitalize(date.getMonth().toString().toLowerCase());

        return String.format("Member since: %s %s %s %s", date.getDayOfMonth(), month, date.getYear(), timeAgo);
    }
}
