package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;

import java.util.Comparator;
import java.util.List;

/**
 * Controller for the display project details page
 */
@Controller
public class DetailsController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private SprintLabelService labelUtils;

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

        labelUtils.refreshProjectSprintLabels(id);

        List<Sprint> sprintList = sprintService.getSprintsOfProjectById(id);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);


        // TODO: Link this with George's role helper class once that's merged
        String role;
        if (debugRole != null) {
            role = debugRole;
        } else {
            role = principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("role"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND");
        }
        /* Return the name of the Thymeleaf template */
        // detects the role of the current user and returns appropriate page
        boolean hasEditPermissions = role.contains("teacher");
        model.addAttribute("canEdit", hasEditPermissions);
        return "projectDetails";
    }

    /**
     * Deletes a sprint and redirects back to the project view
     * @param principal used to check if the user is authorised to delete sprints
     * @param sprintId the id of the sprint to be deleted
     * @return a redirect to the project view
     */
    @DeleteMapping("/delete-sprint/{sprintId}")
    @ResponseBody
    public ResponseEntity<String> deleteSprint(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="sprintId") int sprintId
            ) {

        // Check if the user is authorised to delete sprints
        if(principal.getClaimsList().stream()
                .filter(claim -> claim.getType().equals("role"))
                .findFirst()
                .map(ClaimDTO::getValue)
                .orElse("NOT FOUND").contains("teacher")) {
            try {
                sprintService.deleteSprint(sprintId);
                return new ResponseEntity<>("Sprint deleted.", HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("User not authorised.", HttpStatus.UNAUTHORIZED);
    }

}
