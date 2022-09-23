package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.*;

/**
 * Controller for the edit project details page
 */
@Controller
public class EditProjectController extends PageController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    /**
     * Post request for editing a project with the given ID.
     * @param principal The authenticated or currently logged in user
     * @param id ID of the project to be edited
     * @param projectName (New) Name of the project
     * @param projectStartDate (New) Start date of the project
     * @param projectEndDate (New) End date of the project
     * @param projectDescription (New) description of the project
     * @return A response entity that contains any errors that were found. Bad Request if there were errors, Ok if there are none
     */
    @PostMapping("project/{id}/edit-project")
    public ResponseEntity<String> postEditProject (
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @RequestParam(value="projectName") String projectName,
            @RequestParam(value="projectStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectStartDate,
            @RequestParam(value="projectEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectEndDate,
            @RequestParam(value="projectDescription") String projectDescription
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        Project newProject = projectService.getProjectById(id);

        List<Sprint> sprintList = sprintService.getSprintsInProject(id);

        ResponseEntity<String> validationResponse = validateProject(projectStartDate, projectEndDate,
                newProject.getProjectCreationDate(), sprintList, projectName);

        if (validationResponse.getStatusCode() == HttpStatus.OK) {

            /* Set (new) project details to the corresponding project */
            newProject.setProjectName(projectName);
            newProject.setProjectStartDate(projectStartDate);
            newProject.setProjectEndDate(projectEndDate);
            newProject.setProjectDescription(projectDescription);
            projectService.saveProject(newProject);

            /* Redirect to the details' page when done */
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return validationResponse;
        }
    }

    private ResponseEntity<String> validateProject(Date startDate, Date endDate, Date creationDate, List<Sprint> sprintList, String name) {
        ValidationError dateErrors = ValidationUtils.validateProjectDates(startDate, endDate,
                creationDate, sprintList);
        ValidationError nameErrors = ValidationUtils.validateName(name);
        String errorString = ValidationUtils.joinErrors(dateErrors, nameErrors);
        HttpStatus status = errorString.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorString, status);
    }
}