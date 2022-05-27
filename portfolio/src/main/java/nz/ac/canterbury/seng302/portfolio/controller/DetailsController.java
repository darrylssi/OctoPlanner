package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;

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
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import java.util.Comparator;
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
    @Autowired
    private SprintLabelService labelUtils;
    @Autowired
    private UserAccountClientService userAccountClientService;

    @GetMapping("/project/{id}")
    public String details(
                @AuthenticationPrincipal AuthState principal,
                @PathVariable(name="id") int id,
                Model model
    ) throws Exception {
        PrincipalData principalData = PrincipalData.from(principal);

        /* Add project details to the model */
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);

        labelUtils.refreshProjectSprintLabels(id);

        List<Sprint> sprintList = sprintService.getSprintsOfProjectById(id);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);

        // If the user is at least a teacher, the template will render delete/edit buttons
        boolean hasEditPermissions = principalData.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);

        /* Return the name of the Thymeleaf template */
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
        PrincipalData principalData = PrincipalData.from(principal);
        // Check if the user is authorised to delete sprints
        if (!principalData.hasRoleOfAtLeast(UserRole.TEACHER)) {
            return new ResponseEntity<>("User not authorised.", HttpStatus.UNAUTHORIZED);
        }
        try {
            sprintService.deleteSprint(sprintId);
            return new ResponseEntity<>("Sprint deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
