package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.portfolio.model.ErrorType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Common methods for the portfolio page controllers
 */
@Controller
public abstract class PageController {
    /**
     * Fetches the current role of the user
     * @return user's role - student, teacher, admin, or "NOT FOUND"
     */
    public List<String> getUserRole(AuthState principal) {
        String role = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("role"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");
        return new ArrayList<String>(Arrays.asList(role.split(",")));
    }

    /**
     * Adds error information to the model
     *
     * @param err the type of error used
     */
    public void configureError(Model model, ErrorType err, String source_page) {
        /* Set current timestamp and page */
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
        Date date = new Date();
        model.addAttribute("timestamp", formatter.format(date));
        model.addAttribute("path", source_page);

        /* Handle various types of error */
        switch(err) {
            case ACCESS_DENIED:
                model.addAttribute("error", "Access Denied");
                model.addAttribute("status", "403");
                model.addAttribute("message", "You do not have permission to view this page");
                break;
            case UNKNOWN_CLIENT:
                model.addAttribute("error", "Unknown Client Error");
                model.addAttribute("status", "400");
                break;
        }
    } 
}
