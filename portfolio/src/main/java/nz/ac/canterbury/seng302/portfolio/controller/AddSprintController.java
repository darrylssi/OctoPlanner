package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
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

    private static final String ADD_SPRINT_TEMPLATE = "addSprint";
    private static final String REDIRECT_TO_PROJECT = "redirect:../project/";

    // Provide a list of colours that are noticeably different for the system to cycle through
    private static final List<String> SPRINT_COLOURS = Arrays.asList(
            "#5aff15",
            "#b83daf",
            "#449dd1",
            "#d6871f");

    /**
     * Form to add new sprints to a project. Fields are pre-filled with default values to be edited
     * @param id the id of the project the sprint belongs to
     * @param model the model used to store information to be displayed on the page
     * @return the name of the Thymeleaf .html page to be displayed
     */
    @GetMapping("/add-sprint/{id}")
    public String getsSprint(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            Model model
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        /* Getting project object by using project id */
        Project project = projectService.getProjectById(id);
        List<Sprint> sprintList = sprintService.getSprintsInProject(id);

        // Creating a new sprint object
        Sprint sprint = new Sprint();
        sprint.setParentProjectId(id);          // Setting parent project id

        model.addAttribute("sprint", sprint);
        model.addAttribute("parentProjectId", id);
        model.addAttribute("projectName", project.getProjectName());
        model.addAttribute("sprintName", labelUtils.nextLabel(id));
        model.addAttribute("sprintDescription", "");

        // Calculate the default sprint start date
        Date sprintStart;
        Calendar c = Calendar.getInstance();
        if (sprintList.isEmpty()) { // Use project start date when there are no sprints
            sprintStart = project.getProjectStartDate();
            c.setTime(sprintStart);
        } else {
            Date lastSprintEnd = sprintList.get(sprintList.size()-1).getSprintEndDate();
            c.setTime(lastSprintEnd);
            c.add(Calendar.DAY_OF_MONTH, 1);    // Day after last sprint ends
            sprintStart = c.getTime();
        }

        // This only happens when the last sprint finishes on the same day as the project
        if (sprintStart.after(project.getProjectEndDate())) {
            model.addAttribute("invalidDateRange",
                    "There is no room for more sprints in this project");
        }

        model.addAttribute("sprintStartDate", DateUtils.toString(sprintStart));

        // Calculate the default sprint end date
        Date sprintEnd;
        c.add(Calendar.DAY_OF_MONTH, 21);   // 3 weeks after sprint starts

        // Checks that the default end date is within the project dates
        if (ValidationUtils.sprintsOutsideProject(sprintStart, c.getTime(),
                project.getProjectStartDate(), project.getProjectEndDate())){
            sprintEnd = project.getProjectEndDate();    // Use project end date if there is an overlap
        } else {
            sprintEnd = c.getTime();
        }
        model.addAttribute("sprintEndDate", DateUtils.toString(sprintEnd));
        model.addAttribute("minDate", DateUtils.toString(project.getProjectStartDate()));
        model.addAttribute("maxDate", DateUtils.toString(project.getProjectEndDate()));

        /* Return the name of the Thymeleaf template */
        return ADD_SPRINT_TEMPLATE;
    }

    /**
     * Adds a sprint to the project
     * @param principal The principal used for authentication (role checking)
     * @param id The id of the project to add a sprint to, taken from the URL
     * @param sprintName Gets the given name of the new sprint
     * @param sprintStartDate Gets the given sprint's start date
     * @param sprintEndDate Gets the given sprint's end date
     * @param sprintDescription Gets the given sprint description
     * @param sprint The new sprint to be added
     * @param result The result object that allows for input validation
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return To the teacherProjectDetails page
     * @throws Exception if project not found or a date cannot be parsed
     */
    @PostMapping("/add-sprint/{id}")
    public String sprintSave(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @RequestParam(name="sprintName") String sprintName,
            @RequestParam(name="sprintStartDate") String sprintStartDate,
            @RequestParam(name="sprintEndDate") String sprintEndDate,
            @RequestParam(name="sprintDescription") String sprintDescription,
            @Valid @ModelAttribute("sprint") Sprint sprint,
            BindingResult result,
            Model model
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting project object by project id
        Project parentProject = projectService.getProjectById(sprint.getParentProjectId());

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getSprintsInProject(sprint.getParentProjectId());
        sprintList.sort(Comparator.comparing(Sprint::getSprintEndDate));

        // Fetch system colour for sprint
        int colourIndex = sprintList.size() % SPRINT_COLOURS.size();
        String sprintColour = SPRINT_COLOURS.get(colourIndex);
        if (!sprintList.isEmpty() && sprintColour.equals(sprintList.get(sprintList.size() - 1).getSprintColour())) {
            sprintColour = SPRINT_COLOURS.get((colourIndex + 1) % SPRINT_COLOURS.size());
        }

        ValidationError dateOutOfRange = getDateValidationError(sprintStartDate, sprintEndDate,
                id, parentProject, sprintList);

        ValidationError invalidName = getNameValidationError(sprintName);

        // Checking it there are errors in the input, and also doing the valid dates validation
        if (result.hasErrors() || dateOutOfRange.isError() || invalidName.isError()) {
            model.addAttribute("parentProjectId", id);
            model.addAttribute("sprint", sprint);
            model.addAttribute("projectName", parentProject.getProjectName());
            model.addAttribute("minDate", DateUtils.toString(parentProject.getProjectStartDate()));
            model.addAttribute("maxDate", DateUtils.toString(parentProject.getProjectEndDate()));
            model.addAttribute("sprintName", sprintName);
            model.addAttribute("sprintStartDate", sprintStartDate);
            model.addAttribute("sprintEndDate", sprintEndDate);
            model.addAttribute("sprintDescription", sprintDescription);
            model.addAttribute("invalidDateRange", dateOutOfRange.getFirstError());
            model.addAttribute("invalidName", invalidName.getFirstError());

            return ADD_SPRINT_TEMPLATE;
        }

        // Adding the new sprint object
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(DateUtils.toDate(sprintStartDate));
        sprint.setEndDate(DateUtils.toDate(sprintEndDate));
        sprint.setSprintDescription(sprintDescription);
        sprint.setSprintLabel(labelUtils.nextLabel(id));
        sprint.setSprintColour(sprintColour);

        sprintService.saveSprint(sprint);
        return REDIRECT_TO_PROJECT + parentProject.getId();
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
    static ValidationError getDateValidationError(@RequestParam(name = "sprintStartDate") String sprintStartDate,
                                              @RequestParam(name = "sprintEndDate") String sprintEndDate,
                                              @PathVariable("id") int id, Project parentProject, List<Sprint> sprintList) {
        Date start = DateUtils.toDate(sprintStartDate);
        Date end = DateUtils.toDate(sprintEndDate);
        assert start != null;
        return ValidationUtils.validateSprintDates(id, start, end,
                parentProject, sprintList);
    }

    /**
     * Checks whether the sprint name is valid
     * @param sprintName Sprint name to be tested
     * @return A validation error object, with a boolean error flag and a string list of error messages
     */
    static ValidationError getNameValidationError(String sprintName) {
        return ValidationUtils.validateName(sprintName);
    }


}
