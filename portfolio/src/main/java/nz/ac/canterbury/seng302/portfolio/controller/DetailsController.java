package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;

import java.util.List;

/**
 * Controller for the display project details page
 */
@Controller
public class DetailsController extends PageController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;

    @GetMapping("/project/{id}")
    public String details(
                            @AuthenticationPrincipal AuthState principal,
                            @PathVariable(name="id") int id,
                            @RequestParam(name="role", required=false) String debugRole,
                            Model model) throws Exception {
        /* Add project details to the model */
        // Gets the project with id 0 to plonk on the page
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);
        
        List<Sprint> sprintList = sprintService.getSprintsOfProjectById(id);
        model.addAttribute("sprints", sprintList);


        // Below code is just begging to be added as a method somewhere...
        List<String> roles = getUserRole(principal);
        roles.add(debugRole);

        /* Return the name of the Thymeleaf template */
        // detects the role of the current user and returns appropriate page
        boolean hasEditPermissions = roles.contains("teacher") || roles.contains("course_administrator");
        model.addAttribute("canEdit", hasEditPermissions);
        return "projectDetails";
        // TODO [Andrew]: I have marked "userProjectDetails.html" for deletion
    }

}
