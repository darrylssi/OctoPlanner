package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.authentication.CookieUtil;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class for the list of users page.
 *
 * @since 21st March 2022
 */
@Controller
public class ListUsersController {

    private static final Logger logger = LoggerFactory.getLogger(ListUsersController.class);

    private static final int PAGE_SIZE = 10;
    private static final String COOKIE_NAME_PREFIX = "page_order-";
    private static final String DEFAULT_ORDER_COLUMN = "name";
    private static final boolean DEFAULT_IS_ASCENDING = true;

    @Autowired
    private UserAccountClientService userAccountClientService;

    /**
     * Displays a list of users with their name, username, nickname and roles
     */
    @GetMapping("/users")
    public String GetListOfUsers(
            @RequestParam(name="page", defaultValue="1") int page,
            @AuthenticationPrincipal AuthState principal,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Please for the love of god, someone make this easier
        logger.info(principal.getClaimsList().stream().map(claim -> claim.getType() + "->" + claim.getValue()).toList().toString());
        String userId = principal.getClaimsList().stream()
            .filter(claim -> claim.getType().equals("nameid"))
            .findFirst()
            .map(ClaimDTO::getValue)
            .orElse("-1");
        // Get sort column & direction from cookie
        Pair<String, Boolean> ordering = getPageOrdering(request, userId);
        String orderBy = ordering.getFirst();
        boolean isAscending = ordering.getSecond();

        /* Get users by page */
        PaginatedUsersResponse users;
        try {
            users = userAccountClientService.getPaginatedUsers(page - 1, PAGE_SIZE, orderBy, isAscending);
        } catch (IllegalArgumentException e) {
            // The orderBy column in the cookie is invalid, delete it
            deleteOrderingCookie(userId, response);
            // TODO Andrew: Throw a 400 error once George's branch is merged
            throw e;
        }

        model.addAttribute("page", page);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("users", users.getUsersList());
        model.addAttribute("dir", isAscending);

        /* Total number of pages */
        int totalPages = (users.getResultSetSize() + PAGE_SIZE - 1) / PAGE_SIZE;
        model.addAttribute("totalPages", totalPages);

        return "users";
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
        @RequestParam(name="orderBy", required=true) String orderBy,
        @AuthenticationPrincipal AuthState principal,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        // Please for the love of god, someone make this easier
        String userId = principal.getClaimsList().stream()
            .filter(claim -> claim.getType().equals("nameid"))
            .findFirst()
            .map(ClaimDTO::getValue)
            .orElse("-1");
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
        createOrderingCookie(orderBy, isAscending, userId, response);

        return "redirect:/users?page="+page;    // Send them back to the users page
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
        final Pair<String, Boolean> DEFAULT_VALUES = Pair.of(DEFAULT_ORDER_COLUMN, DEFAULT_IS_ASCENDING);
        
        String value = CookieUtil.getValue(request, COOKIE_NAME_PREFIX+userId);
        if (value == null) {        // Cookie doesn't exist
            return DEFAULT_VALUES;
        }
        // Cookie is of the format "column:ASC/DESC"
        String[] values = value.split(":");
        if (values.length != 2) {   // Cookie has wrong number of values
            logger.error("Cookie [{}] is of length {} (expected 2)", String.join(", ", values), values.length);
            return DEFAULT_VALUES;
        }
        String orderBy = values[0];
        Boolean isAscending = switch (values[1]) {
                case "ASC"  -> true;
                case "DESC" -> false;
                default     -> null;
        };
        if (isAscending == null) {    // Direction must be literally "ASC" or "DESC"
            logger.error("Expected \"ASC\" or \"DESC\", got \"{}\"", values[1]);
            return DEFAULT_VALUES;
        }
        return Pair.of(orderBy, isAscending);        
    }


    void createOrderingCookie(String orderBy, boolean isAscending, String userId, HttpServletResponse response) {
        String strDirection = (isAscending) ? "ASC" : "DESC";
        // Give each user on this machine a different sorting cookie (AC6)
        String cookieName = COOKIE_NAME_PREFIX + userId;
        Cookie cookie = new Cookie(cookieName, orderBy + ':' + strDirection);
        cookie.setPath("/users");
        cookie.setMaxAge(365 * 24 * 60 * 60);   // Expire in a year
        response.addCookie(cookie);
    }

    void deleteOrderingCookie(String userId, HttpServletResponse response) {
        CookieUtil.clear(response, COOKIE_NAME_PREFIX+userId);
    }
}
