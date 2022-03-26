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

import javax.validation.Valid;
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
    public String getsSprint(@PathVariable("id") int id, Model model) throws Exception {

        /* Add project details to the model */
        Project project = projectService.getProjectById(id);

        Sprint sprint = new Sprint();
        sprint.setParentProjectId(id);

        model.addAttribute("sprint", sprint);

        model.addAttribute("parentProjectId", id);
        model.addAttribute("projectName", project.getName() + " - Add Sprint");
        model.addAttribute("sprintLabel", "Add Sprint - Sprint 1");
//        model.addAttribute("sprintLabel", "Add Sprint - Sprint " + sprint.getId());
//        model.addAttribute("sprintStartDate", "");
//        model.addAttribute("sprintEndDate", "");

        model.addAttribute("sprintName",  "");
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
    public String sprintSave(
            @PathVariable("id") int id,
            @RequestParam(name="sprintName") String sprintName,
            @RequestParam(name="sprintStartDate") String sprintStartDate,
            @RequestParam(name="sprintEndDate") String sprintEndDate,
            @RequestParam(name="sprintDescription") String sprintDescription,
            @Valid @ModelAttribute("sprint") Sprint sprint,
            BindingResult result,
            Model model
    ) throws Exception {

        if (result.hasErrors()) {
            model.addAttribute("sprintStartDate", utils.toDate(sprintStartDate));
            model.addAttribute("sprintEndDate", utils.toDate(sprintEndDate));
//            model.addAttribute("sprintStartDate", sprintStartDate);
//            model.addAttribute("sprintEndDate", sprintEndDate);
            return "addSprint";
        }

        // Adding the new sprint
        Project parentProject = projectService.getProjectById(sprint.getParentProjectId());
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(utils.toDate(sprintStartDate));
        sprint.setEndDate(utils.toDate(sprintEndDate));
//        sprint.setStartDate(sprintStartDate);
//        sprint.setEndDate(sprintEndDate);
        sprint.setSprintDescription(sprintDescription);

        sprintService.saveSprint(sprint);
        return "redirect:/details";
    }

//
//    @RequestMapping(value="/add-sprint/error" , method=RequestMethod.GET)
//    public @ResponseBody String fetchDateErrorResult(Project project, Date sprintStartDate, Date sprintEndDate, Model model) {
//
//        if (!sprintStartDate.after(project.getStartDate()) || !project.getEndDate().after(sprintEndDate)) {
//            model.addAttribute("sprintDateError", "The sprint dates must be within the project dates.");
//        } else if (!sprintEndDate.after(sprintStartDate)) {
//            model.addAttribute("sprintDateError", "The start sprint date must be before end sprint date.");
//        } else {
//            model.addAttribute("sprintDateError", "The project dates are incorrect.");
//        }
//        return "addSprint";
//    }


}
