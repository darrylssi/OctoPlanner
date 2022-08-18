package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.EventForm;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;

import java.util.TimeZone;
import java.util.Date;
import java.util.StringJoiner;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import static nz.ac.canterbury.seng302.portfolio.controller.DetailsController.PROJECT_DETAILS_TEMPLATE_NAME;

/**
 * Controller to handle requests related to events.
 */
@Controller
public class EventController extends PageController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private EventService eventService;
    @Autowired
    DetailsController detailsController;


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
        ValidationError dateErrors = null;
        ValidationError nameErrors = null;
        // Pattern: Don't do the deeper validation if the data has no integrity (i.e. has nulls)
        if (bindingResult.hasErrors()) {
            detailsController.populateProjectDetailsModel(model, projectID, thisUser);
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
            detailsController.populateProjectDetailsModel(model, projectID, thisUser);
            return PROJECT_DETAILS_TEMPLATE_NAME;
        }
        // Data is valid, add it to database
        Event event = new Event(eventForm.getName(), eventForm.getDescription(), eventForm.startDatetimeToDate(userTimezone), eventForm.endDatetimeToDate(userTimezone));
        event.setParentProject(parentProject);
        eventService.saveEvent(event);
        return "redirect:.";
    }

    /**
     * 
     * @param eventForm The form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param userTimeZone  The timezone the user's based in
     * @return  A response of either 200 (success), 401 (unauthenticated),
     *          or 400 (Given event failed validation, replies with what errors occurred)
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
        event.setEventName(name);
        event.setEventDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);  //TODO: factor in the time (GEORGE). Find a way to accept it as not a string

        eventService.saveEvent(event);

        return ResponseEntity.ok("");
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
