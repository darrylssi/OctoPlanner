package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Controller to handle requests related to deadlines.
 */
@Controller
public class DeadlineController extends PageController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private DeadlineService deadlineService;

    /**
     * Post request to add a deadline to a project.
     * @param principal Authenticated user
     * @param projectId ID of the project the deadline will be added to
     * @param deadlineName Name of the deadline
     * @param deadlineDate Date of the deadline
     * @param deadlineTime Time of the deadline
     * @param deadlineDescription Description of the deadline
     * @return Project details page
     */
    @PostMapping("/project/{project_id}/add-deadline")
    public String postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @RequestParam(name="deadlineName") String deadlineName,
            @RequestParam(name="deadlineDate") String deadlineDate,
            @RequestParam(name="deadlineTime") String deadlineTime,
            @RequestParam(name="deadlineDescription") String deadlineDescription
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        Deadline deadline = new Deadline();

        String deadlineDateTime = deadlineDate + " " + deadlineTime;

        // Set details of new deadline object
        deadline.setParentProject(parentProject);
        deadline.setName(deadlineName);
        deadline.setStartDate(DateUtils.toDateTime(deadlineDateTime));
        deadline.setDescription(deadlineDescription);

        deadlineService.saveDeadline(deadline);

        return "redirect:../" + parentProject.getId();

    }

    /**
     * Deletes a deadline and redirects back to the project view
     * @param principal used to check if the user is authorised to delete deadlines
     * @param deadlineId the id of the deadline to be deleted
     * @return a redirect to the project view
     */
    @DeleteMapping("/delete-deadline/{deadlineId}")
    @ResponseBody
    public ResponseEntity<String> deleteDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="deadlineId") int deadlineId
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        try {
            deadlineService.deleteDeadline(deadlineId);
            return new ResponseEntity<>("Deadline deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}