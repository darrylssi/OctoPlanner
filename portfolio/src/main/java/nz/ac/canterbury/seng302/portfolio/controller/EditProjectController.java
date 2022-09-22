package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.service.SprintService;
import nz.ac.canterbury.seng302.portfolio.utils.ValidationUtils;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

/**
 * Controller for the edit project details page
 */
@Controller
public class EditProjectController extends PageController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    /**
     * A post request for editing a project with a given ID.
     * @param id ID of the project to be edited
     * @param projectName (New) name of the project
     * @param projectStartDate (New) project start date
     * @param projectEndDate (New) project end date
     * @param projectDescription (New) project description
     * @return Details page
     * @throws ResponseStatusException If the date cannot be parsed
     */
    @PostMapping("project/{id}/edit-project")
    public String postEditProject(
            @AuthenticationPrincipal AuthState principal,
            @Valid Project project,
            BindingResult result,
            @PathVariable("id") int id,
            @RequestParam(value="projectName") String projectName,
            @RequestParam(value="projectStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectStartDate,
            @RequestParam(value="projectEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectEndDate,
            @RequestParam(value="projectDescription") String projectDescription,
            Model model
    ) throws ResponseStatusException {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        Project newProject = projectService.getProjectById(id);

        List<Sprint> sprintList = sprintService.getSprintsInProject(id);
        ValidationError dateOutOfRange = ValidationUtils.validateProjectDates(projectStartDate, projectEndDate,
                newProject.getProjectCreationDate(), sprintList);

        ValidationError invalidName = ValidationUtils.validateName(projectName);

        /* Return editProject template with user input */
        if (result.hasErrors() || dateOutOfRange.isError() || invalidName.isError()) {
            model.addAttribute("project", project);
            model.addAttribute("projectStartDate", DateUtils.toString(project.getProjectStartDate()));
            model.addAttribute("projectEndDate", DateUtils.toString(project.getProjectEndDate()));
            model.addAttribute("projectDescription", projectDescription);
            model.addAttribute("invalidDateRange", dateOutOfRange.getErrorMessages());
            model.addAttribute("invalidName", invalidName.getErrorMessages());
            return "redirect:/project/" + id;
        }

        /* Set (new) project details to the corresponding project */
        newProject.setProjectName(projectName);
        newProject.setProjectStartDate(projectStartDate);
        newProject.setProjectEndDate(projectEndDate);
        newProject.setProjectDescription(projectDescription);
        projectService.saveProject(newProject);

        /* Redirect to the details' page when done */
        return "redirect:/project/" + id;
    }

}
