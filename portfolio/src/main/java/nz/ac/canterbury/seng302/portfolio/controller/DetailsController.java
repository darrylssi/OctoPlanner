package nz.ac.canterbury.seng302.portfolio.controller;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import nz.ac.canterbury.seng302.portfolio.controller.forms.EventForm;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.GlobalVars;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;


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
    private SprintLabelService labelUtils;

    private int currentUpdatedEventId = -1;

    /**
     * Get request to view project details page.
     * @param principal Authenticated user
     * @param id ID of the project to be shown
     * @param model Parameters sent to thymeleaf template
     * @return Project details page
     * @throws Exception When project does not exist
     */
    @GetMapping("/project/{id}/")
    public String details(
                @AuthenticationPrincipal AuthState principal,
                @PathVariable(name="id") int id,
                EventForm eventForm,
                TimeZone userTimezone,
                Model model
    ) throws Exception {
        PrincipalData thisUser = PrincipalData.from(principal);
        prePopulateEventForm(eventForm, userTimezone.toZoneId());
        populateProjectDetailsModel(model, id, thisUser);

        /* Return the name of the Thymeleaf template */
        return PROJECT_DETAILS_TEMPLATE_NAME;
    }

        /**
     Redirects so any project page URL gets a slash on the end
     */
    @GetMapping("/project/{id}")
    public String detailsRedirect(
                @PathVariable(name="id") int id
    ) {
        return "redirect:" + id + '/';
    }


    /**
     * <p>Pre-populates all the data needed in the model</p>
     *
     * Note: You should ONLY DEFINE MODEL ATTRIBUTES IN HERE!
     * @param model The model we'll be blessing with knowledge
     * @param parentProjectId   The ID of this project page
     * @param thisUser          The currently logged in user
     * @throws Exception    Gotta stop doing this, honestly
     */
    private void populateProjectDetailsModel(Model model, int parentProjectId, PrincipalData thisUser) throws Exception {
        // Give the template validation info, so the browser can let the user know.
        // Have to enter these one-by-one because Thymeleaf struggles to access utility classes
        model.addAttribute("minNameLen", GlobalVars.MIN_NAME_LENGTH);
        model.addAttribute("maxNameLen", GlobalVars.MAX_NAME_LENGTH);
        model.addAttribute("maxDescLen", GlobalVars.MAX_DESC_LENGTH);
        model.addAttribute("datetimeISOFormat", GlobalVars.DATETIME_ISO_FORMAT);
        /* Add project details to the model */
        Project project = projectService.getProjectById(parentProjectId);
        model.addAttribute("project", project);

        labelUtils.refreshProjectSprintLabels(parentProjectId);

        // Gets the sprint list and sort it based on the sprint start date
        List<Sprint> sprintList = sprintService.getSprintsInProject(parentProjectId);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);

        // Gets the event list and sort it based on the event start date
        List<Event> eventList = eventService.getEventByParentProjectId(parentProjectId);
        eventList.sort(Comparator.comparing(Event::getEventStartDate));
        model.addAttribute("events", eventList);

        // If the user is at least a teacher, the template will render delete/edit buttons
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);
        model.addAttribute("user", thisUser.getFullName());

        model.addAttribute("tab", 0);
        model.addAttribute("eventId", currentUpdatedEventId);
        currentUpdatedEventId = -1;
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
     * @throws Exception We've gotta find a better way of conveying "not found" then basic Exceptions
     */
    @PostMapping("/project/{project_id}/add-event")
    public String postAddEvent(
        @AuthenticationPrincipal AuthState principal,
        @PathVariable("project_id") int projectID,
        @Valid EventForm eventForm,
        BindingResult bindingResult,
        TimeZone userTimezone,
        Model model
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        // Initial checks that the data has some integrity (The data isn't null, etc.)
        if (bindingResult.hasErrors()) {
            PrincipalData thisUser = PrincipalData.from(principal);
            populateProjectDetailsModel(model, projectID, thisUser);
            return PROJECT_DETAILS_TEMPLATE_NAME;
        }

        Event event = new Event(projectID, eventForm.getName(), eventForm.getDescription(), eventForm.startDatetimeToDate(userTimezone), eventForm.endDatetimeToDate(userTimezone));
        Event savedEvent = eventService.saveEvent(event);
        currentUpdatedEventId = savedEvent.getId();
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

    /**
     * Deletes an event and redirects back to the project view
     * @param principal used to check if the user is authorised to delete events
     * @param eventId the id of the event to be deleted
     * @return a redirect to the project view
     */
    @DeleteMapping("/delete-event/{eventId}")
    @ResponseBody
    public ResponseEntity<String> deleteEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="eventId") int eventId
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        // Check if the user is authorised to delete events
        if (!thisUser.hasRoleOfAtLeast(UserRole.TEACHER)) {
            return new ResponseEntity<>("User not authorised.", HttpStatus.UNAUTHORIZED);
        }
        try {
            eventService.deleteEvent(eventId);
            return new ResponseEntity<>("Event deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
