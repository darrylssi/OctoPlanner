package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


/**
 * Controller for the display project details page
 */
@Controller
public class MonthlyCalendarController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private UserAccountClientService userAccountClientService;


    /**
     *
     * @param principal Current authentication state
     * @param id Current project id
     * @param model Used to display the current attributes being passed to the html page
     * @return The monthly calendar page
     * @throws Exception
     */
    @GetMapping("/monthlyCalendar/{id}")
    public String getMonthlyCalendar(
                                    @AuthenticationPrincipal AuthState principal,
                                    @PathVariable(name="id") int id,
                                    Model model) throws Exception {
        // Get the current project id
        Project project = projectService.getProjectById(id);

        // Get current user's username for the header
        model.addAttribute("userName", userAccountClientService.getUsernameById(principal));
        model.addAttribute("project", project);
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