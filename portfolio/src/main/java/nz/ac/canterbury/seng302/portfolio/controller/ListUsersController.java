package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class for the list of users page.
 *
 * @since 21st March 2022
 */
@Controller
public class ListUsersController {

    private final int LIMIT = 10;

    @Autowired
    private UserAccountClientService userAccountClientService;

    /**
     * Displays a list of users with their name, username, nickname and roles
     */
    @GetMapping("/users")
    public String GetListOfUsers(
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(name="page", defaultValue="1") int page,
            @RequestParam(name="orderBy", defaultValue="name") String orderBy,    // ! USE A PRE-DEFINED LIST OF VALUES OR SOMETHING, BUT *DO NOT* LET USERS CHANGE THIS DIRECTLY
            @RequestParam(name="asc", defaultValue="true") boolean isAscending,
            Model model
    ) {
        // Getting the current user's username for the header
        String getUsername = getUsernameById(principal);
        model.addAttribute("userName", getUsername);

        /* Get users by page */
        PaginatedUsersResponse users = userAccountClientService.getPaginatedUsers(page - 1, LIMIT, orderBy, isAscending);

        model.addAttribute("page", page);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("users", users.getUsersList());
        model.addAttribute("dir", isAscending);
        model.addAttribute("reverseDir", !isAscending);


        /* Total number of pages */
        int totalPages = (users.getResultSetSize() + LIMIT - 1) / LIMIT;
        model.addAttribute("totalPages", totalPages);

        return "users";
    }

    public String getUsernameById(@AuthenticationPrincipal AuthState principal) {
        // Setting the current user's username at the header
        String currentUserId = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("nameid"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");

        String username = userAccountClientService.getUserAccountById(Integer.parseInt(currentUserId)).getUsername();
        return username;
    }
}
