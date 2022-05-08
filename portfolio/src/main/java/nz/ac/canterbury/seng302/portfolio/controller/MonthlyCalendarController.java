package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for displaying the project's monthly planner
 */
public class MonthlyCalendarController {

    @Autowired
    private UserAccountClientService userAccountClientService; // Initializes the UserAccountClientService object
    @Autowired
    private ProjectService projectService;              // Initializes the ProjectService object
    @Autowired
    private SprintService sprintService;                // Initializes the SprintService object

    private static final Logger logger = LoggerFactory.getLogger(EditSprintController.class);

    /**
     *
     * @param principal Current authentication state
     * @param id Current project id
     * @param model Used to display the current attributes being passed to the html page
     * @return The monthly calendar page
     * @throws Exception
     */
    @GetMapping("/monthlyCalendar/{id}")
    public String getMonthlyCalendar(@AuthenticationPrincipal AuthState principal,
                             @PathVariable(name="id") int id, Model model) throws Exception {
        logger.info("ha ha ha ----> " + id + " <---- finally here");
        // Get the current project id
        Project project = projectService.getProjectById(id);

        // Gets current user's username
        model.addAttribute("userName", userAccountClientService.getUsernameById(principal));
        model.addAttribute("projectId", id);
        model.addAttribute("project", project);
        model.addAttribute("projectName", project.getProjectName());
        model.addAttribute("sprintList", sprintService.getSprintsOfProjectById(id));
        return "monthlyCalendar";
    }

    /**
     *
     * @param principal Current authentication state
     * @param id Current user id
     * @param result It is used to check errors and send appropriate response based on it
     * @param model Used to display the current attributes being passed to the html page
     * @return The monthly calendar page
     */
    @PostMapping("/monthlyCalendar/{id}")
    public String postMonthlyCalendar(@AuthenticationPrincipal AuthState principal,
                             @PathVariable(name="id") int id,
                             BindingResult result,
                             Model model
    ) {

        return "monthlyCalendar";
    }

}
