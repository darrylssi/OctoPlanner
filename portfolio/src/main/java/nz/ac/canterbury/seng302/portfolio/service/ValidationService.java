package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ValidationService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    public String validateSprintDates(Sprint sprint) {

        // Checks if the sprint's start date is after the sprint's end date
        if (sprint.getSprintStartDate().after(sprint.getSprintEndDate())) {
            return "Start date must always be before end date";
        }

        // Gets the parent project to check that the sprints dates are within the projects dates
        Project parentProject;
        try {
            parentProject = projectService.getProjectById(sprint.getParentProjectId());
        } catch (Exception e) {
            return "Project not found";
        }
        // Checks that the sprint dates are within the project dates
        if (!validateSprintWithinProject(sprint, parentProject)) {
            return "Sprint dates must be within project date range: " +
                    parentProject.getStartDateString() + " - " + parentProject.getEndDateString();
        }

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
        for (Sprint other : sprintList) {
            if (!compareSprintDates(sprint, other)){    // Sprint dates overlap

            }
        }



        return "No errors found"; //TODO find a better way to convey this than checking a string
    }

    public boolean validateSprintWithinProject(Sprint sprint, Project project) {
        if (sprint.getSprintStartDate().before(project.getProjectStartDate())) { return false; }
        else if (sprint.getSprintEndDate().after(project.getProjectEndDate())) { return false; }
        return true;
    }

    public boolean compareSprintDates(Sprint sprint, Sprint other) {
        return false; // Not implemented
    }
}
