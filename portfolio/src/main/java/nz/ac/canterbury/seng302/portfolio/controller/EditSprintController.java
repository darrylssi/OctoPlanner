package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.text.ParseException;
import java.util.Date;


/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

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
    public String sprintForm(@PathVariable("id") int id, Model model) throws Exception{
        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        model.addAttribute("id", id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintLabel", sprint.getLabel());
        model.addAttribute("startDate", utils.toString(sprint.getStartDate()));
        model.addAttribute("endDate", utils.toString(sprint.getEndDate()));
        model.addAttribute("name", sprint.getName());
        model.addAttribute("description", sprint.getDescription());

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
            @Valid Sprint sprint,
            BindingResult result,
            @PathVariable("id") int id,
            @RequestParam(value="name") String name,
            @RequestParam(value="startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(value="endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(value="description") String description,
            Model model
    ) throws Exception {
        Sprint newSprint = sprintService.getSprintById(id);

        if (result.hasErrors()) {
            for (ObjectError error : result.getAllErrors()) {
                System.out.println(error);
            }



            // return to edit sprint page with user input
            // doesn't work though :)
            sprint.setStartDate(startDate);
            sprint.setEndDate(endDate);
            sprint.setParentProjectId(newSprint.getParentProjectId());
            sprint.setSprintName(name);
            sprint.setSprintDescription(description);

            model.addAttribute("sprint", sprint);
            model.addAttribute("sprintId", newSprint.getId());
            model.addAttribute("startDate", utils.toString(sprint.getStartDate()));
            model.addAttribute("endDate", utils.toString(sprint.getEndDate()));

            return "editSprint";
        }

        /* Set (new) sprint details to the corresponding sprint */
        newSprint.setSprintName(name);
        newSprint.setStartDate(startDate);
        newSprint.setEndDate(endDate);
        newSprint.setSprintDescription(description);
        sprintService.saveSprint(newSprint);

        return "redirect:/project/" + id;
    }

}
