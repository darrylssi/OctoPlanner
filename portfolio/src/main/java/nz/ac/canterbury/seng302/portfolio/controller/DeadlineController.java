package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Deadline;
import nz.ac.canterbury.seng302.portfolio.service.DeadlineService;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.service.ProjectService;
import nz.ac.canterbury.seng302.shared.identityprovider.AuthState;
import nz.ac.canterbury.seng302.shared.identityprovider.UserRole;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller to handle requests related to deadlines.
 */
@Controller
public class DeadlineController extends PageController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DeadlineService deadlineService;

    /**
     * Post request to add a deadline to a project.
     * @param principal Authenticated user
     * @param projectId ID of the project the deadline will be added to
     * @param deadlineName Name of the deadline
     * @param deadlineDate Date of the deadline
     * @param deadlineTime Time of the deadline
     * @param deadlineDescription Description of the deadline
     * @return Project details page
     */
    @PostMapping("/project/{project_id}/add-deadline")
    public String postAddDeadline(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @RequestParam(name="deadlineName") String deadlineName,
            @RequestParam(name="deadlineDate") String deadlineDate,
            @RequestParam(name="deadlineTime") String deadlineTime,
            @RequestParam(name="deadlineDescription") String deadlineDescription
    ) {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        Deadline deadline = new Deadline();

        String deadlineDateTime = deadlineDate + " " + deadlineTime;

        // Set details of new deadline object
        deadline.setParentProjectId(parentProject.getId());
        deadline.setDeadlineName(deadlineName);
        deadline.setDeadlineDate(DateUtils.toDateTime(deadlineDateTime));
        deadline.setDeadlineDescription(deadlineDescription);

        deadlineService.saveDeadline(deadline);

        return "redirect:../" + parentProject.getId();

    }

}