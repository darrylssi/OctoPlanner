package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * Controller to handle requests related to events.
 */
@Controller
public class EventController extends PageController {

    private static final String REDIRECT_TO_PROJECT = "redirect:../project/";

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
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        Event event = eventService.getEventById(eventId);

        // Set new event details
        event.setEventName(name);
        event.setEventDescription(description);
        event.setStartDate(DateUtils.toDateTime(startDate + 'T' + startTime));
        event.setEndDate(DateUtils.toDateTime(endDate + 'T' + endTime));

        eventService.saveEvent(event);

        return REDIRECT_TO_PROJECT + event.getParentProject().getId();
    }
}
