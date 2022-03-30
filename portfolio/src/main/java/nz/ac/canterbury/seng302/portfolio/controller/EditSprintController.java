package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import nz.ac.canterbury.seng302.portfolio.model.ErrorType;

import java.text.ParseException;
import java.util.List;

/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController extends PageController {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private DateUtils utils;

    /**
     * Show the edit-sprint page.
     * @param id ID of the sprint to be edited
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Edit-sprint page
     */
    @GetMapping("/edit-sprint/{id}")
    public String sprintForm(@PathVariable("id") int id, 
            @AuthenticationPrincipal AuthState principal,
            Model model) throws Exception {
        /* Ensure that the user is at least a teacher */
        List<String> roles = getUserRole(principal);
        if (!(roles.contains("teacher") || roles.contains("course_administrator"))) {
            configureError(model, ErrorType.ACCESS_DENIED, "/edit-project");
            return "error";
        }

        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintLabel", sprint.getLabel());
        model.addAttribute("sprintName", sprint.getName());
        model.addAttribute("sprintStartDate", utils.toString(sprint.getStartDate()));
        model.addAttribute("sprintEndDate", utils.toString(sprint.getStartDate()));
        model.addAttribute("sprintDescription", sprint.getDescription());

        /* Return the name of the Thymeleaf template */
        return "editSprint";
    }

    /**
     * Post request for editing a sprint with a given ID.
     * @param id ID of the sprint to be edited
     * @param name (New) name of the sprint
     * @param startDate (New) start date of the sprint
     * @param endDate (New) end date of the sprint
     * @param description (New) description of the sprint
     * @return Details page
     * @throws ParseException If date is of a different format than expected
     */
    @PostMapping("/edit-sprint/{id}")
    public String sprintSave(
            @PathVariable("id") int id,
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value="sprintName") String name,
            @RequestParam(value="sprintStartDate") String startDate,
            @RequestParam(value="sprintEndDate") String endDate,
            @RequestParam(value="sprintDescription") String description,
            Model model
    ) throws Exception {
        /* Ensure that the user is at least a teacher */
        List<String> roles = getUserRole(principal);
        if (!(roles.contains("teacher") || roles.contains("course_administrator"))) {
            configureError(model, ErrorType.ACCESS_DENIED, "/edit-project");
            return "error";
        }

        /* Set (new) sprint details to the corresponding sprint */
        Sprint sprint = sprintService.getSprintById(id);
        sprint.setSprintName(name);
        sprint.setStartDate(utils.toDate(startDate));
        sprint.setEndDate(utils.toDate(endDate));
        sprint.setSprintDescription(description);
        sprintService.saveSprint(sprint);

        return "redirect:/project/" + id;
    }

}
