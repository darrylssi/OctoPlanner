package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.EventForm;
import nz.ac.canterbury.seng302.portfolio.model.*;
import nz.ac.canterbury.seng302.portfolio.service.*;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import javax.validation.Valid;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

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
     * @param model Parameters sent to thymeleaf template
     * @return Project details page
     */
    @GetMapping("/project/{id}/")
    public String details(
                @AuthenticationPrincipal AuthState principal,
                @PathVariable(name="id") int id,
                EventForm eventForm,
                TimeZone userTimezone,
                Model model
    ){
        PrincipalData thisUser = PrincipalData.from(principal);
        prePopulateEventForm(eventForm, userTimezone.toZoneId());
        populateProjectDetailsModel(model, id, thisUser);

        /* Return the name of the Thymeleaf template */
        return PROJECT_DETAILS_TEMPLATE_NAME;
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
     * @param thisUser          The currently logged in user
     */
    private void populateProjectDetailsModel(Model model, int parentProjectId, PrincipalData thisUser) {
        // Give the template validation info, so the browser can let the user know.
        // Have to enter these one-by-one because Thymeleaf struggles to access utility classes
        model.addAttribute("minNameLen", GlobalVars.MIN_NAME_LENGTH);
        model.addAttribute("maxNameLen", GlobalVars.MAX_NAME_LENGTH);
        model.addAttribute("maxDescLen", GlobalVars.MAX_DESC_LENGTH);
        model.addAttribute("dateISOFormat", GlobalVars.DATE_FORMAT);
        /* Add project details to the model */
        Project project = projectService.getProjectById(parentProjectId);
        model.addAttribute("project", project);

        labelUtils.refreshProjectSprintLabels(parentProjectId);

        // Gets the sprint list and sorts it based on the sprint start date
        List<Sprint> sprintList = sprintService.getSprintsInProject(parentProjectId);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);

        // Gets the event list and sorts it based on the event start date
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
     * Endpoint for adding events to the database, or conveying errors
     * to the user
     * @param projectID The project this event will be bound to
     * @param eventForm The form submitted by our lovely customers
     * @param bindingResult Any errors that came up during validation
     * @return  Either redirects them back to the project page, or renders the project page with errors.
     */
    @PostMapping("/project/{project_id}/add-event")
    public String postAddEvent(
        @AuthenticationPrincipal AuthState principal,
        @PathVariable("project_id") int projectID,
        @Valid EventForm eventForm,
        BindingResult bindingResult,
        TimeZone userTimezone,
        Model model
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        ValidationError dateErrors;
        ValidationError nameErrors;
        // Pattern: Don't do the deeper validation if the data has no integrity (i.e. has nulls)
        if (bindingResult.hasErrors()) {
            populateProjectDetailsModel(model, projectID, thisUser);
            return PROJECT_DETAILS_TEMPLATE_NAME;
        }
        // Check that the dates are correct
        Project parentProject = projectService.getProjectById(projectID);
        dateErrors = ValidationUtils.validateEventDates(eventForm.startDatetimeToDate(userTimezone), eventForm.endDatetimeToDate(userTimezone), parentProject);
        nameErrors = ValidationUtils.validateName(eventForm.getName());
        if (dateErrors.isError() || nameErrors.isError()) {
            // Merge both errors into one
            nameErrors.getErrorMessages().forEach(dateErrors::addErrorMessage);
            model.addAttribute("eventFormError", dateErrors.getErrorMessages());
            populateProjectDetailsModel(model, projectID, thisUser);
            return PROJECT_DETAILS_TEMPLATE_NAME;
        }
        // Data is valid, add it to database
        Event event = new Event(eventForm.getName(), eventForm.getDescription(), eventForm.startDatetimeToDate(userTimezone), eventForm.endDatetimeToDate(userTimezone));
        event.setParentProject(parentProject);
        eventService.saveEvent(event);
        return "redirect:.";
    }

    /**
     * Pre-populates the event form with default values, if they don't already exist
     * @param eventForm The eventForm object from your endpoint args
     */
    private void prePopulateEventForm(EventForm eventForm, ZoneId userTimezone) {
        Instant rightNow = Instant.now();
        Instant inOneMinute = rightNow.plus(1, MINUTES);
        // If field isn't filled (because we just loaded the page), use this default value
        if (eventForm.getStartTime() == null) {
            eventForm.setStartDate(LocalDate.ofInstant(rightNow, userTimezone));
            eventForm.setStartTime(LocalTime.ofInstant(rightNow, userTimezone));
        }
        // Default the value to 1 minute in the future
        if (eventForm.getEndTime() == null) {
            eventForm.setEndDate(LocalDate.ofInstant(inOneMinute, userTimezone));
            eventForm.setEndTime(LocalTime.ofInstant(inOneMinute, userTimezone));
        }
    }

}
