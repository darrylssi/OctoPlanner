package nz.ac.canterbury.seng302.portfolio.controller;

import io.grpc.Status;
import io.grpc.StatusException;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(name="page", defaultValue="1") int page,
            @RequestParam(name="orderBy", defaultValue="name") String orderBy,    // ! USE A PRE-DEFINED LIST OF VALUES OR SOMETHING, BUT *DO NOT* LET USERS CHANGE THIS DIRECTLY
            @RequestParam(name="asc", defaultValue="true") boolean isAscending,
            Model model
    ) {
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
        if(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("role"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND").contains("teacher")) {
            try {
                // Add the role to the user
                var response = userAccountClientService.addRoleToUser(id, role);
                return new ResponseEntity<>(String.valueOf(response), HttpStatus.OK);
            } catch (StatusException e) {
                if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                    return new ResponseEntity<>("Invalid User Id", HttpStatus.BAD_REQUEST);
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return new ResponseEntity<>("User not authorised.", HttpStatus.UNAUTHORIZED);
    }
}
