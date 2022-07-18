package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * This service class handles validation for the dates of objects in the portfolio.
 * It has methods to compare dates and date ranges against each other to ensure that
 * they are valid.
 */
@Service
public class ValidationUtils {

    private ValidationUtils() {}

    /**
     * Validates that a sprint's start and end dates are valid. The checks are:
     * <ul>
     *     <li>Sprint start date is before end date</li>
     *     <li>Sprint dates are within project dates</li>
     *     <li>Sprint dates do not overlap with other sprints</li>
     * </ul>
     * @param id The id of the sprint to be validated, as an integer
     * @param start The sprint's start date to validate
     * @param end The sprint's end date to validate
     * @param parentProject The project that the sprint belongs to
     * @param sprintList A list of sprints in the same project to be compared to
     * @return A ValidationError with a boolean error flag and a list of error messages
     */
    public static ValidationError validateSprintDates(int id, Date start, Date end,
                                               Project parentProject, List<Sprint> sprintList) {
        // Initial error flag = false (no errors yet)
        ValidationError error = new ValidationError(false);

        // Checks if the sprint's start date is after the sprint's end date
        // Does this first so that future checks can assume this is true
        if (start.after(end)) {
            error.setErrorFlag(true);
            error.addErrorMessage("Start date must always be before end date");
        }

        // Checks that the sprint dates are within the project dates
        if (sprintsOutsideProject(start, end,
                parentProject.getProjectStartDate(), parentProject.getProjectEndDate())) {
            error.setErrorFlag(true);
            error.addErrorMessage("Sprint dates must be within project date range: " +
                    parentProject.getStartDateString() + " - " + parentProject.getEndDateString());
        }

        // Checking against other sprint dates
        for (Sprint other : sprintList) {
            if (!sprintDatesOverlap(start, end, other) // Sprint dates overlap
                    && (other.getId() != id)){        // Sprint isn't checking against itself
                error.setErrorFlag(true);
                error.addErrorMessage("Sprint dates must not overlap with other sprints. Dates are overlapping with "
                        + other.getStartDateString() + " - " + other.getEndDateString());
            }
        }

        return error;
    }

    /**
     * Validates that a project's start and end dates are valid. The checks are:
     * <ul>
     *     <li>Project start date is before the end date</li>
     *     <li>Project dates contain all sprints</li>
     *     <li>Project start date is not more than 1 year before creation date</li>
     * </ul>
     * @param start The project's start date
     * @param end The project's end date
     * @param creation The project's creation date
     * @param sprintList A list of sprints in the same project to be compared to
     * @return A ValidationError with a boolean error flag and a list of error messages
     */
    public static ValidationError validateProjectDates(Date start, Date end, Date creation, List<Sprint> sprintList) {
        // Initial error flag = false (no errors yet)
        ValidationError error = new ValidationError(false);

        // Checks if the project's start date is after the project's end date
        if (start.after(end)) {
            error.setErrorFlag(true);
            error.addErrorMessage("Start date must always be before end date");
        }

        // Checking against sprint dates
        for (Sprint sprint : sprintList) {
            if (sprintsOutsideProject(sprint.getSprintStartDate(), sprint.getSprintEndDate(), start, end)) {
                error.setErrorFlag(true);
                error.addErrorMessage(sprint.getSprintLabel() + ": " +
                        sprint.getStartDateString() + " - " + sprint.getEndDateString() +
                        " is outside the project dates");
            }
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(creation);
        startCal.add(Calendar.YEAR, -1);
        Date earliestStart = startCal.getTime();
        if (start.before(earliestStart)) {
            error.setErrorFlag(true);
            error.addErrorMessage("Project cannot be set to start more than a year before it was " +
                    "created (cannot start before " + DateUtils.toDisplayString(earliestStart) + ")");
        }

        return error;
    }

    /**
     * Checks whether a sprint's dates are within a project's dates
     * @param sprintStart The sprint's start date to validate
     * @param sprintEnd The sprint's end date to validate
     * @param projectStart The project's start date to validate
     * @param projectEnd The project's end date to validate
     * @return True if sprint dates are outside project dates, otherwise false
     */
    public static boolean sprintsOutsideProject(Date sprintStart, Date sprintEnd, Date projectStart, Date projectEnd) {
        if (sprintStart.before(projectStart)) { return true; }
        else return sprintEnd.after(projectEnd);
    }

    /**
     * Checks that two sprints' dates do not overlap
     * @param start The sprint's start date to validate
     * @param end The sprint's end date to validate
     * @param other The other sprint to compare to
     * @return True if sprint dates do not overlap, otherwise false
     */
    public static boolean sprintDatesOverlap(Date start, Date end, Sprint other) {
        // If the sprint is not before or after the other sprint, then the dates must overlap
            // Sprint is before other sprint
        if (end.before(other.getSprintStartDate())) { return true; }
            // Sprint is after other sprint
        else return start.after(other.getSprintEndDate());
    }

}
