package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.Date;


/**
 * Controller for the add sprint details page
 */
@Controller
public class AddSprintController {

    @Autowired
    ProjectService projectService;

    @Autowired
    SprintService sprintService;

    @Autowired
    private DateUtils utils;

    /**
     * Gets the project name and creates a new sprint label
     * @param model Used to display the project name in title
     * @return The sprint add page
     */
    @GetMapping("/add-sprint/{id}")
    public String getsSprint(@PathVariable("id") int id, Model model) {

        /* Add project details to the model */
        Project project = projectService.getProjectById(id);

        Sprint sprint = new Sprint();
        sprint.setParentProjectId(id);

        model.addAttribute("projectName", project.getName() + " - Add Sprint");
        model.addAttribute("sprintLabel", "Add Sprint - Sprint " + sprint.getId());
        model.addAttribute("sprintName",  "");
        model.addAttribute("sprintStartDate", "");
        model.addAttribute("sprintStartDate", "");
        model.addAttribute("sprintDescription",  "");

        /* Return the name of the Thymeleaf template */
        return "addSprint";
    }

    /**
     * Adds a sprint to the project
     * @param sprintName Gets the given name of the new sprint
     * @param sprintStartDate Gets the given sprint start date
     * @param sprintEndDate Gets the given sprint end date
     * @param sprintDescription Gets the given sprint description
     * @return To the teacherProjectDetails page
     */
    @PostMapping("/add-sprint/{id}")
    public String projectSave(
            @PathVariable("id") int id,
            @RequestParam(value="sprintName") String sprintName,
            @RequestParam(value="sprintStartDate") String sprintStartDate,
            @RequestParam(value="sprintEndDate") String sprintEndDate,
            @RequestParam(value="sprintDescription") String sprintDescription,
            Sprint sprint,
            Model model
    ) throws ParseException {

        Project parentProject = projectService.getProjectById(sprint.getParentProjectId());
        if (parentProject.getStartDate().after(sprint.getStartDate())) {
            return "addSprint";
        }

        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(utils.toDate(sprintStartDate));
        sprint.setEndDate(utils.toDate(sprintEndDate));
        sprint.setSprintDescription(sprintDescription);

        sprintService.saveOrUpdateSprint(sprint);
        return "details";
    }

}
