package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SprintForm;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;

import javax.validation.Valid;
import java.util.*;

/**
 * Controller for the add sprint details page
 */
@Controller
public class AddSprintController extends PageController {

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
     * @param userTimezone the current timezone
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return To the teacherProjectDetails page
     */
    @PostMapping("/add-sprint/{project_id}")
    public ResponseEntity<String> sprintSave(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @Valid SprintForm sprintForm,
            BindingResult bindingResult,
            TimeZone userTimezone,
            Model model
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

        ValidationError invalidName = getNameValidationError(sprintForm.getName());
        if (dateOutOfRange.isError() || invalidName.isError()) {
            return new ResponseEntity<>(ValidationUtils.joinErrors(dateOutOfRange, invalidName), HttpStatus.BAD_REQUEST);
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
     * Checks whether the sprint name is valid
     * @param sprintName The sprint name to be tested
     * @return A validation error object, with a boolean error flag and a string list of error messages
     */
    static ValidationError getNameValidationError(String sprintName) {
        return ValidationUtils.validateName(sprintName);
    }

}
