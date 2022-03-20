package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;


/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

    @Autowired
    private SprintService sprintService;

    @Autowired
    private DateUtils utils;

    @GetMapping("/edit-sprint/{id}")
    public String sprintForm(@PathVariable("id") int id, Model model) {
        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintLabel", sprint.getLabel());
        model.addAttribute("sprintName", sprint.getName());
        model.addAttribute("sprintStartDate", utils.toString(sprint.getStartDate()));
        model.addAttribute("sprintEndDate", utils.toString(sprint.getStartDate()));
        model.addAttribute("sprintDescription", sprint.getDescription());

        /* Return the name of the Thymeleaf template */
        return "editSprint";
    }

    @PostMapping("/edit-sprint/{id}")
    public String sprintSave(
            @PathVariable("id") int id,
            @RequestParam(value="sprintName") String name,
            @RequestParam(value="sprintStartDate") String startDate,
            @RequestParam(value="sprintEndDate") String endDate,
            @RequestParam(value="sprintDescription") String description
    ) throws ParseException {
        Sprint sprint = sprintService.getSprintById(id);
        sprint.setSprintName(name);
        sprint.setStartDate(utils.toDate(startDate));
        sprint.setEndDate(utils.toDate(endDate));
        sprint.setSprintDescription(description);
        sprintService.saveSprint(sprint);
        return "redirect:/details";
    }

}
