package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import nz.ac.canterbury.seng302.portfolio.exception.ForbiddenException;
import nz.ac.canterbury.seng302.portfolio.model.ErrorType;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Common methods for the portfolio page controllers
 */
@Controller
public abstract class PageController {
    /**
     * Adds error information to the model
     *
     * @param err the type of error used
     * @param source_page The URL that caused this error
     */
    public static void configureError(Model model, ErrorType err, String source_page) {
        /* Set current timestamp and page */
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
        Date date = new Date();
        model.addAttribute("timestamp", formatter.format(date));
        model.addAttribute("path", source_page);

        /* Handle various types of error */
        switch(err) {
            case NOT_FOUND:
                model.addAttribute("error", "Page not found");
                model.addAttribute("status", "404");
                model.addAttribute("message", "The page you attempted to access does not exist");
                break;
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

    /**
     * Checks if the current user has permission to access this endpoint, otherwise filters them out with a 403 error.
     * <p>Recommend putting this at the top of your endpoint's code block</p>
     * 
     * @param minimumRole The lowest role allowed to access this endpoint [STUDENT < TEACHER < ADMIN]
     * @param principal The principal object from your controller args
     * @throws ForbiddenException If the user doesn't have permission, throws a 403 forbidden error
     */
    public static void requiresRoleOfAtLeast(UserRole minimumRole, AuthState principal) throws ForbiddenException {
        PrincipalData user = PrincipalData.from(principal);

        boolean hasPermissions = user.hasRoleOfAtLeast(minimumRole);
        if (!hasPermissions) {
            throw new ForbiddenException();
        }
    }
}
