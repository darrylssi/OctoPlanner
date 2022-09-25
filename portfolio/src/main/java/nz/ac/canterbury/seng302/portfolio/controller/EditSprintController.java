package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SprintForm;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
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
import java.util.List;
import java.util.StringJoiner;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.NAME_ERROR_MESSAGE;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.NAME_REGEX;

/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController extends PageController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private SprintLabelService labelUtils;

    /**
     * A post request for editing a sprint with a given ID.
     * @param principal The authenticated or currently logged in user
     * @param id ID of the sprint to be edited
     * @param projectId ID of the sprint's parent project
     * @param sprintForm The form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @return A response entity that contains any errors that were found. Bad Request if there were errors, Ok if there are none
     */
    @PostMapping("/project/{projectId}/edit-sprint/{id}")
    public ResponseEntity<String> postEditSprint(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @RequestParam(name = "projectId") int projectId,
            @Valid @ModelAttribute SprintForm sprintForm,
            BindingResult bindingResult
    ) {
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        Project parentProject = projectService.getProjectById(projectId);
        List<Sprint> sprintList = sprintService.getSprintsInProject(projectId);

        ResponseEntity<String> validationResponse = validateSprint(parentProject, id, sprintForm, bindingResult, sprintList);

        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            Sprint sprint = sprintService.getSprintById(id);

            /* Set (new) sprint details to the corresponding sprint */
            sprint.setSprintName(sprintForm.getName());
            sprint.setStartDate(DateUtils.localDateToDate(sprintForm.getStartDate()));
            sprint.setEndDate(DateUtils.localDateToDate(sprintForm.getEndDate()));
            sprint.setSprintDescription(sprintForm.getDescription());
            sprintService.saveSprint(sprint);

            /* Redirect to the details' page when done */
            return new ResponseEntity<>("", HttpStatus.OK);
        } else {
            return validationResponse;
        }

    }

    /**
     * This validates sprints when they are edited.
     * @param parentProject Object containing details of a project
     * @param sprintId Id of the sprint
     * @param sprintForm Form containing details of a sprint
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param sprintList List of sprints in the project
     * @return A response entity that contains any errors that were found; Bad Request if there are errors, Ok if there are none
     */
    private ResponseEntity<String> validateSprint(Project parentProject, int sprintId, SprintForm sprintForm, BindingResult bindingResult, List<Sprint> sprintList) {

        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        ValidationError dateErrors = ValidationUtils.validateSprintDates(sprintId, DateUtils.localDateToDate(sprintForm.getStartDate()),
                DateUtils.localDateToDate(sprintForm.getEndDate()), parentProject, sprintList);
        ValidationError nameErrors = ValidationUtils.validateText(sprintForm.getName(), NAME_REGEX, NAME_ERROR_MESSAGE);
        String errorString = ValidationUtils.joinErrors(dateErrors, nameErrors, new ValidationError());
        HttpStatus status = errorString.isEmpty() ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(errorString, status);
    }

}
