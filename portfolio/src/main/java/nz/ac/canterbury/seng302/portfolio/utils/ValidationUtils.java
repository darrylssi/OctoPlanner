package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Project;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;
import nz.ac.canterbury.seng302.portfolio.model.ValidationError;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility class handles validation for the dates of objects in the portfolio.
 * It has methods to compare dates and date ranges against each other to ensure that
 * they are valid.
 */
@Component
public class ValidationUtils {

    private ValidationUtils() {}

    public static final String DATES_IN_WRONG_ORDER_MESSAGE = "Start date must always be before end date";

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
            error.addErrorMessage(DATES_IN_WRONG_ORDER_MESSAGE);
        }

        // Checks that the sprint dates are within the project dates
        if (datesOutsideProject(start, end,
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
            error.addErrorMessage(DATES_IN_WRONG_ORDER_MESSAGE);
        }

        // Checking against sprint dates
        for (Sprint sprint : sprintList) {
            if (datesOutsideProject(sprint.getSprintStartDate(), sprint.getSprintEndDate(), start, end)) {
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
     * Validates that an event's start and end dates are valid. The checks are:
     * <ul>
     *     <li>Event's start date is before the end date</li>
     *     <li>Event dates are within project dates</li>
     * </ul>
     * @param start The event's start date
     * @param end The event's end date
     * @param parentProject The project that the event belongs to
     * @return A ValidationError with a boolean error flag and a list of error messages
     */
    public static ValidationError validateEventDates(Date start, Date end, Project parentProject) {
        // Initial error flag = false (no errors yet)
        ValidationError error = new ValidationError(false);

        // Checks if the event's start date is after the project's end date
        if (start.after(end)) {
            error.setErrorFlag(true);
            error.addErrorMessage(DATES_IN_WRONG_ORDER_MESSAGE);
        }

        // Checks that the event's dates are within the project dates
        if (datesOutsideProject(start, end,
                parentProject.getProjectStartDate(), parentProject.getProjectEndDate())) {
            error.setErrorFlag(true);
            error.addErrorMessage("Event dates must be within project date range: " +
                    parentProject.getStartDateString() + " - " + parentProject.getEndDateString());
        }

        return error;
    }

    /**
     * Checks whether a given start and end date are within a project's dates (or any two given dates)
     * @param startDate The start date to validate
     * @param endDate The end date to validate
     * @param projectStart The project's start date to test against
     * @param projectEnd The project's end date to test against
     * @return True if given dates are outside the project dates, otherwise false
     */
    public static boolean datesOutsideProject(Date startDate, Date endDate, Date projectStart, Date projectEnd) {
        if (startDate.before(projectStart)) { return true; }
        else return endDate.after(projectEnd);
    }

    /**
     * Checks whether a given date is within a project's dates (or any two given dates)
     * @param date The date to validate
     * @param projectStart The project's start date to test against
     * @param projectEnd The project's end date to test against
     * @return True if the given date is outside the project dates, otherwise false
     */
    public static boolean dateOutsideProject(Date date, Date projectStart, Date projectEnd) {
        // TODO use this for milestones & deadlines because they only have one date to check
        if (date.before(projectStart)) { return true; }
        else return date.after(projectEnd);
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

    /**
     * Checks whether the name contains only valid characters
     * @param name The sprint name to be tested
     * @return A ValidationError with a boolean error flag and a list of error messages
     */
    public static ValidationError validateName(String name) {
        // Initial error flag = false (no errors yet)
        ValidationError error = new ValidationError(false);

        if (name == null) {
            error.setErrorFlag(true);
            error.addErrorMessage("Must enter a sprint name");
            return error;
        }

        /* string can only have alphanumeric and _ , . - ( ) symbols */
        String regex = "^([a-zA-Z0-9\\s\\-\\.\\_]){2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        if(!matcher.matches()) {
            error.setErrorFlag(true);
            error.addErrorMessage("Name can only have alphanumeric and . - _ characters");
        }

        return error;
    }

}
