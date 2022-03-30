package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.ErrorType;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.util.List;
import javax.validation.Valid;
import java.util.Date;


/**
 * Controller for the edit project details page
 */
@Controller
public class EditProjectController extends PageController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DateUtils utils;

    /**
     * Show the edit-project page.
     * @param id ID of the project to be edited
     * @param model Parameters sent to thymeleaf template to be rendered into HTML
     * @return Edit-project page
     */
    @GetMapping("/edit-project/{id}")
    public String projectForm(
            @PathVariable("id") int id,
            @AuthenticationPrincipal AuthState principal,
            Model model
    ) {
        /* Ensure that the user is at least a teacher */
        List<String> roles = getUserRole(principal);
        if (!(roles.contains("teacher") || roles.contains("course_administrator"))) {
            configureError(model, ErrorType.ACCESS_DENIED, "/edit-project/" + Integer.toString(id));
            return "error";
        }

        /* Add project details to the model */
        try {
            Project project = projectService.getProjectById(id);
            model.addAttribute("id", id);
            model.addAttribute("project", project);
            model.addAttribute("projectStartDate", utils.toString(project.getProjectStartDate()));
            model.addAttribute("projectEndDate", utils.toString(project.getProjectEndDate()));
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
            @Valid Project project,
            BindingResult result,
            @PathVariable("id") int id,
            @AuthenticationPrincipal AuthState principal,
            @RequestParam(value="projectName") String projectName,
            @RequestParam(value="projectStartDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectStartDate,
            @RequestParam(value="projectEndDate") @DateTimeFormat(pattern = "yyyy-MM-dd") Date projectEndDate,
            @RequestParam(value="projectDescription") String projectDescription,
            Model model
    ) throws Exception {
        /* Ensure that the user is at least a teacher */
        List<String> roles = getUserRole(principal);
        if (!(roles.contains("teacher") || roles.contains("course_administrator"))) {
            configureError(model, ErrorType.ACCESS_DENIED, "/edit-project/" + Integer.toString(id));
            return "error";
        }

        /* Return editProject template with user input */
        if (result.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("projectStartDate", utils.toString(project.getProjectStartDate()));
            model.addAttribute("projectEndDate", utils.toString(project.getProjectEndDate()));
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
