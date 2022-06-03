package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.ValidationService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * Controller for the edit sprint details page
 */
@Controller
public class EditSprintController extends PageController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private SprintLabelService labelUtils;

    @Autowired
    private DateUtils utils;

    /**
     * Show the edit-sprint page.
     * @param id ID of the sprint to be edited
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Edit-sprint page
     */
    @GetMapping("/edit-sprint/{id}")
    public String sprintForm(
            @PathVariable("id") int id,
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        sprint.setId(id);
        model.addAttribute("id", id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("projectId", sprint.getParentProjectId());
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintName", sprint.getSprintName());
        model.addAttribute("sprintStartDate", utils.toString(sprint.getSprintStartDate()));
        model.addAttribute("sprintEndDate", utils.toString(sprint.getSprintEndDate()));
        model.addAttribute("sprintDescription", sprint.getSprintDescription());
        model.addAttribute("sprintColour", sprint.getSprintColour());

        /* Return the name of the Thymeleaf template */
        return "editSprint";
    }

    /**
     * A post request for editing a sprint with a given ID.
     *
     * @param id                ID of the sprint to be edited
     * @param projectId         ID of the sprint's parent project
     * @param sprintName        (New) name of the sprint
     * @param sprintStartDate   (New) start date of the sprint
     * @param sprintEndDate     (New) end date of the sprint
     * @param sprintDescription (New) description of the sprint
     * @return Details page
     */
    @PostMapping("/edit-sprint/{id}")
    public String sprintSave(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @RequestParam(name = "projectId") int projectId,
            @RequestParam(name = "sprintName") String sprintName,
            @RequestParam(name = "sprintStartDate") String sprintStartDate,
            @RequestParam(name = "sprintEndDate") String sprintEndDate,
            @RequestParam(name = "sprintDescription") String sprintDescription,
            @RequestParam(name = "sprintColour") String sprintColour,
            @Valid @ModelAttribute("sprint") Sprint sprint,
            BindingResult result,
            Model model
    ) throws Exception {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        Project parentProject = projectService.getProjectById(projectId);

        Date start = DateUtils.toDate(sprintStartDate);
        Date end = DateUtils.toDate(sprintEndDate);
        List<Sprint> sprintList = sprintService.getAllSprints();
        String dateOutOfRange = validationService.validateSprintDates(sprint.getId(), start, end,
                parentProject, sprintList);

        // Checking if there are errors in the input, and also doing the valid dates validation
        if (result.hasErrors() || !dateOutOfRange.equals("")) {
            model.addAttribute("id", id);
            model.addAttribute("sprint", sprint);
            model.addAttribute("projectId", projectId);
            model.addAttribute("sprintId", id);
            model.addAttribute("sprintName", sprintName);
            model.addAttribute("sprintStartDate", sprintStartDate);
            model.addAttribute("sprintEndDate", sprintEndDate);
            model.addAttribute("sprintDescription", sprintDescription);
            model.addAttribute("invalidDateRange", dateOutOfRange);
            model.addAttribute("sprintColour", sprintColour);
            return "editSprint";
        }

        // Adding the new sprint object
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(utils.toDate(sprintStartDate));
        sprint.setEndDate(utils.toDate(sprintEndDate));
        sprint.setSprintDescription(sprintDescription);
        sprint.setSprintLabel("");  //temporarily set sprint label to blank because it is a required field
        sprint.setSprintColour(sprintColour);

        sprintService.saveSprint(sprint);
        labelUtils.refreshProjectSprintLabels(parentProject); //refresh sprint labels because order of sprints may have changed
        return "redirect:../project/" + projectId;
    }

}
