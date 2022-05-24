package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SprintService sprintService;

    /**
     * Validates that a sprint's start and end dates are valid
     * @param sprint The sprint to validate
     * @return An error message if invalid, otherwise returns an empty string
     */
    public String validateSprintDates(Sprint sprint) {
        // Checks if the sprint's start date is after the sprint's end date
        // Does this first so that future checks can assume this is true
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
                return "Sprint dates must not overlap with other sprints. Dates are overlapping with "
                        + other.getSprintStartDate() + " - " + other.getSprintEndDate();
            }
        }

        // No errors found
        return "";
    }

    /**
     * Validates that a project's start and end dates are valid
     * @param project The project to validate
     * @return An error message if invalid, otherwise returns an empty string
     */
    public String validateProjectDates(Project project) {
        // Checks if the project's start date is after the project's end date
        if (project.getProjectStartDate().after(project.getProjectEndDate())) {
            return "Start date must always be before end date";
        }

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
        for (Sprint sprint : sprintList) {
            if (!validateSprintWithinProject(sprint, project)) {
                return "The sprint with dates: " +
                        sprint.getStartDateString() + " - " + sprint.getEndDateString() +
                        " is outside the project dates";
            }
        }

        //TODO check that project start date is up to a year before the project was created

        // After all other checks, check whether project length is more than 10 years
        long projectLength = project.getProjectEndDate().getTime() - project.getProjectStartDate().getTime();
        long lengthInYears = projectLength / (1000L*60*60*24*365);
        if (lengthInYears >= 10) {
            //TODO Warn the user according to UPi AC3
        }

        // No errors found
        return "";
    }

    /**
     * Checks that a sprint's date are within it's parent project
     * @param sprint The sprint to validate
     * @param project The parent project of the sprint
     * @return True if sprint dates are within project dates, otherwise false
     */
    public boolean validateSprintWithinProject(Sprint sprint, Project project) {
        if (sprint.getSprintStartDate().before(project.getProjectStartDate())) { return false; }
        else if (sprint.getSprintEndDate().after(project.getProjectEndDate())) { return false; }
        return true;
    }

    /**
     * Checks that two sprint's dates do not overlap
     * @param sprint The sprint being validated
     * @param other The other sprint to compare to
     * @return True if sprint dates do not overlap, otherwise false
     */
    public boolean compareSprintDates(Sprint sprint, Sprint other) {
        // Sprint is before other sprint
        if (sprint.getSprintEndDate().before(other.getSprintStartDate())) { return true; }
        // Sprint is after other sprint
        else if (sprint.getSprintStartDate().after(other.getSprintEndDate())) { return true; }
        // If the sprint is not before or after the other sprint, then the dates must overlap
        return false;
    }


}
