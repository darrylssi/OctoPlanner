package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.*;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import nz.ac.canterbury.seng302.portfolio.controller.forms.SprintForm;
import javax.validation.Valid;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import static java.time.temporal.ChronoUnit.MINUTES;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


/**
 * Controller for the display project details page
 */
@Controller
public class DetailsController extends PageController {

    public static final String PROJECT_DETAILS_TEMPLATE_NAME = "projectDetails";

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private EventService eventService;
    @Autowired
    private DeadlineService deadlineService;
    @Autowired
    private MilestoneService milestoneService;
    @Autowired
    private SprintLabelService labelUtils;

    /**
     * Get request to view project details page.
     * @param principal Authenticated user
     * @param id ID of the project to be shown
     * @param schedulableForm The form submitted by the user
     * @param userTimezone The user's time zone
     * @param model Parameters sent to thymeleaf template
     * @return Project details page
     */
    @GetMapping("/project/{id}/")
    public String details(
                @AuthenticationPrincipal AuthState principal,
                @PathVariable(name="id") int id,
                SchedulableForm schedulableForm,
                TimeZone userTimezone,
                Model model
    ){
        PrincipalData thisUser = PrincipalData.from(principal);
        prePopulateSchedulableForm(schedulableForm, userTimezone.toZoneId());
        populateProjectDetailsModel(model, id, thisUser);

        return PROJECT_DETAILS_TEMPLATE_NAME;   // Return the name of the Thymeleaf template
    }

    /**
     * Redirects so any project page URL gets a slash on the end
     * */
    @GetMapping("/project/{id}")
    public String detailsRedirect(
                @PathVariable(name="id") int id
    ) {
        return "redirect:" + id + '/';
    }


    /**
     * <p>Pre-populates all the data needed in the model</p>
     *
     * @param model The model we'll be blessing with knowledge
     * @param parentProjectId   The ID of this project page
     * @param thisUser          The currently logged-in user
     */
    public void populateProjectDetailsModel(Model model, int parentProjectId, PrincipalData thisUser) {
        // Give the template validation info, so the browser can let the user know.
        // Have to enter these one-by-one because Thymeleaf struggles to access utility classes
        model.addAttribute("minNameLen", GlobalVars.MIN_NAME_LENGTH);
        model.addAttribute("maxNameLen", GlobalVars.MAX_NAME_LENGTH);
        model.addAttribute("maxDescLen", GlobalVars.MAX_DESC_LENGTH);
        model.addAttribute("dateISOFormat", GlobalVars.DATE_FORMAT);

        /* Add project details to the model */
        Project project = projectService.getProjectById(parentProjectId);
        model.addAttribute("project", project);
        model.addAttribute("projectStart", DateUtils.toString(project.getProjectStartDate()));
        model.addAttribute("projectEnd", DateUtils.toString(project.getProjectEndDate()));
        model.addAttribute("sprintForm", new SprintForm());

        labelUtils.refreshProjectSprintLabels(parentProjectId);

        // Gets the sprint list and sorts it based on the sprint start date
        List<Sprint> sprintList = sprintService.getSprintsInProject(parentProjectId);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);

        // Gets the event, deadline and milestone lists and sorts them based on their start dates
        List<Event> eventList = eventService.getEventByParentProjectId(parentProjectId);
        List<Deadline> deadlineList = deadlineService.getDeadlineByParentProjectId(parentProjectId);
        List<Milestone> milestoneList = milestoneService.getMilestoneByParentProjectId(parentProjectId);

        List<Schedulable> schedulableList = new ArrayList<>();
        schedulableList.addAll(eventList);
        schedulableList.addAll(deadlineList);
        schedulableList.addAll(milestoneList);

        // Sorts schedulable list by start dates.
        schedulableList.sort(Comparator.comparing(Schedulable::getStartDate));
        model.addAttribute("schedulables", schedulableList);

        // If the user is at least a teacher, the template will render delete/edit buttons
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);
        model.addAttribute("user", thisUser.getFullName());
        model.addAttribute("userId", thisUser.getID());
        model.addAttribute("editSchedulableForm", new SchedulableForm());

        model.addAttribute("tab", 0);
    }

    /**
     * Deletes a sprint and redirects back to the project view
     * @param principal used to check if the user is authorised to delete sprints
     * @param sprintId the id of the sprint to be deleted
     * @return a redirect to the project view
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
     * Pre-populates the event form with default values, if they don't already exist
     * @param schedulableForm The schedulableForm object from your endpoint args
     * @param userTimezone The user's time zone for calculating the correct start dates
     */
    private void prePopulateSchedulableForm(SchedulableForm schedulableForm, ZoneId userTimezone) {
        Instant rightNow = Instant.now();
        Instant inOneMinute = rightNow.plus(1, MINUTES);
        // If field isn't filled (because we just loaded the page), use this default value
        if (schedulableForm.getStartTime() == null) {
            schedulableForm.setStartDate(LocalDate.ofInstant(rightNow, userTimezone));
            schedulableForm.setStartTime(LocalTime.ofInstant(rightNow, userTimezone));
        }
        // Default the value to 1 minute in the future
        if (schedulableForm.getEndTime() == null) {
            schedulableForm.setEndDate(LocalDate.ofInstant(inOneMinute, userTimezone));
            schedulableForm.setEndTime(LocalTime.ofInstant(inOneMinute, userTimezone));
        }
    }

    /**
     * A method to get the html of a schedulable that can be added to the details
     * page using javascript
     * @param principal the current user
     * @param schedulableType the type of schedulable
     * @param schedulableId the id of the schedulable being displayed
     * @param boxId the id of the box in the html element being created
     * @param model the model that stores the attributes of the schedulable
     * @return an html fragment of the given schedulable
     */
    @GetMapping("/frag/{type}/{schedulableId}/{boxId}")
    public String schedulableFragment(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="type") String schedulableType,
            @PathVariable(name="schedulableId") int schedulableId,
            @PathVariable(name="boxId") String boxId,
            Model model
    ){
        PrincipalData thisUser = PrincipalData.from(principal);
        Schedulable schedulable;
        if (EVENT_TYPE.equals(schedulableType)) {
            schedulable = eventService.getEventById(schedulableId);
        } else if (DEADLINE_TYPE.equals(schedulableType)) {
            schedulable = deadlineService.getDeadlineById(schedulableId);
        } else if (MILESTONE_TYPE.equals(schedulableType)) {
            schedulable = milestoneService.getMilestoneById(schedulableId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, schedulableType + " is not a type of schedulable!");
        }
        List<Sprint> sprints = sprintService.getSprintsInProject(schedulable.getParentProject().getId());
        model.addAttribute(schedulableType, schedulable);
        model.addAttribute("canEdit", thisUser.hasRoleOfAtLeast(UserRole.TEACHER));
        model.addAttribute("boxId", boxId);
        model.addAttribute("sprints", sprints);
        model.addAttribute("minNameLen", GlobalVars.MIN_NAME_LENGTH);
        model.addAttribute("maxNameLen", GlobalVars.MAX_NAME_LENGTH);
        model.addAttribute("maxDescLen", GlobalVars.MAX_DESC_LENGTH);
        model.addAttribute("projectStart", DateUtils.toString(schedulable.getParentProject().getProjectStartDate()));
        model.addAttribute("projectEnd", DateUtils.toString(schedulable.getParentProject().getProjectEndDate()));
        model.addAttribute("editSchedulableForm", new SchedulableForm());

        return "detailFragments :: " + schedulableType;
    }
}
