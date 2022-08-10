package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.EventForm;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;

import java.util.TimeZone;
import java.util.StringJoiner;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

/**
 * Controller to handle requests related to events.
 */
@Controller
public class EventController extends PageController {

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
    @PostMapping("/project/{project_id}/edit-event/{event_id}")
    @ResponseBody
    public ResponseEntity<String> postEditEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @PathVariable("event_id") int eventId,
            @Valid EventForm eventForm,
            BindingResult bindingResult,
            TimeZone userTimeZone
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);
        // Check if the user is authorised to edit events
        if (!thisUser.hasRoleOfAtLeast(UserRole.TEACHER)) {
            return new ResponseEntity<>("User not authorised.", HttpStatus.UNAUTHORIZED);
        }
        // Validation round 1: Do the Javax Validation annotations pass?
        Event event = eventService.getEventById(eventId);
        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.toString());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        // Validation round 2: Do our custom errors pass?
        var generalErrors = ValidationUtils.validateEventDates(eventForm.startDatetimeToDate(userTimeZone), eventForm.endDatetimeToDate(userTimeZone), event.getParentProject());
        if (generalErrors.isError()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: generalErrors.getErrorMessages()) {
                errors.add(err);
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        // Set new event details
        event.setEventName(eventForm.getName());
        event.setEventDescription(eventForm.getDescription());
        event.setStartDate(eventForm.startDatetimeToDate(userTimeZone));
        event.setEndDate(eventForm.endDatetimeToDate(userTimeZone));

        eventService.saveEvent(event);

        return ResponseEntity.ok("");
    }
}
