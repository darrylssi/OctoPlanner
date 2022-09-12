package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.DeadlineForm;
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
import java.util.StringJoiner;
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
     * Post request to add a deadline to a project.
     * @param principal Authenticated user
     * @param projectId ID of the project the deadline will be added to
     * @param deadlineForm Form that stores information about the deadline
     * @param bindingResult Any errors that came up during validation
     * @return A response of either 200 (success), 403 (forbidden),
     *         or 400 (Given deadline failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/add-deadline")
    public ResponseEntity<String> postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @Valid DeadlineForm deadlineForm,
            BindingResult bindingResult,
            TimeZone userTimezone
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        // Pattern: Don't do the deeper validation if the data has no integrity (i.e. has nulls)
        Project parentProject = projectService.getProjectById(projectId);
        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        // Validation round 2: Do our custom errors pass?
        var dateErrors = ValidationUtils.validateDeadlineDate(deadlineForm.datetimeToDate(userTimezone), parentProject);
        var nameError = ValidationUtils.validateName(deadlineForm.getName());
        ResponseEntity<String> errors = validateDateAndName(dateErrors, nameError);
        if (errors != null) return errors;

        // Data is valid, add it to database
        Deadline deadline = new Deadline(deadlineForm.getName(), deadlineForm.getDescription(), deadlineForm.datetimeToDate(userTimezone));
        deadline.setParentProject(parentProject);
        deadlineService.saveDeadline(deadline);
        return ResponseEntity.ok("");
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
     * Handle edit requests for deadlines. Validate the form and determine the response
     * @param principal Authenticated user
     * @param projectId ID of the project the deadline belongs to
     * @param deadlineId ID of the deadline to be edited
     * @param deadlineForm Form that stores information about the deadline
     * @param userTimeZone Current timezone of the user
     * @return A response of either 200 (success), 403 (forbidden),
     *         or 400 (Given deadline failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/edit-deadline/{deadline_id}")
    public ResponseEntity<String> postEditDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @PathVariable("deadline_id") int deadlineId,
            @Valid @ModelAttribute DeadlineForm deadlineForm,
            BindingResult bindingResult,
            TimeZone userTimeZone
    ){
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        // Validation round 1: Do the Javax Validation annotations pass?
        Deadline deadline = deadlineService.getDeadlineById(deadlineId);
        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        // Validation round 2: Do our custom errors pass?
        var dateErrors = ValidationUtils.validateDeadlineDate(deadlineForm.datetimeToDate(userTimeZone), deadline.getParentProject());
        var nameError = ValidationUtils.validateName(deadlineForm.getName());
        ResponseEntity<String> errors = validateDateAndName(dateErrors, nameError);
        if (errors != null) return errors;

        deadline.setName(deadlineForm.getName());
        deadline.setDescription(deadlineForm.getDescription());
        deadline.setStartDate(deadlineForm.datetimeToDate(userTimeZone));
        deadline.setParentProject(projectService.getProjectById(projectId));

        deadlineService.saveDeadline(deadline);
        return ResponseEntity.ok("");
    }

    /**
     * Validation for deadline's date and name.
     * @param dateErrors Checks custom date error
     * @param nameError Checks custom name error
     * @return A response of either null, 200 (success),
     *          or 400 (Given deadline failed validation, replies with what errors occurred)
     */
    private ResponseEntity<String> validateDateAndName(ValidationError dateErrors, ValidationError nameError) {
        if (dateErrors.isError() || nameError.isError()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: dateErrors.getErrorMessages()) {
                errors.add(err);
            }
            for (var err: nameError.getErrorMessages()) {
                errors.add(err);
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        return null;
    }}