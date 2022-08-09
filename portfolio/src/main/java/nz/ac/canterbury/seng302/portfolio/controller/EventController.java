package nz.ac.canterbury.seng302.portfolio.controller;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.Date;
import java.util.TimeZone;

import javax.validation.Valid;

import nz.ac.canterbury.seng302.portfolio.model.Event;
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
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * Controller to handle requests related to events.
 */
@Controller
public class EventController extends PageController {

    private static final String ADD_EVENT_FORM_TEMPLATE = "addEvent";
    private static final String REDIRECT_TO_PROJECT = "redirect:../project/";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EventService eventService;

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
            @RequestParam(name = "eventStartTime") String startTime,
            @RequestParam(name = "eventEndDate") String endDate,
            @RequestParam(name = "eventEndTime") String endTime
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        Event event = eventService.getEventById(eventId);

        // Set new event details
        event.setEventName(name);
        event.setEventDescription(description);
        event.setStartDate(DateUtils.toDateTime(startDate + 'T' + startTime));
        event.setEndDate(DateUtils.toDateTime(endDate + 'T' + endTime));

        eventService.saveEvent(event);

        return REDIRECT_TO_PROJECT + event.getParentProjectId();
    }
}
