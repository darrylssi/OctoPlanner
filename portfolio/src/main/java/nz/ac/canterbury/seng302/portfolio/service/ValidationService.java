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
    private SprintService sprintService;

    /**
     * Validates that a sprint's start and end dates are valid
     * @param start The sprint's start date to validate
     * @param end The sprint's end date to validate
     * @return An error message if invalid, otherwise returns an empty string
     */
    public String validateSprintDates(int id, Date start, Date end, Project parentProject) {
        // Checks if the sprint's start date is after the sprint's end date
        // Does this first so that future checks can assume this is true
        if (start.after(end)) {
            return "Start date must always be before end date";
        }

        // Checks that the sprint dates are within the project dates
        if (sprintsOutsideProject(start, end, parentProject)) {
            return "Sprint dates must be within project date range: " +
                    parentProject.getStartDateString() + " - " + parentProject.getEndDateString();
        }

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
        for (Sprint other : sprintList) {
            if (!compareSprintDates(start, end, other) // Sprint dates overlap
                    && !(other.getId() == id)){        // Sprint isn't checking against itself
                return "Sprint dates must not overlap with other sprints. Dates are overlapping with "
                        + other.getStartDateString() + " - " + other.getEndDateString();
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
            if (sprintsOutsideProject(sprint.getSprintStartDate(), sprint.getSprintEndDate(), project)) {
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
     * Checks whether a sprint's dates are within a projects dates
     * @param sprintStart The sprint's start date to validate
     * @param sprintEnd The sprint's end date to validate
     * @param project The parent project of the sprint
     * @return True if sprint dates are outside project dates, otherwise false
     */
    public boolean sprintsOutsideProject(Date sprintStart, Date sprintEnd, Project project) {
        if (sprintStart.before(project.getProjectStartDate())) { return true; }
        else return sprintEnd.after(project.getProjectEndDate());
    }

    /**
     * Checks that two sprint's dates do not overlap
     * @param start The sprint's start date to validate
     * @param end The sprint's end date to validate
     * @param other The other sprint to compare to
     * @return True if sprint dates do not overlap, otherwise false
     */
    public boolean compareSprintDates(Date start, Date end, Sprint other) {
        // If the sprint is not before or after the other sprint, then the dates must overlap
            // Sprint is before other sprint
        if (end.before(other.getSprintStartDate())) { return true; }
            // Sprint is after other sprint
        else return start.after(other.getSprintEndDate());
    }

}
