package nz.ac.canterbury.seng302.portfolio.controller;

import static java.time.temporal.ChronoUnit.MINUTES;

import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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

    @GetMapping("/project/{id}/")
    public String details(
                @AuthenticationPrincipal AuthState principal,
                @PathVariable(name="id") int id,
                EventForm eventForm,
                Model model
    ) throws Exception {
        PrincipalData thisUser = PrincipalData.from(principal);
        prePopulateEventForm(eventForm);
        /* Add project details to the model */
        Project project = projectService.getProjectById(id);
        model.addAttribute("project", project);

        labelUtils.refreshProjectSprintLabels(id);

        List<Sprint> sprintList = sprintService.getSprintsInProject(id);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("sprints", sprintList);

        //
        List<Event> eventList = eventService.getEventByParentProjectId(id);
        sprintList.sort(Comparator.comparing(Sprint::getSprintStartDate));
        model.addAttribute("events", eventList);

        // If the user is at least a teacher, the template will render delete/edit buttons
        boolean hasEditPermissions = thisUser.hasRoleOfAtLeast(UserRole.TEACHER);
        model.addAttribute("canEdit", hasEditPermissions);

        model.addAttribute("tab", 0);

        /* Return the name of the Thymeleaf template */
        return PROJECT_DETAILS_TEMPLATE_NAME;
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
            return PROJECT_DETAILS_TEMPLATE_NAME;
        }

        Project parentProject = projectService.getProjectById(projectID);
        // Test here if the event leaks outside the project's range
        if (eventForm.getStartTime().before(parentProject.getProjectStartDate())) {
            bindingResult.rejectValue("startDate", "Event can't start before the project");
        } else if (eventForm.getStartTime().after(parentProject.getProjectEndDate())) {
            bindingResult.rejectValue("startDate", "Event can't start after the project");
        }
        if (eventForm.getEndTime().before(parentProject.getProjectStartDate())) {
            bindingResult.rejectValue("endDate", "Event can't end before the project");
        } else if (eventForm.getEndTime().after(parentProject.getProjectEndDate())) {
            bindingResult.rejectValue("endDate", "Event can't end after the project");
        }
        // Now that we've added more error, do it again.
        if (bindingResult.hasErrors()) {
            return PROJECT_DETAILS_TEMPLATE_NAME;
        }

        // OK, the data's valid... add it
        Event event = new Event(projectID, eventForm.getName(), eventForm.getDescription(), eventForm.getStartTime(), eventForm.getEndTime());
        eventService.saveEvent(event);
        return "redirect:.";
    }


    private void prePopulateEventForm(EventForm eventForm) {
        Instant rightNow = Instant.now();
        Instant inOneMinute = rightNow.plus(1, MINUTES);
        // If field isn't filled (because we just loaded the page), use this default value
        if (eventForm.getStartTime() == null)
            eventForm.setStartTime(Date.from(rightNow));
        // Default the value to 1 minute in the future
        if (eventForm.getEndTime() == null)
            eventForm.setEndTime(Date.from(inOneMinute));
    }

}
