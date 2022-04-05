package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * This service class handles the allocation of labels to sprint objects.
 * It has methods to re-allocate sprint labels for one or all projects,
 * and to get the next available sprint label for a given project.
 *
 * As this is a service, instantiate it with an @Autowired annotation as you would any other service.
 */
@Service
public class SprintLabelService {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private SprintService sprintService;

    // the thing that goes at the start of every sprint label before the number
    public static final String SPRINT_LABEL_BASE = "Sprint ";
    // labels start at 0 if this is 0, so to start at 1, set it to 1.
    public static final int SPRINT_LABEL_OFFSET = 1;

    /**
     * Re-allocates all sprint labels across all projects.
     */
    public void refreshAllSprintLabels() {
        List<Project> allProjects = projectService.getAllProjects();
        for (Project project : allProjects) {
            refreshProjectSprintLabels(project.getId());
        }
    }

    /**
     * Re-allocates all sprint labels for a specified project.
     * @param projectId the id of the project to refresh
     */
    public void refreshProjectSprintLabels(int projectId) {
        List<Sprint> projectSprints = sprintService.getSprintsOfProjectById(projectId);
        projectSprints.sort(Comparator.comparing(Sprint::getSprintStartDate));
        for (int i = 0; i < projectSprints.size(); i++) {
            Sprint sprint = projectSprints.get(i);
            sprint.setSprintLabel(SPRINT_LABEL_BASE + (i + SPRINT_LABEL_OFFSET));
            sprintService.saveSprint(sprint);
        }
    }

    /**
     * Re-allocates all sprint labels for a specified project.
     * @param project the project to refresh
     */
    public void refreshProjectSprintLabels(Project project) {
        refreshProjectSprintLabels(project.getId());
    }

    /**
     * Returns the next available sprint label for the given project id.
     * @param projectId the id of the project to get the next label for
     * @return a label String of the form (SPRINT_LABEL_BASE + [x]), where [x] is a number >= 1
     */
    public String nextLabel(int projectId) {
        return SPRINT_LABEL_BASE + (sprintService.getSprintsOfProjectById(projectId).size() + SPRINT_LABEL_OFFSET);
    }

    /**
     * Returns the next available sprint label for the given project.
     * @param project the project to get the next label for
     * @return a label String of the form (SPRINT_LABEL_BASE + [x]), where [x] is a number >= 1
     */
    public String nextLabel(Project project) {
        return nextLabel(project.getId());
    }
}
