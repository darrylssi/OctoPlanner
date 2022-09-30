package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SprintForm;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;
import javax.validation.Valid;
import java.util.*;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Controller for endpoints for adding and editing sprints
 */
@Controller
public class SprintController extends PageController {

    @Autowired
    private ProjectService projectService;              // Initializes the ProjectService object
    @Autowired
    private SprintService sprintService;                // Initializes the SprintService object
    @Autowired
    private SprintLabelService labelUtils;

    // Provide a list of colours that are noticeably different for the system to cycle through
    private static final List<String> SPRINT_COLOURS = Arrays.asList(
            "#5aff15",
            "#b83daf",
            "#449dd1",
            "#d6871f");

    /**
     * Adds a sprint to the project
     * @param principal The principal used for authentication (role checking)
     * @param projectId The id of the project to add a sprint to, taken from the URL
     * @param sprintForm The form with the information on the new sprint to be added
     * @param bindingResult The result object that allows for input validation
     * @return To the teacherProjectDetails page
     */
    @PostMapping("/add-sprint/{project_id}")
    public ResponseEntity<String> sprintSave(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @Valid SprintForm sprintForm,
            BindingResult bindingResult
    ){
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }

        // Getting project object by project id
        Project parentProject = projectService.getProjectById(projectId);

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getSprintsInProject(projectId);
        sprintList.sort(Comparator.comparing(Sprint::getSprintEndDate));

        ValidationError dateOutOfRange = getDateValidationError(DateUtils.localDateToDate(sprintForm.getStartDate()), DateUtils.localDateToDate(sprintForm.getEndDate()),
                projectId, parentProject, sprintList);

        ValidationError invalidName = ValidationUtils.validateText(sprintForm.getName(), NAME_REGEX, GlobalVars.NAME_ERROR_MESSAGE);
        ValidationError invalidDescription = ValidationUtils.validateText(sprintForm.getDescription(), DESC_REGEX, DESC_ERROR_MESSAGE);
        if (dateOutOfRange.isError() || invalidName.isError() || invalidDescription.isError()) {
            return new ResponseEntity<>(ValidationUtils.joinErrors(dateOutOfRange, invalidName, invalidDescription), HttpStatus.BAD_REQUEST);
        }

        // Fetch system colour for sprint
        int colourIndex = sprintList.size() % SPRINT_COLOURS.size();
        String sprintColour = SPRINT_COLOURS.get(colourIndex);
        if (!sprintList.isEmpty() && sprintColour.equals(sprintList.get(sprintList.size() - 1).getSprintColour())) {
            sprintColour = SPRINT_COLOURS.get((colourIndex + 1) % SPRINT_COLOURS.size());
        }

        // Adding the new sprint object
        Sprint sprint = new Sprint();
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintForm.getName());
        sprint.setStartDate(DateUtils.localDateToDate(sprintForm.getStartDate()));
        sprint.setEndDate(DateUtils.localDateToDate(sprintForm.getEndDate()));
        sprint.setSprintDescription(sprintForm.getDescription());
        sprint.setSprintLabel(labelUtils.nextLabel(projectId));
        sprint.setSprintColour(sprintColour);

        sprintService.saveSprint(sprint);
        return new ResponseEntity<>("Sprint Updated", HttpStatus.OK);
    }

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
            @PathVariable("projectId") int projectId,
            @PathVariable("id") int id,
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
     * Deletes a sprint
     * @param principal used to check if the user is authorised to delete sprints
     * @param sprintId the id of the sprint to be deleted
     * @return a response entity with the result of the delete request: unauthorised, server error or ok
     */
    @DeleteMapping("/delete-sprint/{sprintId}")
    @ResponseBody
    public ResponseEntity<String> deleteSprint(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="sprintId") int sprintId
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        // Check if the user is authorised to delete sprints
        if (!thisUser.hasRoleOfAtLeast(UserRole.TEACHER)) {
            return new ResponseEntity<>("User not authorised.", HttpStatus.UNAUTHORIZED);
        }
        try {
            sprintService.deleteSprint(sprintId);
            return new ResponseEntity<>("Sprint deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sends the sprints dates and relevant parameters for them to be tested against to be validated
     * @param sprintStartDate Gets the given sprint's start date
     * @param sprintEndDate Gets the given sprint's end date
     * @param id The id of the sprint
     * @param parentProject The sprint's parent project
     * @param sprintList A list of sprints in the same project
     * @return A validation error object, with a boolean error flag and a string list of error messages
     */
    static ValidationError getDateValidationError(@RequestParam(name = "sprintStartDate") Date sprintStartDate,
                                              @RequestParam(name = "sprintEndDate") Date sprintEndDate,
                                              @PathVariable("id") int id, Project parentProject, List<Sprint> sprintList) {
        assert sprintStartDate != null;
        return ValidationUtils.validateSprintDates(id, sprintStartDate, sprintEndDate,
                parentProject, sprintList);
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
    private ResponseEntity<String> validateSprint(Project parentProject, int sprintId, SprintForm sprintForm,
                                                  BindingResult bindingResult, List<Sprint> sprintList) {
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
