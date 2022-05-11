package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import javax.validation.Valid;
import java.util.Comparator;
import java.util.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Controller for the add sprint details page
 */
@Controller
public class AddSprintController {

    @Autowired
    private ProjectService projectService;              // Initializes the ProjectService object
    @Autowired
    private SprintService sprintService;                // Initializes the SprintService object
    @Autowired
    private SprintLabelService labelUtils;

    // Initializes the DateUtils object to be used for converting date to string and string to date
    @Autowired
    private DateUtils utils;

    /**
     * Form to add new sprints to a project. Fields are pre-filled with default values to be edited
     * @param id the id of the project the sprint belongs to
     * @param model the model used to store information to be displayed on the page
     * @return the name of the Thymeleaf .html page to be displayed
     * @throws Exception
     */
    @GetMapping("/add-sprint/{id}")
    public String getsSprint(@PathVariable("id") int id, Model model) throws Exception {

        /* Getting project object by using project id */
        Project project = projectService.getProjectById(id);
        List<Sprint> sprintList = sprintService.getAllSprints();

        // Creating a new sprint object
        Sprint sprint = new Sprint();
        sprint.setParentProjectId(id);          // Setting parent project id

        model.addAttribute("sprint", sprint);
        model.addAttribute("parentProjectId", id);
        model.addAttribute("projectName", project.getProjectName());
        model.addAttribute("sprintName", labelUtils.nextLabel(id));
        model.addAttribute("sprintDescription", "");

        // Puts the default sprint start date
        String getSprintStartDate = "";
        if (sprintList.size() == 0) {
            getSprintStartDate += utils.toString(project.getProjectStartDate());
        } else {
            String getLocalSprintStartDate = utils.toString(sprintList.get(sprintList.size()-1).getSprintEndDate());

            // Creating default start date for the new sprint
            // Converting the date to LocalDate, so we can add the three weeks of default end date
            String newDate = utils.toString(new SimpleDateFormat("yyyy-MM-dd").parse(getLocalSprintStartDate));

            // Converting date to LocalDate
            Instant instant = utils.toDate(newDate).toInstant();
            ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
            LocalDate sprintOldStartDate = zdt.toLocalDate();

            // Adding 3 weeks (21 days) of default sprint end date
            LocalDate sprintLocalStartDate = sprintOldStartDate.plusDays(1);

            // Converting the new sprint end date of LocalDate object to Date object
            getSprintStartDate += utils.toString(Date.from(sprintLocalStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        model.addAttribute("sprintStartDate", getSprintStartDate);


        // Creating default end date for the new sprint
        // Converting the date to LocalDate, so we can add the three weeks of default end date
        String newDate = utils.toString(new SimpleDateFormat("yyyy-MM-dd").parse(getSprintStartDate));

        // Converting date to LocalDate
        Instant instant = utils.toDate(newDate).toInstant();
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        LocalDate sprintOldEndDate = zdt.toLocalDate();

        // Adding 3 weeks (21 days) of default sprint end date
        LocalDate sprintLocalEndDate = sprintOldEndDate.plusDays(21);

        // Converting the new sprint end date of LocalDate object to Date object
        Date sprintNewEndDate = Date.from(sprintLocalEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        //Check if end date falls outside project dates
        if(sprintNewEndDate.after(project.getProjectEndDate())){
            sprintNewEndDate = project.getProjectEndDate();
        };

        model.addAttribute("sprintEndDate", utils.toString(sprintNewEndDate));
        model.addAttribute("minDate", utils.toString(project.getProjectStartDate()));
        model.addAttribute("maxDate", utils.toString(project.getProjectEndDate()));

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
        // Getting project object by project id
        Project parentProject = projectService.getProjectById(sprint.getParentProjectId());

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();

        // Checking the sprint dates validation and returning appropriate error message
        Date utilsProjectStartDate = parentProject.getProjectStartDate();
        Date utilsProjectEndDate = parentProject.getProjectEndDate();
        String dateOutOfRange = sprint.validAddSprintDateRanges(utils.toDate(sprintStartDate),utils.toDate(sprintEndDate), utilsProjectStartDate, utilsProjectEndDate,  sprintList);

        // Checking it there are errors in the input, and also doing the valid dates validation
        if (result.hasErrors() || !dateOutOfRange.equals("")) {
            model.addAttribute("parentProjectId", id);
            model.addAttribute("sprint", sprint);
            model.addAttribute("projectName", parentProject.getProjectName());
            model.addAttribute("minDate", utils.toString(parentProject.getProjectStartDate()));
            model.addAttribute("maxDate", utils.toString(parentProject.getProjectEndDate()));
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
        sprint.setEndDate(utils.toDate(sprintEndDate));
        sprint.setSprintDescription(sprintDescription);
        sprint.setSprintLabel(labelUtils.nextLabel(id));

        sprintService.saveSprint(sprint);
        return "redirect:/project/" + parentProject.getId();
    }

}
