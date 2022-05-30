package nz.ac.canterbury.seng302.portfolio.service;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class ValidationService {

    @Autowired
    private SprintService sprintService;
    @Autowired
    private DateUtils utils;

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
        if (sprintsOutsideProject(start, end,
                parentProject.getProjectStartDate(), parentProject.getProjectEndDate())) {
            return "Sprint dates must be within project date range: " +
                    parentProject.getStartDateString() + " - " + parentProject.getEndDateString();
        }

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
        for (Sprint other : sprintList) {
            if (!compareSprintDates(start, end, other) // Sprint dates overlap
                    && (other.getId() != id)){        // Sprint isn't checking against itself
                return "Sprint dates must not overlap with other sprints. Dates are overlapping with "
                        + other.getStartDateString() + " - " + other.getEndDateString();
            }
        }

        // No errors found
        return "";
    }

    /**
     * Validates that a project's start and end dates are valid
     * @param start The project's start date
     * @param end The project's end date
     * @param creation The project's creation date
     * @return An error message if invalid, otherwise returns an empty string
     */
    public String validateProjectDates(Date start, Date end, Date creation) {
        // Checks if the project's start date is after the project's end date
        if (start.after(end)) {
            return "Start date must always be before end date";
        }

        // Getting sprint list containing all the sprints
        List<Sprint> sprintList = sprintService.getAllSprints();
        for (Sprint sprint : sprintList) {
            if (sprintsOutsideProject(sprint.getSprintStartDate(), sprint.getSprintEndDate(), start, end)) {
                return "The sprint with dates: " +
                        sprint.getStartDateString() + " - " + sprint.getEndDateString() +
                        " is outside the project dates";
            }
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(creation);
        startCal.add(Calendar.YEAR, -1);
        Date earliestStart = startCal.getTime();
        if (start.before(earliestStart)) {
            return "Project cannot be set to start more than a year before it was " +
                    "created (cannot start before " + utils.toString(earliestStart) + ")";
        }

        // After all other checks, check whether project length is more than 10 years
        long projectLength = end.getTime() - start.getTime();
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
     * @param projectStart The project's start date to validate
     * @param projectEnd The project's end date to validate
     * @return True if sprint dates are outside project dates, otherwise false
     */
    public boolean sprintsOutsideProject(Date sprintStart, Date sprintEnd, Date projectStart, Date projectEnd) {
        if (sprintStart.before(projectStart)) { return true; }
        else return sprintEnd.after(projectEnd);
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
