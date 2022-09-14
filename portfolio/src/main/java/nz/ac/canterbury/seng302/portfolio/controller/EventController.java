package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.controller.forms.SchedulableForm;
import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.StringJoiner;
import java.util.TimeZone;

/**
 * Controller to handle requests related to events.
 */
@Controller
public class EventController extends PageController {

    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    @Autowired
    private ProjectService projectService;
    @Autowired
    private EventService eventService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    DetailsController detailsController;


    /**
     * Endpoint for adding events to the database, or conveying errors
     * to the user
     * @param principal Authenticated user
     * @param projectID The project this event will be bound to
     * @param schedulableForm Form that stores information about the deadline
     * @param bindingResult Any errors that came up during validation
     * @param userTimezone The user's time zone
     * @param model Parameters sent to thymeleaf template
     * @return  A response of either 200 (success), 403 (forbidden),
     *          or 400 (Given event failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/add-event")
    public ResponseEntity<String> postAddEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectID,
            @Valid SchedulableForm schedulableForm,
            BindingResult bindingResult,
            TimeZone userTimezone,
            Model model
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        ValidationError dateErrors;
        ValidationError nameErrors;
        // Pattern: Don't do the deeper validation if the data has no integrity (i.e. has nulls)
        if (bindingResult.hasErrors()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: bindingResult.getAllErrors()) {
                errors.add(err.getDefaultMessage());
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        // Check that the dates are correct
        Project parentProject = projectService.getProjectById(projectID);
        dateErrors = ValidationUtils.validateEventDates(schedulableForm.startDatetimeToDate(userTimezone), schedulableForm.endDatetimeToDate(userTimezone), parentProject);
        nameErrors = ValidationUtils.validateName(schedulableForm.getName());
        if (dateErrors.isError() || nameErrors.isError()) {
            StringJoiner errors = new StringJoiner("\n");
            for (var err: dateErrors.getErrorMessages()) {
                errors.add(err);
            }
            for (var err: nameErrors.getErrorMessages()) {
                errors.add(err);
            }
            return new ResponseEntity<>(errors.toString(), HttpStatus.BAD_REQUEST);
        }
        // Data is valid, add it to database
        Event event = new Event(schedulableForm.getName(), schedulableForm.getDescription(), schedulableForm.startDatetimeToDate(userTimezone), schedulableForm.endDatetimeToDate(userTimezone));
        event.setParentProject(parentProject);

        Event savedEvent = eventService.saveEvent(event);
        logger.info("Added new Event {}", savedEvent.getId());

        return ResponseEntity.ok(String.valueOf(savedEvent.getId()));
    }

    /**
     * Handle edit requests for events. Validate the form and determine the response
     * @param principal Authenticated user
     * @param projectId The ID of the project the event belongs to
     * @param eventId The ID of the event to be edited
     * @param editSchedulableForm The form submitted by the user
     * @param bindingResult Any errors that occurred while constraint checking the form
     * @param userTimeZone  The timezone the user's based in
     * @return  A response of either 200 (success), 403 (forbidden),
     *          or 400 (Given event failed validation, replies with what errors occurred)
     */
    @PostMapping("/project/{project_id}/edit-event/{event_id}")
    @ResponseBody
    public ResponseEntity<String> postEditEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @PathVariable("event_id") int eventId,
            @Valid @ModelAttribute SchedulableForm editSchedulableForm,
            BindingResult bindingResult,
            TimeZone userTimeZone
    ) {
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
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
        var dateErrors = ValidationUtils.validateEventDates(editSchedulableForm.startDatetimeToDate(userTimeZone), editSchedulableForm.endDatetimeToDate(userTimeZone), event.getParentProject());
        var nameError = ValidationUtils.validateName(editSchedulableForm.getName());
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
        event.setName(editSchedulableForm.getName());
        event.setDescription(editSchedulableForm.getDescription());
        event.setStartDate(editSchedulableForm.startDatetimeToDate(userTimeZone));
        event.setEndDate(editSchedulableForm.endDatetimeToDate(userTimeZone));

        Event savedEvent = eventService.saveEvent(event);
        logger.info("Edited event {}", eventId);

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
        // Check if the user is authorised for this
        try {
            requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        } catch (ResponseStatusException ex) {
            return new ResponseEntity<>(ex.getReason(), ex.getStatus());
        }

        try {
            eventService.deleteEvent(eventId);
            return new ResponseEntity<>("Event deleted.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
