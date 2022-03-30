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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(EditSprintController.class);

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
        sprint.setId(id);
        model.addAttribute("id", id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("projectId", sprint.getParentProjectId());
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintLabel", " Edit Sprint - Sprint " + sprint.getId());
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
            @PathVariable("id") int id,
            @RequestParam(value = "projectId") int projectId,
            @RequestParam(value = "sprintName") String sprintName,
            @RequestParam(value = "sprintStartDate") String sprintStartDate,
            @RequestParam(value = "sprintEndDate") String sprintEndDate,
            @RequestParam(value = "sprintDescription") String sprintDescription,
            @Valid @ModelAttribute("sprint") Sprint sprint,
            BindingResult result,
            Model model
    ) throws Exception {
        Project parentProject = projectService.getProjectById(projectId);

//        Sprint updateSprint = sprintService.getSprintById(id);


        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
//        Integer newSprintId = Integer.valueOf(id);

        String dateOutOfRange = sprint.validEditSprintDateRanges(sprint.getId(), utils.toDate(sprintStartDate), utils.toDate(sprintEndDate), parentProject.getProjectStartDate(), parentProject.getProjectEndDate(), sprintList);

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
            logger.info("here editSprint");
            logger.info(dateOutOfRange);

            return "editSprint";
        }

        // Adding the new sprint object
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(utils.toDate(sprintStartDate));
        sprint.setEndDate(utils.toDate(sprintEndDate));
        sprint.setSprintDescription(sprintDescription);

        sprintService.saveSprint(sprint);
        logger.info("here saveSprint");
        return "redirect:/project/" + projectId;
    }

}
