package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.ProjectForm;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Schedulable;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Controller for the edit project details page
 */
@Controller
public class ProjectController extends PageController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private DetailsController detailsController;

    /**
     * Post request for editing a project with the given ID.
     * @param principal The authenticated or currently logged in user
     * @param id ID of the project to be edited
     * @param projectForm The form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @return A response entity that contains any errors that were found. Bad Request if there were errors, Ok if there are none
     */
    @PostMapping("project/{id}/edit-project")
    public ResponseEntity<String> postEditProject (
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @Valid @ModelAttribute ProjectForm projectForm,
            BindingResult bindingResult
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        Project newProject = projectService.getProjectById(id);

        List<Sprint> sprintList = sprintService.getSprintsInProject(id);

        List<Schedulable> schedulableList = detailsController.getAllSchedulablesInProject(id);

        ResponseEntity<String> validationResponse = validateProject(projectForm, bindingResult, sprintList, schedulableList,
                newProject.getProjectCreationDate());

        if (validationResponse.getStatusCode() == HttpStatus.OK) {

            /* Set (new) project details to the corresponding project */
            newProject.setProjectName(projectForm.getName());
            newProject.setProjectStartDate(DateUtils.localDateToDate(projectForm.getStartDate()));
            newProject.setProjectEndDate(DateUtils.localDateToDate(projectForm.getEndDate()));
            newProject.setProjectDescription(projectForm.getDescription());
            projectService.saveProject(newProject);

            /* Send an "OK" response when done */
            logger.info("Edited project {}", id);
            return ResponseEntity.ok(String.valueOf(id));
        } else {
            return validationResponse;
        }
    }

    /**
     * This validates projects when they are edited.
     * @param projectForm Form containing details of a project
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param sprintList List of sprints in the project
     * @param schedulableList List of schedulables in the project
     * @param creationDate Date when the project was created
     * @return A response entity that contains any errors that were found;
     * Bad Request if there are errors, Ok if there are none
     */
    private ResponseEntity<String> validateProject(ProjectForm projectForm, BindingResult bindingResult,
                                                   List<Sprint> sprintList, List<Schedulable> schedulableList,
                                                   Date creationDate) {

        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        ValidationError dateErrors = ValidationUtils.validateProjectDates(DateUtils.localDateToDate(projectForm.getStartDate()),
                DateUtils.localDateToDate(projectForm.getEndDate()), creationDate, sprintList, schedulableList);
        ValidationError nameErrors = ValidationUtils.validateText(projectForm.getName(), NAME_REGEX, NAME_ERROR_MESSAGE);
        ValidationError descErrors = ValidationUtils.validateText(projectForm.getDescription(), DESC_REGEX, DESC_ERROR_MESSAGE);
        String errorString = ValidationUtils.joinErrors(dateErrors, nameErrors, descErrors);
        HttpStatus status = errorString.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorString, status);
    }
}
