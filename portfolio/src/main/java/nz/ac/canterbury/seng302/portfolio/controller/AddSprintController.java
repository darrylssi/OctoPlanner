package nz.ac.canterbury.seng302.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for the add sprint details page
 */
@Controller
public class AddSprintController {

    @Autowired
    SprintService sprintService;

    /**
     * Gets the project name and creates a new sprint label
     * @param model Used to display the project name in title
     * @param project Used to get the project name
     * @return The sprint add page
     */
    @GetMapping("/add-sprint")
    public String getsSprint(Model model, Project project) {

        Sprint sprint = new Sprint();
        model.addAttribute("projectName", project.getName() + " - Add Sprint");
        model.addAttribute("sprintLabel", "Add Sprint - " + sprint.getLabel());

        /* Return the name of the Thymeleaf template */
        return "addSprint";
    }

    /**
     * Adds a sprint to the project
     * @param sprintName Gets the given name of the new sprint
     * @param sprintStartDate Gets the given sprint start date
     * @param sprintEndDate Gets the given sprint end date
     * @param sprintDescription Gets the given sprint description
     * @param project Gets the current project
     * @return To the teacherProjectDetails page
     */
    @PostMapping("/add-sprint")
    public String projectSave(
            @RequestParam(name="sprintName") String sprintName,
            @RequestParam(name="sprintStartDate") String sprintStartDate,
            @RequestParam(name="sprintEndDate") String sprintEndDate,
            @RequestParam(name="sprintDescription") String sprintDescription,
            Project project,
            Model model
    ) {
        Sprint newSprint = new Sprint(project.getId(), sprintName, sprintDescription, sprintStartDate, sprintEndDate);
        sprintService.saveOrUpdateSprint(newSprint);
        return "teacherProjectDetails";
    }

}
