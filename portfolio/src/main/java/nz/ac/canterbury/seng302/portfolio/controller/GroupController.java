package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller to handle requests on the groups page.
 */
@Controller
public class GroupController extends PageController{

    public static final String GROUPS_TEMPLATE_NAME = "groups";

    /**
     * Get request to view the groups page.
     * @param principal Authenticated user
     * @param model Parameters sent to thymeleaf template
     * @return Groups page
     */
    @GetMapping("/groups")
    public String groups(
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        model.addAttribute("tab", 3);
        return GROUPS_TEMPLATE_NAME;    // Return the name of the Thymeleaf template
    }
}
