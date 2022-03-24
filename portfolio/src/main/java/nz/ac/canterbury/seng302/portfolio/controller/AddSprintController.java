package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import org.springframework.format.annotation.DateTimeFormat;

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
        model.addAttribute("sprintLabel", "Add Sprint - Sprint 1");
//        model.addAttribute("sprintLabel", "Add Sprint - Sprint " + sprint.getId());
        model.addAttribute("sprintName",  "");
        model.addAttribute("sprintDescription",  "");
        model.addAttribute("sprintDateError",  "");

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
            @RequestParam(name="sprintName") String sprintName,
            @RequestParam(name="sprintStartDate") Date sprintStartDate,
            @RequestParam(name="sprintEndDate") Date sprintEndDate,
            @RequestParam(name="sprintDescription") String sprintDescription,
            @ModelAttribute("sprintDateError") String sprintDateError,
            BindingResult result,
            Sprint sprint,
            Model model
    ) throws ParseException {

        Project parentProject = projectService.getProjectById(sprint.getParentProjectId());

        if (result.hasErrors()) {
            fetchDateErrorResult(parentProject, sprintStartDate, sprintEndDate, model);
        }
        // Adding the new sprint
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(sprintStartDate);
        sprint.setEndDate(sprintEndDate);
        sprint.setSprintDescription(sprintDescription);

        sprintService.saveOrUpdateSprint(sprint);
        return "details";


    }


    @RequestMapping(value="/add-sprint/error" , method=RequestMethod.GET)
    public @ResponseBody String fetchDateErrorResult(Project project, Date sprintStartDate, Date sprintEndDate, Model model) {

        if (!sprintStartDate.after(project.getStartDate()) || !project.getEndDate().after(sprintEndDate)) {
            model.addAttribute("sprintDateError", "The sprint dates must be within the project dates.");
        } else if (!sprintEndDate.after(sprintStartDate)) {
            model.addAttribute("sprintDateError", "The start sprint date must be before end sprint date.");
        } else {
            model.addAttribute("sprintDateError", "The project dates are incorrect.");
        }
        return "addSprint";
    }


}
