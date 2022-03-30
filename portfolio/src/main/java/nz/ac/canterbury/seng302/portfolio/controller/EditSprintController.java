package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;


/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

    @Autowired
    ProjectService projectService;

    @Autowired
    SprintService sprintService;

    @Autowired
    private DateUtils utils;

    /**
     * Show the edit-sprint page.
     * @param id ID of the sprint to be edited
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Edit-sprint page
     */
    @GetMapping("/edit-sprint/{id}")
    public String sprintForm(@PathVariable("id") int id, Model model) throws Exception{
        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("sprintId", sprint.getId());

        model.addAttribute("sprintLabel", sprint.getSprintLabel());
        model.addAttribute("sprintName", sprint.getSprintName());
        model.addAttribute("sprintStartDate", utils.toString(sprint.getSprintStartDate()));
        model.addAttribute("sprintEndDate", utils.toString(sprint.getSprintStartDate()));
        model.addAttribute("sprintDescription", sprint.getSprintDescription());



        /* Return the name of the Thymeleaf template */
        return "editSprint";
    }

    @PostMapping("/edit-sprint")
    public String sprintSave(
            @PathVariable("id") int id,
            @RequestParam(value="sprintName") String name,
            @RequestParam(value="sprintStartDate") String startDate,
            @RequestParam(value="sprintEndDate") String endDate,
            @RequestParam(value="sprintDescription") String description
    ) throws Exception {

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
