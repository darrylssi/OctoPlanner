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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.TimeZone;

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
     * @param principal Authenticated user
     * @param projectId The project this event will be bound to
     * @param schedulableForm Form that stores information about the deadline
     * @return  A response of either 200 (success), 403 (forbidden),
     *          or 400 (Given event failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/add-deadline")
    public ResponseEntity<String> postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @Valid SchedulableForm schedulableForm
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        Deadline deadline = new Deadline();

        String deadlineDateTime = schedulableForm.getStartDate() + " " + schedulableForm.getStartTime();

        // Set details of new deadline object
        deadline.setParentProject(parentProject);
        deadline.setName(schedulableForm.getName());
        deadline.setStartDate(DateUtils.toDateTime(deadlineDateTime));
        deadline.setDescription(schedulableForm.getDescription());


        Deadline savedDeadline = deadlineService.saveDeadline(deadline);

        return ResponseEntity.ok(String.valueOf(savedDeadline.getId()));

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
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        try {
            deadlineService.deleteDeadline(deadlineId);
            return new ResponseEntity<>("Deadline deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Post request to edit a deadline.
     * @param principal Authenticated user
     * @param projectId ID of the project the deadline belongs to
     * @param deadlineId ID of the deadline to be edited
     * @param schedulableForm Form that stores information about the deadline
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param userTimeZone Current timezone of the user
     * @return Project details page
     */
    @PostMapping("/project/{project_id}/edit-deadline/{deadline_id}")
    public ResponseEntity<String> postEditDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @PathVariable("deadline_id") int deadlineId,
            @Valid @ModelAttribute SchedulableForm schedulableForm,
            BindingResult bindingResult,
            TimeZone userTimeZone
    ){
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        Deadline deadline = deadlineService.getDeadlineById(deadlineId);
        deadline.setName(schedulableForm.getName());
        deadline.setDescription(schedulableForm.getDescription());
        deadline.setStartDate(DateUtils.localDateAndTimeToDate(schedulableForm.getStartDate(), schedulableForm.getStartTime(), userTimeZone));
        deadline.setParentProject(projectService.getProjectById(projectId));

        Deadline savedDeadline = deadlineService.saveDeadline(deadline);

        return ResponseEntity.ok(String.valueOf(savedDeadline.getId()));
    }

}