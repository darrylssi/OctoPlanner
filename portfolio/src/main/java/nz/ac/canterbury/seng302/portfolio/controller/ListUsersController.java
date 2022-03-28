package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.PaginatedUsersResponse;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserAccountClientService userAccountClientService;

    /**
     * Displays a list of users with their name, username, nickname and roles
     */
    @GetMapping("/users")
    public String GetListOfUsers(
            @RequestParam(name="page", defaultValue="1") int page,
            @RequestParam(name="orderBy", defaultValue="firstName") String orderBy,    // ! USE A PRE-DEFINED LIST OF VALUES OR SOMETHING, BUT *DO NOT* LET USERS CHANGE THIS DIRECTLY
            @RequestParam(name="dir", defaultValue="asc") String dir,
            Model model
    ) {
        int limit = 10;

        /* Get users by page */
        PaginatedUsersResponse users = userAccountClientService.getPaginatedUsers(page-1, limit, orderBy);

        model.addAttribute("page", page);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("users", users.getUsersList());
        model.addAttribute("dir", dir.equals("asc") ? "desc" : "asc");

        /* Total number of pages */
        int totalPages = (users.getResultSetSize() + limit - 1) / limit;
        model.addAttribute("totalPages", totalPages);

        System.out.println(totalPages);
        System.out.println(users.getResultSetSize());

        return "users";
    }
}
