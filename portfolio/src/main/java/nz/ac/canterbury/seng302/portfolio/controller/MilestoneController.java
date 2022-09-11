package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.MilestoneService;
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
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;


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
     * @return A ResponseEntity with the id of the milestone that was saved
     */
    @PostMapping("/project/{project_id}/add-milestone")
    public ResponseEntity<String> postAddMilestone(
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

        Milestone milestone = new Milestone();

        // Set details of new milestone object
        milestone.setParentProject(parentProject);
        milestone.setName(schedulableForm.getName());
        milestone.setStartDate(DateUtils.localDateToDate(schedulableForm.getStartDate()));
        milestone.setDescription(schedulableForm.getDescription());

        Milestone savedMilestone = milestoneService.saveMilestone(milestone);

        return ResponseEntity.ok(String.valueOf(savedMilestone.getId()));

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

}