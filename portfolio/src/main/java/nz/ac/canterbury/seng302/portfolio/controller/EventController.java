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
     * 
     * @param eventForm The form submitted by the user
     * @param bindingResult Any errors that occured while constraint checking the form
     * @param userTimeZone  The timezone the user's based in
     * @return  A response of either 200 (success), 401 (unauthenticated),
     *          or 400 (Given event failed validation, replies with what errors occured)
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
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        // Validation round 2: Do our custom errors pass?
        var dateErrors = ValidationUtils.validateEventDates(eventForm.startDatetimeToDate(userTimeZone), eventForm.endDatetimeToDate(userTimeZone), event.getParentProject());
        var nameError = ValidationUtils.validateName(eventForm.getName());
        if (dateErrors.isError() || nameError.isError()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: dateErrors.getErrorMessages()) {
                errors.add(err);
            }
            for (var err: nameError.getErrorMessages()) {
                errors.add(err);
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        // Set new event details
        event.setName(eventForm.getName());
        event.setDescription(eventForm.getDescription());
        event.setStartDate(eventForm.startDatetimeToDate(userTimeZone));
        event.setEndDate(eventForm.endDatetimeToDate(userTimeZone));

        eventService.saveEvent(event);

        return ResponseEntity.ok("");
    }
}
