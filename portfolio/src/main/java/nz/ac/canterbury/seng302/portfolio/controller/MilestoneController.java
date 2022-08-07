package nz.ac.canterbury.seng302.portfolio.controller;

import nz.ac.canterbury.seng302.portfolio.model.Milestone;
import nz.ac.canterbury.seng302.portfolio.service.MilestoneService;
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
import org.springframework.web.server.ResponseStatusException;

@Controller
public class MilestoneController extends PageController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MilestoneService milestoneService;

    @PostMapping("/project/{project_id}/add-milestone")
    public String postAddEvent(
            @AuthenticationPrincipal AuthState principal,
            @PathVariable("project_id") int projectId,
            @RequestParam(name="milestoneName") String milestoneName,
            @RequestParam(name="milestoneDate") String milestoneDate,
            @RequestParam(name="milestoneDescription") String milestoneDescription
    ) throws ResponseStatusException {
        requiresRoleOfAtLeast(UserRole.TEACHER, principal);

        // Getting parent project object by path id
        Project parentProject = projectService.getProjectById(projectId);

        Milestone milestone = new Milestone();

        // Set details of new milestone object
        milestone.setParentProjectId(parentProject.getId());
        milestone.setMilestoneName(milestoneName);
        milestone.setMilestoneDate(DateUtils.toDate(milestoneDate));
        milestone.setMilestoneDescription(milestoneDescription);

        milestoneService.saveMilestone(milestone);

        return "redirect:../project/" + parentProject.getId();

    }

}
