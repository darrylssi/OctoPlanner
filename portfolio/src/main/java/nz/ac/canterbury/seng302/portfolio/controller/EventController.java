package nz.ac.canterbury.seng302.portfolio.controller;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.Date;
import java.util.TimeZone;

import javax.validation.Valid;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.portfolio.controller.forms.EventForm;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

@Controller
public class EventController extends PageController {

    static final String ADD_EVENT_FORM_TEMPLATE = "addEvent";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private EventService eventService;

    @GetMapping("/project/{project_id}/add-event")
    public String getAddEvent(
        @AuthenticationPrincipal AuthState principal,
        @PathVariable("project_id") int projectID,
        EventForm eventForm,
        TimeZone timeZone,
        Model model
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        // Get a timezoned "right now" for the current user
        Instant rightNow = ZonedDateTime.now(timeZone.toZoneId()).toInstant();
        // If field isn't filled (because we just loaded the page), use this default value
        if (eventForm.getStartDate() == null)
            eventForm.setStartDate(Date.from(rightNow));
        // Default the value to 1 minute in the future
        if (eventForm.getEndDate() == null) {
            Instant inOneMinute = rightNow.plus(1, MINUTES);
            eventForm.setEndDate(Date.from(inOneMinute));
        }
        Project parentProject = projectService.getProjectById(projectID);
        // TODO Andrew: Once Jacque's validation utils are merged, check that
        // the event takes place inside the project properly
        Date earliestDate = parentProject.getProjectStartDate();
        Date latestDate = parentProject.getProjectEndDate();
        model.addAttribute("parentProject", parentProject);
        model.addAttribute("earliestDate", earliestDate);
        model.addAttribute("latestDate", latestDate);
        return ADD_EVENT_FORM_TEMPLATE;
    }

    @PostMapping("/project/{project_id}/add-event")
    public String postAddEvent(
        @AuthenticationPrincipal AuthState principal,
        @PathVariable("project_id") int projectID,
        @Valid EventForm eventForm,
        BindingResult bindingResult
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        // Initial return so I don't have to also null test
        if (bindingResult.hasErrors()) {
            return ADD_EVENT_FORM_TEMPLATE;
        }

        Project parentProject = projectService.getProjectById(projectID);
        // Test here if the event leaks outside the project's range
        if (eventForm.getStartDate().before(parentProject.getProjectStartDate())) {
            bindingResult.rejectValue("startDate", "Event can't start before the project");
        } else if (eventForm.getStartDate().after(parentProject.getProjectEndDate())) {
            bindingResult.rejectValue("startDate", "Event can't start after the project");
        }
        if (eventForm.getEndDate().before(parentProject.getProjectStartDate())) {
            bindingResult.rejectValue("endDate", "Event can't end before the project");
        } else if (eventForm.getEndDate().after(parentProject.getProjectEndDate())) {
            bindingResult.rejectValue("endDate", "Event can't end after the project");
        }
        
        // Now that we've added more error, do it again.
        if (bindingResult.hasErrors()) {
            return ADD_EVENT_FORM_TEMPLATE;
        }

        return "redirect:../";
    }

    /**
     * Post request for editing an event with a given ID.
     * @param principal The authenticated user.
     * @param eventId ID of the event to be edited.
     * @param name New name of the event, if changed.
     * @param description New description of the event, if changed.
     * @param startDate New start date and time of the event, if changed.
     * @param endDate New end date and time of the event, if changed.
     * @return Project details page.
     * @throws Exception when the event of the said ID does not exist.
     */
    @PostMapping("/edit-event/{event_id}")
    public String postEditEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("event_id") int eventId,
            @RequestParam(name = "eventName") String name,
            @RequestParam(name = "eventDescription") String description,
            @RequestParam(name = "eventStartDate") String startDate,
            @RequestParam(name = "eventEndDate") String endDate
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        Event event = eventService.getEventById(eventId);

        // Set new event details
        event.setEventName(name);
        event.setEventDescription(description);
        event.setStartDate(DateUtils.toDateTime(startDate));
        event.setEndDate(DateUtils.toDateTime(endDate));

        eventService.saveEvent(event);

        return "redirect:../project/0";
    }
}
