package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
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

import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.TimeZone;

/**
 * Controller to handle requests related to events.
 */
@Controller
public class EventController extends PageController {

    @Autowired
    private EventService eventService;
    @Autowired
    private SprintService sprintService;

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
            @RequestParam(name="editEventName") String name,
            @RequestParam(name="editEventDescription") String description,
            @RequestParam(name="editEventStartDate") Date startDate,
            @RequestParam(name="editEventStartTime") String startTime,
            @RequestParam(name="editEventEndDate") Date endDate,
            @RequestParam(name="editEventEndTime") String endTime,
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
        // // Validation round 2: Do our custom errors pass? TODO: Refactor this (GEORGE)
        // var dateErrors = ValidationUtils.validateEventDates(editEventForm.startDatetimeToDate(userTimeZone), editEventForm.endDatetimeToDate(userTimeZone), event.getParentProject());
        // var nameError = ValidationUtils.validateName(editEventForm.getName());
        // if (dateErrors.isError() || nameError.isError()) {
        //     StringJoiner errors = new StringJoiner("\n");
        //     for (var err: dateErrors.getErrorMessages()) {
        //         errors.add(err);
        //     }
        //     for (var err: nameError.getErrorMessages()) {
        //         errors.add(err);
        //     }
        //     return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        // }
        // Set new event details
        event.setName(name);
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);  //TODO: factor in the time (GEORGE). Find a way to accept it as not a string

        Event savedEvent = eventService.saveEvent(event);

        return ResponseEntity.ok(String.valueOf(savedEvent.getId()));
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

    @GetMapping("/event-frag/{eventId}")
    public String eventFragment(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable(name="eventId") int eventId,
            Model model
    ){
        PrincipalData thisUser = PrincipalData.from(principal);
        Event event = eventService.getEventById(eventId);
        List<Sprint> sprints = sprintService.getSprintsInProject(event.getParentProject().getId());
        model.addAttribute("event", event);
        model.addAttribute("sprints", sprints);
        model.addAttribute("canEdit", thisUser.hasRoleOfAtLeast(UserRole.TEACHER));
        model.addAttribute("boxId", "temp-box-id");
        model.addAttribute("minNameLen", GlobalVars.MIN_NAME_LENGTH);
        model.addAttribute("maxNameLen", GlobalVars.MAX_NAME_LENGTH);
        model.addAttribute("maxDescLen", GlobalVars.MAX_DESC_LENGTH);
        model.addAttribute("projectStart", event.getParentProject().getProjectStartDate());
        model.addAttribute("projectEnd", event.getParentProject().getProjectEndDate());

        return "detailFragments :: event";
    }
}
