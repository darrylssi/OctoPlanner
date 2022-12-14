package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

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

    private static final String ERROR = "error";
    private static final String STATUS = "status";
    private static final String FEEDBACK = "feedback";

    /**
     * Adds error information to the model
     *
     * @param err the type of error used
     * @param sourcePage The URL that caused this error
     */
    public static void configureError(Model model, ErrorType err, String sourcePage) {
        /* Set current timestamp and page */
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd hh:mm:ss z yyyy");
        Date date = new Date();
        model.addAttribute("timestamp", formatter.format(date));
        model.addAttribute("path", sourcePage);

        /* Handle various types of error */
        switch (err) {
            case NOT_FOUND -> {
                model.addAttribute(ERROR, "Page not found");
                model.addAttribute(STATUS, "404");
                model.addAttribute(FEEDBACK, "We couldn't find the page you're looking for");
            }
            case ACCESS_DENIED -> {
                model.addAttribute(ERROR, "Access Denied");
                model.addAttribute(STATUS, "403");
                model.addAttribute(FEEDBACK, "You do not have permission to view this page");
            }
            case UNKNOWN_CLIENT -> {
                model.addAttribute(ERROR, "Unknown Client Error");
                model.addAttribute(STATUS, "400");
            }
        }
    }

    /**
     * Checks if the current user has permission to access this endpoint, otherwise filters them out with a 403 error.
     * <p>Recommend putting this at the top of your endpoint's code block</p>
     * 
     * @param minimumRole The lowest role allowed to access this endpoint [STUDENT < TEACHER < ADMIN]
     * @param principal The principal object from your controller args
     * @throws ResponseStatusException If the user doesn't have permission, throws a 403 forbidden error
     */
    public void requiresRoleOfAtLeast(UserRole minimumRole, AuthState principal) throws ResponseStatusException {
        PrincipalData user = PrincipalData.from(principal);
        boolean hasPermissions = user.hasRoleOfAtLeast(minimumRole);
        if (!hasPermissions) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this endpoint");
        }
    }
}
