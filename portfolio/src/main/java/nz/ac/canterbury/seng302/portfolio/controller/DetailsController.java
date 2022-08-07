package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.portfolio.utils.PrincipalData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import java.util.Comparator;
import java.util.List;


/**
 * Controller for the display project details page
 */
@Controller
public class DetailsController extends PageController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private EventService eventService;
    @Autowired
    private SprintLabelService labelUtils;

    /**
     * Get request to view project details page.
     * @param principal Authenticated user
     * @param id ID of the project to be shown
     * @param model Parameters sent to thymeleaf template
     * @return Project details page
     */
    @GetMapping("/project/{id}")
    public String details(
                @AuthenticationPrincipal AuthState principal,
                @PathVariable(name="id") int id,
                Model model
    ) {
        PrincipalData thisUser = PrincipalData.from(principal);

        /* Add project details to the model */
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);
        model.addAttribute("parentProjectId", id);
        model.addAttribute("projectStartDate", DateUtils.toString(project.getProjectStartDate()));
        model.addAttribute("projectEndDate", DateUtils.toString(project.getProjectEndDate()));

        labelUtils.refreshProjectSprintLabels(id);

        // Gets the sprint list and sort it based on the sprint start date
        List<Sprint> sprintList = sprintService.getSprintsInProject(id);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);

        // Gets the event list and sort it based on the event start date
        List<Event> eventList = eventService.getEventByParentProjectId(id);
        eventList.sort(Comparator.comparing(Event::getEventStartDate));
        model.addAttribute("events", eventList);

        // If the user is at least a teacher, the template will render delete/edit buttons
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);
        model.addAttribute("user", thisUser.getFullName());

        model.addAttribute("tab", 0);

        /* Return the name of the Thymeleaf template */
        return "projectDetails";
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
