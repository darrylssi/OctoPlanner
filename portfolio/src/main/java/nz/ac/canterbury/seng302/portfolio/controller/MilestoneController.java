package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.MilestoneService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
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
 * Controller to handle requests related to milestones.
 */
@Controller
public class MilestoneController extends PageController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MilestoneService milestoneService;

    /**
     * Post request to add milestones to a project.
     * @param principal Authenticated user
     * @param projectId ID of the project the milestone will be added to
     * @param schedulableForm Form that stores information about the milestone
     * @return A response of either 200 (success), 403 (forbidden),
     *         or 400 (Given event failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/add-milestone")
    public ResponseEntity<String> postAddMilestone(
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

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        // validate milestone
        ResponseEntity<String> validationResponse = validateMilestone(schedulableForm, bindingResult, parentProject);
        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // passed validation, create a new milestone
            Milestone milestone = new Milestone();

            // Set details of new milestone object
            milestone.setParentProject(parentProject);
            milestone.setName(schedulableForm.getName());
            milestone.setStartDate(DateUtils.localDateToDate(schedulableForm.getStartDate()));
            milestone.setDescription(schedulableForm.getDescription());

            Milestone savedMilestone = milestoneService.saveMilestone(milestone);

            return ResponseEntity.ok(String.valueOf(savedMilestone.getId()));
        } else {
            return validationResponse;
        }

    }

    /**
     * Deletes a milestone and redirects back to the project view
     * @param principal used to check if the user is authorised to delete events
     * @param milestoneId the id of the milestone to be deleted
     * @return response if milestone is deleted
     */
    @DeleteMapping("/delete-milestone/{milestoneId}")
    @ResponseBody
    public ResponseEntity<String> deleteMilestone(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="milestoneId") int milestoneId
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        try {
            milestoneService.deleteMilestone(milestoneId);
            return new ResponseEntity<>("Milestone deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * A method to validate milestones when they are added or edited
     * @param schedulableForm the form containing the milestone information
     * @param bindingResult any errors that came up during validation
     * @param parentProject the parent project of the milestone
     * @return a response entity that contains any errors that were found. Bad Request if there were errors, Ok if there are none
     */
    private ResponseEntity<String> validateMilestone(SchedulableForm schedulableForm, BindingResult bindingResult, Project parentProject) {
        // list of errors that can be removed as they are not applicable to milestones
        List<String> notApplicableErrors = new ArrayList<>(List.of("Start time cannot be blank", "End date cannot be blank", "End time cannot be blank"));
        // Pattern: Don't do the deeper validation if the data has no integrity (i.e. has nulls)
        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                if (Objects.equals(err.getDefaultMessage(), "Start date cannot be blank")) {
                    errors.add("Date cannot be blank");
                } else if (!notApplicableErrors.contains(err.getDefaultMessage())) {
                    errors.add(err.getDefaultMessage());
                }
            }
            if(errors.toString().length() != 0) {
                return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
            }
        }
        // Check that the date is correct
        ValidationError dateErrors = ValidationUtils.validateMilestoneDate(DateUtils.localDateToDate(schedulableForm.getStartDate()), parentProject);
        ValidationError nameErrors = ValidationUtils.validateText(schedulableForm.getName(), GlobalVars.NAME_REGEX, GlobalVars.NAME_ERROR_MESSAGE);
        ValidationError descErrors = ValidationUtils.validateText(schedulableForm.getDescription(), GlobalVars.DESC_REGEX, GlobalVars.DESC_ERROR_MESSAGE);
        System.out.println(descErrors.getErrorMessages());
        String errorString = ValidationUtils.joinErrors(dateErrors, nameErrors, descErrors);
        HttpStatus status = errorString.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorString, status);
    }

    /**
     * Post request to edit a milestone.
     * @param principal Authenticated user
     * @param projectId ID of the project the milestone belongs to
     * @param milestoneId ID of the milestone to be edited
     * @param schedulableForm Form that stores information about the milestone
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param userTimeZone Current timezone of the user
     * @return A response of either 200 (success), 403 (forbidden),
     *         or 400 (Given milestone failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/edit-milestone/{milestone_id}")
    public ResponseEntity<String> postEditMilestone(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @PathVariable("milestone_id") int milestoneId,
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

        Project parentProject = projectService.getProjectById(projectId);
        Milestone milestone = milestoneService.getMilestoneById(milestoneId);
        // validate milestone
        ResponseEntity<String> validationResponse = validateMilestone(schedulableForm, bindingResult, parentProject);
        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // passed validation, save edited milestone
            milestone.setName(schedulableForm.getName());
            milestone.setDescription(schedulableForm.getDescription());
            milestone.setStartDate(DateUtils.localDateToDate(schedulableForm.getStartDate()));
            milestone.setParentProject(projectService.getProjectById(projectId));

            Milestone savedMilestone = milestoneService.saveMilestone(milestone);

            return ResponseEntity.ok(String.valueOf(savedMilestone.getId()));
        } else {
            return validationResponse;
        }
    }

}