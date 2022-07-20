package nz.ac.canterbury.seng302.portfolio.controller;

import java.time.Instant;
import java.time.ZonedDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import nz.ac.canterbury.seng302.portfolio.controller.forms.EventForm;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.EventService;
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

    @PostMapping("/project/{project_id}/add-event")
    public String postAddEvent(
        @AuthenticationPrincipal AuthState principal,
        @PathVariable("project_id") int projectID,
        @Valid EventForm eventForm,
        BindingResult bindingResult,
        TimeZone timeZone
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);
        // Initial return so I don't have to also null test
        if (bindingResult.hasErrors()) {
            return ADD_EVENT_FORM_TEMPLATE;
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
            return ADD_EVENT_FORM_TEMPLATE;
        }

        // OK, the data's valid... add it
        var start = new GregorianCalendar(timeZone);
        start.setTime(eventForm.getStartTime());
        var end = new GregorianCalendar(timeZone);
        end.setTime(eventForm.getStartTime());
        System.out.println(start);
        System.out.println(end);
        // eventService.saveEvent(event);

        return "redirect:..";
    }
}
