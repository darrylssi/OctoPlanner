package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

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
     * Endpoint for adding events to the database, or conveying errors
     * to the user
     * @param projectId The project this event will be bound to
     * @param schedulableForm The form submitted by our lovely customers
     * @return  A response of either 200 (success), 403 (forbidden),
     *          or 400 (Given event failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/add-deadline")
    public String postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @Valid SchedulableForm schedulableForm
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        Deadline deadline = new Deadline();

        String deadlineDateTime = schedulableForm.getStartDate() + " " + schedulableForm.getStartTime();

        // Set details of new deadline object
        deadline.setParentProject(parentProject);
        deadline.setName(schedulableForm.getName());
        deadline.setStartDate(DateUtils.toDateTime(deadlineDateTime));
        deadline.setDescription(schedulableForm.getDescription());

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