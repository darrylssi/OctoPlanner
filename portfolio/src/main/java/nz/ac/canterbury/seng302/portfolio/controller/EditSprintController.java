package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Date;


/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController {

    @Autowired
    private ProjectService projectService;

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
    public String sprintForm(@PathVariable("id") int id, Model model) throws Exception {
        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        model.addAttribute("id", id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("projectId", sprint.getParentProjectId());
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintLabel", "Edit Sprint - Sprint " + sprint.getId());
        model.addAttribute("sprintName", sprint.getSprintName());
        model.addAttribute("sprintStartDate", utils.toString(sprint.getSprintStartDate()));
        model.addAttribute("sprintEndDate", utils.toString(sprint.getSprintEndDate()));
        model.addAttribute("sprintDescription", sprint.getSprintDescription());

        /* Return the name of the Thymeleaf template */
        return "editSprint";
    }

    /**
     * Post request for editing a sprint with a given ID.
     *
     * @param result
     * @param id                ID of the sprint to be edited
     * @param projectId
     * @param sprintName        (New) name of the sprint
     * @param sprintStartDate   (New) start date of the sprint
     * @param sprintEndDate     (New) end date of the sprint
     * @param sprintDescription (New) description of the sprint
     * @param sprint
     * @param model
     * @return Details page
     * @throws Exception
     */
    @PostMapping("/edit-sprint/{id}")
    public String sprintSave(
            BindingResult result,
            @PathVariable("id") int id,
            @RequestParam(value = "projectId") int projectId,
            @RequestParam(value = "sprintName") String sprintName,
            @RequestParam(value = "sprintStartDate") String sprintStartDate,
            @RequestParam(value = "sprintEndDate") String sprintEndDate,
            @RequestParam(value = "sprintDescription") String sprintDescription,
            @Valid @ModelAttribute("sprint") Sprint sprint,
            Model model
    ) throws Exception {
        Project parentProject = projectService.getProjectById(projectId);


        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
        String dateOutOfRange = "";
        String sprintNewEndDate = "";

        // Checking if the sprint end date is not selected
        if (sprintEndDate == null || sprintEndDate == "") {
            // Converting the date to LocalDate, so we can add the three weeks of default end date
            String newDate = utils.toString(new SimpleDateFormat("yyyy-MM-dd").parse(sprintStartDate));

            // Converting date to LocalDate
            Instant instant = utils.toDate(newDate).toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            LocalDate sprintOldEndDate = zdt.toLocalDate();

            // Adding 3 weeks (21 days) of default sprint end date
            LocalDate sprintLocalEndDate = sprintOldEndDate.plusDays(21);

            // Converting the new sprint end date of LocalDate object to Date object
            sprintNewEndDate += utils.toString(Date.from(sprintLocalEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));

            // Checking the sprint dates validation with default sprint end date and returning appropriate error message
            dateOutOfRange += sprint.validSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintNewEndDate), parentProject.getStartDate(), parentProject.getEndDate(), sprintList);

        } else {
            sprintNewEndDate += sprintEndDate;

            // Checking the sprint dates validation and returning appropriate error message
            dateOutOfRange += sprint.validSprintDateRanges(utils.toDate(sprintStartDate), utils.toDate(sprintNewEndDate), parentProject.getStartDate(), parentProject.getEndDate(), sprintList);
        }

        // Checking it there are errors in the input, and also doing the valid dates validation
        if (result.hasErrors() || !dateOutOfRange.equals("")) {
            model.addAttribute("id", id);
            model.addAttribute("sprint", sprint);
            model.addAttribute("projectId", projectId);
            model.addAttribute("sprintId", id);
            model.addAttribute("sprintLabel", "Edit Sprint - Sprint " + id);
            model.addAttribute("sprintName", sprintName);
            model.addAttribute("sprintStartDate", sprintStartDate);
            model.addAttribute("sprintEndDate", sprintEndDate);
            model.addAttribute("sprintDescription", sprintDescription);

            model.addAttribute("invalidDateRange", dateOutOfRange);
            return "addSprint";
        }

        // Adding the new sprint object
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(utils.toDate(sprintStartDate));
        sprint.setEndDate(utils.toDate(sprintNewEndDate));
        sprint.setSprintDescription(sprintDescription);

        sprintService.saveSprint(sprint);
        return"redirect:/details";
    }

}
