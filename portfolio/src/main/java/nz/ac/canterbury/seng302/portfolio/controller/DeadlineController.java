package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DeadlineController extends PageController {

    @Autowired
    private ProjectService projectService;              // Initializes the ProjectService object
    @Autowired
    private DeadlineService deadlineService;            // Initializes the DeadlineService object

    @GetMapping("/project/{project_id}/add-deadline")
    public String getAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            Model model
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting project object by project id
        Project parentProject = projectService.getProjectById(projectId);

        return "redirect:../project/" + parentProject.getId();
    }

    @PostMapping("/project/{project_id}/add-deadline")
    public String postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @RequestParam(name="deadlineName") String deadlineName,
            @RequestParam(name="deadlineDate") String deadlineDate,
            @RequestParam(name="deadlineDescription") String deadlineDescription,
            BindingResult result
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting project object by project id
        Project parentProject = projectService.getProjectById(projectId);

        Deadline deadline = new Deadline();

        // TODO do validation

        // Adding the new sprint object
        deadline.setParentProjectId(parentProject.getId());
        deadline.setDeadlineName(deadlineName);
        deadline.setDeadlineDate(DateUtils.toDate(deadlineDate));
        deadline.setDeadlineDescription(deadlineDescription);

        deadlineService.saveDeadline(deadline);

        return "redirect:../project/" + parentProject.getId();
    }
}
