package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintLabelService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
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
public class EditSprintController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private SprintLabelService labelUtils;
    @Autowired
    private UserAccountClientService userAccountClientService;
    @Autowired
    private DateUtils utils;


    /**
     * Show the edit-sprint page.
     * @param id ID of the sprint to be edited
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Edit-sprint page
     */
    @GetMapping("/edit-sprint/{id}")
    public String sprintForm(@AuthenticationPrincipal AuthState principal,
                             @PathVariable("id") int id, Model model) throws Exception {
        /* Add sprint details to the model */
        Sprint sprint = sprintService.getSprintById(id);
        sprint.setId(id);
        // Get current user's username for the header
        model.addAttribute("userName", userAccountClientService.getUsernameById(principal));
        model.addAttribute("id", id);
        model.addAttribute("sprint", sprint);
        model.addAttribute("projectId", sprint.getParentProjectId());
        model.addAttribute("sprintId", sprint.getId());
        model.addAttribute("sprintLabel", " Edit Sprint - " + labelUtils.nextLabel(id) );
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
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("id") int id,
            @RequestParam(name = "projectId") int projectId,
            @RequestParam(name = "sprintName") String sprintName,
            @RequestParam(name = "sprintStartDate") String sprintStartDate,
            @RequestParam(name = "sprintEndDate") String sprintEndDate,
            @RequestParam(name = "sprintDescription") String sprintDescription,
            @Valid @ModelAttribute("sprint") Sprint sprint,
            BindingResult result,
            Model model
    ) throws Exception {
        Project parentProject = projectService.getProjectById(projectId);

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();

        //
        Date utilsProjectStartDate = utils.toDate(utils.toString(parentProject.getProjectStartDate()));
        Date utilsProjectEndDate = utils.toDate(utils.toString(parentProject.getProjectEndDate()));
        String dateOutOfRange = sprint.validEditSprintDateRanges(sprint.getId(), utils.toDate(sprintStartDate), utils.toDate(sprintEndDate), utilsProjectStartDate, utilsProjectEndDate, sprintList);

        // Checking it there are errors in the input, and also doing the valid dates validation
        if (result.hasErrors() || !dateOutOfRange.equals("")) {
            // Get current user's username for the header
            model.addAttribute("userName", userAccountClientService.getUsernameById(principal));
            model.addAttribute("id", id);
            model.addAttribute("sprint", sprint);
            model.addAttribute("projectId", projectId);
            model.addAttribute("sprintId", id);
            model.addAttribute("sprintLabel", "Edit Sprint - " + labelUtils.nextLabel(id));
            model.addAttribute("sprintName", sprintName);
            model.addAttribute("sprintStartDate", sprintStartDate);
            model.addAttribute("sprintEndDate", sprintEndDate);
            model.addAttribute("sprintDescription", sprintDescription);
            model.addAttribute("invalidDateRange", dateOutOfRange);
            return "editSprint";
        }

        // Adding the new sprint object
        sprint.setParentProjectId(parentProject.getId());
        sprint.setSprintName(sprintName);
        sprint.setStartDate(utils.toDate(sprintStartDate));
        sprint.setEndDate(utils.toDate(sprintEndDate));
        sprint.setSprintDescription(sprintDescription);

        sprintService.saveSprint(sprint);
        return "redirect:/project/" + projectId;
    }


}
