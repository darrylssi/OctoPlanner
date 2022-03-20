package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.DateUtils;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * Controller for the edit project details page
 */
@Controller
public class EditProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DateUtils utils;

    @GetMapping("/edit-project/{id}")
    public String projectForm(@PathVariable("id") int id, Model model) throws Exception {

        /* Add project details to the model */
        Project project = projectService.getProjectById(id);
        model.addAttribute("projectId", id);
        model.addAttribute("projectName", project.getName());
        model.addAttribute("projectStartDate", utils.toString(project.getStartDate()));
        model.addAttribute("projectEndDate", utils.toString(project.getEndDate()));
        model.addAttribute("projectDescription", project.getDescription());

        /* Return the name of the Thymeleaf template */
        return "editProject";
    }

    @PostMapping("/edit-project/{id}")
    public String projectSave(
            @PathVariable("id") int id,
            @RequestParam(value="projectName") String projectName,
            @RequestParam(value="projectStartDate") String projectStartDate,
            @RequestParam(value="projectEndDate") String projectEndDate,
            @RequestParam(value="projectDescription") String projectDescription
    ) throws Exception {
        Project project = projectService.getProjectById(id);
        project.setName(projectName);
        project.setStartDate(utils.toDate(projectStartDate));
        project.setEndDate(utils.toDate(projectEndDate));
        project.setDescription(projectDescription);
        projectService.saveProject(project);
        return "redirect:/details";
    }

}
