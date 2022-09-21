package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
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
import java.util.*;

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
     * @param projectId ID of the project the deadline will be added to
     * @param schedulableForm Form that stores information about the deadline
     * @param bindingResult Any errors that came up during validation
     * @param userTimezone The user's time zone
     * @return A response of either 200 (success), 403 (forbidden),
     * or 400 (Given deadline failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/add-deadline")
    public ResponseEntity<String> postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @Valid SchedulableForm schedulableForm,
            BindingResult bindingResult,
            TimeZone userTimezone
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        // Getting parent project object by path project id
        Project parentProject = projectService.getProjectById(projectId);

        // validate deadline
        ResponseEntity<String> validationResponse = validateDeadline(schedulableForm, bindingResult, parentProject, userTimezone);
        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // Passed validation, create a new deadline and set details of new deadline object
            Deadline deadline = new Deadline(schedulableForm.getName(), schedulableForm.getDescription(), schedulableForm.startDatetimeToDate(userTimezone));
            deadline.setParentProject(parentProject);

            Deadline savedDeadline = deadlineService.saveDeadline(deadline);

            return ResponseEntity.ok(String.valueOf(savedDeadline.getId()));
        } else {
            return validationResponse;
        }
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
            @PathVariable(name = "deadlineId") int deadlineId
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
     * Handle edit requests for deadlines. Validate the form and determine the response
     * @param principal Authenticated user
     * @param projectId ID of the project the deadline belongs to
     * @param deadlineId ID of the deadline to be edited
     * @param editSchedulableForm Form that stores information about the deadline
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param userTimeZone Current timezone of the user
     * @return A response of either 200 (success), 403 (forbidden),
     *         or 400 (Given deadline failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/edit-deadline/{deadline_id}")
    @ResponseBody
    public ResponseEntity<String> postEditDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @PathVariable("deadline_id") int deadlineId,
            @Valid @ModelAttribute SchedulableForm editSchedulableForm,
            BindingResult bindingResult,
            TimeZone userTimeZone
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }
        // validate deadline
        Project parentProject = projectService.getProjectById(projectId);
        ResponseEntity<String> validationResponse = validateDeadline(editSchedulableForm, bindingResult, parentProject, userTimeZone);
        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // passed validation, save edited deadline
            Deadline deadline = deadlineService.getDeadlineById(deadlineId);
            deadline.setName(editSchedulableForm.getName());
            deadline.setDescription(editSchedulableForm.getDescription());
            deadline.setStartDate(editSchedulableForm.startDatetimeToDate(userTimeZone));
            deadline.setParentProject(projectService.getProjectById(projectId));

            Deadline savedDeadline = deadlineService.saveDeadline(deadline);

            return ResponseEntity.ok(String.valueOf(savedDeadline.getId()));
        } else {
            return validationResponse;
        }
    }

    /**
     * A method to validate deadlines when they are added or edited
     * @param schedulableForm the form containing the deadline information
     * @param bindingResult any errors that came up during validation
     * @param parentProject the parent project of the deadline
     * @return a response entity that contains any errors that were found. Bad Request if there were errors, Ok if there are none
     */
    private ResponseEntity<String> validateDeadline(SchedulableForm schedulableForm, BindingResult bindingResult, Project parentProject, TimeZone userTimeZone) {
        // list of errors that can be removed as they are not applicable to milestones
        List<String> notApplicableErrors = new ArrayList<>(List.of("End date cannot be blank", "End time cannot be blank"));
        // Pattern: Don't do the deeper validation if the data has no integrity (i.e. has nulls)
        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                if (Objects.equals(err.getDefaultMessage(), "Start date cannot be blank")) {
                    errors.add("Date cannot be blank");
                } else if (Objects.equals(err.getDefaultMessage(), "Start time cannot be blank")) {
                    errors.add("Time cannot be blank");
                } else if (!notApplicableErrors.contains(err.getDefaultMessage())) {
                    errors.add(err.getDefaultMessage());
                }
            }
            if(errors.toString().length() != 0) {
                return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
            }
        }
        // Check that the date is correct
        ValidationError dateErrors = ValidationUtils.validateDeadlineDate(schedulableForm.startDatetimeToDate(userTimeZone), parentProject);
        ValidationError nameErrors = ValidationUtils.validateName(schedulableForm.getName());
        ValidationError descriptionErrors = ValidationUtils.validateDescription(schedulableForm.getDescription());
        String errorString = ValidationUtils.joinErrors(dateErrors, nameErrors, descriptionErrors);
        HttpStatus status = errorString.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorString, status);
    }

}