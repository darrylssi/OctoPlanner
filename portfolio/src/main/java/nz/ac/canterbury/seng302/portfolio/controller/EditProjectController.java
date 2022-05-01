package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.User;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.service.UserAccountClientService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.ClaimDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;


/**
 * Controller for the edit project details page
 */
@Controller
public class EditProjectController {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;
    @Autowired
    private UserAccountClientService userAccountClientService;

    @Autowired
    private DateUtils utils;

    private String globalUsername;

    /**
     * Show the edit-project page.
     * @param id ID of the project to be edited
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Edit-project page
     */
    @GetMapping("/edit-project/{id}")
    public String projectForm(@AuthenticationPrincipal AuthState principal,
                              @PathVariable("id") int id, User user, Model model) {

        /* Add project details to the model */
        try {
            // Setting the current user's username at the header
            String currentUserId = principal.getClaimsList().stream()
                    .filter(claim -> claim.getType().equals("nameid"))
                    .findFirst()
                    .map(ClaimDTO::getValue)
                    .orElse("NOT FOUND");

            String getUsername = userAccountClientService.getUserAccountById(Integer.parseInt(currentUserId)).getUsername();
            globalUsername = getUsername;
            model.addAttribute("userName", getUsername);

            Project project = projectService.getProjectById(id);
            model.addAttribute("id", id);
            model.addAttribute("project", project);
            model.addAttribute("projectStartDate", utils.toString(project.getProjectStartDate()));
            model.addAttribute("projectEndDate", utils.toString(project.getProjectEndDate()));
            model.addAttribute("projectDescription", project.getProjectDescription());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found", e);
        }

        /* Return the name of the Thymeleaf template */
        return "editProject";
    }

    /**
     * Post request for editing a project with a given ID.
     * @param id ID of the project to be edited
     * @param projectName (New) name of the project
     * @param projectStartDate (New) project start date
     * @param projectEndDate (New) project end date
     * @param projectDescription (New) project description
     * @return Details page
     * @throws Exception If the date cannot be parsed
     */
    @PostMapping("/edit-project/{id}")
    public String projectSave(
            @AuthenticationPrincipal AuthState principal,
            @Valid Project project,
            BindingResult result,
            @PathVariable("id") int id,
            @RequestParam(value="projectName") String projectName,
            @RequestParam(value="projectStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectStartDate,
            @RequestParam(value="projectEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectEndDate,
            @RequestParam(value="projectDescription") String projectDescription,
            Model model
    ) throws Exception {
        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();

        //
        Date utilsProjectStartDate = utils.toDate(utils.toString(projectStartDate));
        Date utilsProjectEndDate = utils.toDate(utils.toString(projectEndDate));
        String dateOutOfRange = project.validEditProjectDateRanges(utilsProjectStartDate, utilsProjectEndDate, sprintList);


        /* Return editProject template with user input */
        if (result.hasErrors() || !dateOutOfRange.equals("")) {
            model.addAttribute("userName", globalUsername);
            model.addAttribute("project", project);
            model.addAttribute("projectStartDate", utils.toString(project.getProjectStartDate()));
            model.addAttribute("projectEndDate", utils.toString(project.getProjectEndDate()));
            model.addAttribute("projectDescription", projectDescription);
            model.addAttribute("invalidDateRange", dateOutOfRange);
            return "editProject";
        }

        /* Set (new) project details to the corresponding project */
        Project newProject = projectService.getProjectById(id);
        newProject.setProjectName(projectName);
        newProject.setProjectStartDate(projectStartDate);
        newProject.setProjectEndDate(projectEndDate);
        newProject.setProjectDescription(projectDescription);
        projectService.saveProject(newProject);

        /* Redirect to details page when done */
        return "redirect:/project/" + id;
    }

}
