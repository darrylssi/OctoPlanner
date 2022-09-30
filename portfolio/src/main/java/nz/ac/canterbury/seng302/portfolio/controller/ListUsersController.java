package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.Status;
import io.grpc.StatusException;
import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import java.util.List;
import java.util.stream.Stream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for the list of users page.
 *
 * @since 21st March 2022
 */
@Controller
public class ListUsersController extends PageController {

    @Value("${base-url}")
    private String baseURL;

    private static final Logger logger = LoggerFactory.getLogger(ListUsersController.class);

    private static final int PAGE_SIZE = 10;
    private static final String COOKIE_NAME_PREFIX = "page_order-";
    private static final String COOKIE_VALUE_SEPARATOR = ":";
    private static final String DEFAULT_ORDER_COLUMN = "name";
    private static final boolean DEFAULT_IS_ASCENDING = true;

    private static final String USERS_PATH = "users";

    private static final Pair<String, Boolean> DEFAULT_COOKIE_VALUE_PAIR = Pair.of(DEFAULT_ORDER_COLUMN, DEFAULT_IS_ASCENDING);

    @Autowired
    private UserAccountClientService userAccountClientService;

    /**
     * Displays a list of users with their name, username, nickname and roles
     */
    @GetMapping("/users")
    public String getListOfUsers(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(name="page", defaultValue="1") int page,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        String sThisUserID = String.valueOf(thisUser.getID());
        // Get sort column & direction from cookie
        Pair<String, Boolean> ordering = getPageOrdering(request, sThisUserID);
        String orderBy = ordering.getFirst();
        boolean isAscending = ordering.getSecond();

        /* Get users by page */
        PaginatedUsersResponse users;
        try {
            users = userAccountClientService.getPaginatedUsers(page - 1, PAGE_SIZE, orderBy, isAscending);
        } catch (IllegalArgumentException e) {
            // The orderBy column in the cookie is invalid, delete it
            clearPageOrdering(sThisUserID, response);
            throw e;
        }

        // Only allows the user to touch roles they have access to (Teachers can't unassign admins)
        List<UserRole> acceptableRoles = Stream.of(UserRole.values())
                                    .filter(thisUser::hasRoleOfAtLeast)
                                    .toList();

        model.addAttribute("acceptableRoles", acceptableRoles);

        // Only teachers or above can edit roles
        model.addAttribute("canEdit", thisUser.hasRoleOfAtLeast(UserRole.TEACHER));

        model.addAttribute("page", page);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute(USERS_PATH, users.getUsersList());
        model.addAttribute("dir", isAscending);

        // If the user is at least a teacher, the template will render delete/edit buttons
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);

        /* Total number of pages */
        int totalPages = (users.getPaginationResponseOptions().getResultSetSize() + PAGE_SIZE - 1) / PAGE_SIZE;
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("tab", 1);

        return USERS_PATH;
    }


    /**
     * Endpoint for modifying the user list sort direction.
     *
     * @param page Used to redirect the user back to the original page
     * @param orderBy The column to order by. If they were already ordered by this, flip the direction
     * @return Redirects the user back to the page number they were just on
     */
    @PostMapping("/users")
    public String changeUserListOrdering(
        @RequestParam(name="page", defaultValue="1") int page,
        @RequestParam(name="orderBy") String orderBy,
        @AuthenticationPrincipal AuthState principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        String userId = String.valueOf(thisUser.getID());
        // Get the user's existing ordering
        Pair<String, Boolean> ordering = getPageOrdering(request, userId);
        String savedOrderBy = ordering.getFirst();
        boolean savedIsAscending = ordering.getSecond();

        boolean isAscending;
        if (orderBy.equals(savedOrderBy)) {     // If they were already ordered by that column, reverse the direction
            isAscending = !savedIsAscending;
        } else {                                // Default ordering if they click on a new column
            isAscending = DEFAULT_IS_ASCENDING;
        }
        // Set the cookie
        createPageOrdering(orderBy, isAscending, userId, response);

        return "redirect:users?page="+page;    // Send them back to the users page
    }

    /**
     * Parses the user page ordering from the user's cookie, and returns the ordering column & direction (true if ascending).
     * <p>
     *   If the cookie is poorly formatted/non-existent, it returns the default ordering and direction.
     * </p>
     * NOTE: This won't check if the orderBy column will be accepted by the gRPC, merely that the format's correct
     *
     * @param request Your controller's request object
     * @param userId The ID of the logged in user. This is to satisfy AC6
     * @return A pair of type (orderBy, isAscending) for the page.
     *         If the cookie is abscent or invalid, it'll return the default values <code>("name", true(<i>ascending</i>))</code>
     */
    private static Pair<String, Boolean> getPageOrdering(HttpServletRequest request, String userId) {
        // Cookie should be in the format "<orderBy>:<ASC || DESC>" e.g. "username:DESC"
        // * 1. Does the cookie exist?
        String value = CookieUtil.getValue(request, COOKIE_NAME_PREFIX+userId);
        if (value == null) {
            return DEFAULT_COOKIE_VALUE_PAIR;
        }
        // * 2. Is the cookie made up of two values?
        String[] values = value.split(COOKIE_VALUE_SEPARATOR);
        if (values.length != 2) {
            logger.error("Cookie \"{}\" is of length {} (expected 2)", value, values.length);
            return DEFAULT_COOKIE_VALUE_PAIR;
        }
        // * 3. Is the cookie's 2nd value (direction) valid?
        String orderBy = values[0];
        Boolean isAscending = switch (values[1]) {
                case "ASC"  -> true;
                case "DESC" -> false;
                default     -> null;
        };
        if (isAscending == null) {
            logger.error("Expected \"ASC\" or \"DESC\", got \"{}\"", values[1]);
            return DEFAULT_COOKIE_VALUE_PAIR;
        }
        return Pair.of(orderBy, isAscending);
    }

    /**
     * Creates and sets the ordering cookie for the current user.
     *
     * @param orderBy The column to be ordered by
     * @param isAscending If the column's in ascending order
     * @param userId A unique ID for each user, so different users on the same
     *               machine keep the ordering when logging back in
     * @param response Your endpoint's response object, to bind the cookie to.
     */
    void createPageOrdering(String orderBy, boolean isAscending, String userId, HttpServletResponse response) {
        // Cookie should be in the format "<orderBy>:<ASC || DESC>" e.g. "username DESC"
        String strDirection = (isAscending) ? "ASC" : "DESC";
        // Give each user on this machine a different sorting cookie (AC6)
        String cookieName = COOKIE_NAME_PREFIX + userId;
        Cookie cookie = new Cookie(cookieName, orderBy + COOKIE_VALUE_SEPARATOR + strDirection);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(365 * 24 * 60 * 60);   // Expire in a year
        cookie.setPath(baseURL + USERS_PATH);
        response.addCookie(cookie);
    }

    /**
     * <p>Invalidates the user's ordering cookie.</p>
     * This can be useful if the cookie's data is "bad", and needs to be
     * gotten rid of (e.g. sorting by an invalid column)
     *
     * @param userId The ID associated with the cookie
     * @param response Your endpoint's response object, to remove the cookie from
     */
    void clearPageOrdering(String userId, HttpServletResponse response) {
        CookieUtil.clear(response, COOKIE_NAME_PREFIX+userId);
    }


    /**
     * Adds a role to a user
     * @param principal used to check if the user sending the request is authorised to add roles
     * @param id the id of the user being edited
     * @param role the role to be added
     * @return an HttpResponse describing the result
     */
    @PatchMapping("/users/{id}/add-role/{role}")
    @ResponseBody
    public ResponseEntity<String> addRoleToUser(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @PathVariable("role") UserRole role
    ) {
        // Check if the user is authorised to add roles
        PrincipalData thisUser = PrincipalData.from(principal);
        if (thisUser.hasRoleOfAtLeast(UserRole.TEACHER)) {
            try {
                // Add the role to the user
                boolean roleAdded = userAccountClientService.addRoleToUser(id, role);
                if(roleAdded) {
                    return new ResponseEntity<>("Role " + role.name() + " added", HttpStatus.OK);
                } else {
                    return  new ResponseEntity<>("Role not added", HttpStatus.BAD_REQUEST);
                }
            } catch (StatusException e) {
                if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                    return new ResponseEntity<>("Invalid User Id", HttpStatus.NOT_FOUND);
                } else {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>("User not authorised to edit roles", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Removes a role from a user
     * @param principal used to check if the user sending the request is authorised to remove roles
     * @param id the id of the user being edited
     * @param role the role to be removed
     * @return an HttpResponse describing the result
     */
    @PatchMapping("/users/{id}/remove-role/{role}")
    @ResponseBody
    public ResponseEntity<String> removeRoleFromUser(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @PathVariable("role") UserRole role
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        if (thisUser.hasRoleOfAtLeast(UserRole.TEACHER)) {
            try {
                // Remove role from user
                boolean roleRemoved = userAccountClientService.removeRoleFromUser(id, role);
                if(roleRemoved) {
                    return new ResponseEntity<>("Role " + role.name() + " removed", HttpStatus.OK);
                } else {
                    return  new ResponseEntity<>("Role not removed", HttpStatus.BAD_REQUEST);
                }
            } catch (StatusException e) {
                if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                    return new ResponseEntity<>("Invalid User Id", HttpStatus.NOT_FOUND);
                } else {
                    return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }
        }
        return new ResponseEntity<>("User not authorised to edit roles", HttpStatus.UNAUTHORIZED);
    }

}
