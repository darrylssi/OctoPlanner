package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.Calendar;
import java.text.SimpleDateFormat;

@Entity // this is an entity, assumed to be in a table called Project
@Table (name = "Project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column (nullable = false)
    @Size (max = 50, message = "Project name cannot be more than 50 characters")
    @NotBlank (message = "Project name is required")
    private String projectName;

    @Size (max = 200, message = "Description cannot be more than 200 characters.")
    private String projectDescription;

    @Column (nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date projectStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date projectEndDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date projectCreationDate;

    protected Project() {
        this.projectCreationDate = new Date();
    }

    /**
     * Constructor taking dates as Date objects directly
     * @param projectName
     * @param projectDescription
     * @param projectStartDate {Date} project start date. Must be before the end date.
     * @param projectEndDate {Date} project end date. Must be after the start date.
     */
    public Project(String projectName, String projectDescription, Date projectStartDate, Date projectEndDate) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
        this.projectCreationDate = new Date();
    }

    /**
     * Constructor taking dates as String objects. Date strings should be of the format dd/MON/yyyy, where MON is the
     * first three letters of the name of the month, e.g. Jan, Feb, Mar, Apr, etc. Day and year are numbers.
     * @param projectStartDate project start date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     * @param projectName
     * @param projectDescription
     * @param projectStartDate {String} project start date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     *                         Must be before the end date.
     * @param projectEndDate {String} project end date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     *                       Must be after the start date.
     */
    public Project(String projectName, String projectDescription, String projectStartDate, String projectEndDate) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = Project.stringToDate(projectStartDate);
        this.projectEndDate = Project.stringToDate(projectEndDate);
        this.projectCreationDate = new Date();
    }

    @Override
    public String toString() {
        return String.format(
                "Project[id=%d, projectName='%s', projectStartDate='%s', projectEndDate='%s', projectDescription='%s']",
                id, projectName, projectStartDate, projectEndDate, projectDescription);
    }

    /**
     * Gets the date form of the given date string
     *
     * @param dateString the string to read as a date in format 01/Jan/2000
     * @return the given date, as a date object
     */
    public static Date stringToDate(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MMM/yyyy").parse(dateString);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
        }
        return date;
    }

    /**
     * Gets the string form of the given date in
     *
     * @param date the date to convert
     * @return the given date, as a string in format 01/Jan/2000
     */
    static String dateToString(Date date) {
        return new SimpleDateFormat("dd/MMM/yyyy").format(date);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public Date getProjectStartDate() {
        return projectStartDate;
    }

    public void setProjectStartDate(Date projectStartDate) {
        this.projectStartDate = projectStartDate;
    }

    public Date getProjectEndDate() {
        return projectEndDate;
    }

    public void setProjectEndDate(Date projectEndDate) {
        this.projectEndDate = projectEndDate;
    }

    /* Dates have string get/set methods to interact with view */

    public String getStartDateString() {
        return Project.dateToString(this.projectStartDate);
    }

    public void setStartDateString(String date) {
        this.projectStartDate = Project.stringToDate(date);
    }

    public String getEndDateString() {
        return Project.dateToString(this.projectEndDate);
    }

    public void setEndDateString(String date) {
        this.projectEndDate = Project.stringToDate(date);
    }

    public Date getProjectCreationDate() {
        return projectCreationDate;
    }

    /**
     * Validates the given project start and end dates to check that they match the following criteria:
     * <ul>
     *  <li>The start date is not more than a year before the date the project was created</li>
     *  <li>The project dates still fit around the sprints (i.e. no sprint or part of a sprint would
     *      lie outside these project dates)</li>
     * </ul>
     *
     * @param projectStartDate {Date} the project start date being checked
     * @param projectEndDate {Date} the project end date being checked
     * @param projectCreationDate {Date} the date of creation of the project being edited
     * @param sprintList {List<Sprint>} the sprints within the project being edited
     */
    public String validEditProjectDateRanges(Date projectStartDate, Date projectEndDate,
            Date projectCreationDate, List<Sprint> sprintList) throws ParseException {
        String invalidDateRange = "";
        DateUtils utils = new DateUtils();

        /* Calculate earliest possible start date and check that start is not before this */
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(projectCreationDate);
        startCal.add(Calendar.YEAR, -1);
        Date earliestStart = startCal.getTime();
        if (projectStartDate.before(earliestStart)) {
            invalidDateRange += "Project cannot be set to start more than a year before it was created (cannot start before " + utils.toString(earliestStart) + ")\n";
        }

        /* Check that the project dates still fit around the sprints */
        if (!sprintList.isEmpty()) {
            for (Sprint eachSprint: sprintList) {
                Date utilsSprintStartDate = utils.toDate(utils.toString(eachSprint.getSprintStartDate()));
                Date utilsSprintEndDate = utils.toDate(utils.toString(eachSprint.getSprintEndDate()));
                if ((projectStartDate.after(utilsSprintEndDate) || projectEndDate.before(utilsSprintStartDate)) ) {
                    invalidDateRange += "Project dates must not be before or after the sprint dates " + utils.toString(eachSprint.getSprintStartDate()) + " - " +
                            utils.toString(eachSprint.getSprintEndDate());
                    break;
                } else if (((projectStartDate.after(utilsSprintStartDate)) || (projectEndDate.before(utilsSprintEndDate))) ||
                        (projectStartDate.after(utilsSprintStartDate) && projectStartDate.before(utilsSprintEndDate)) ||
                        (projectEndDate.after(utilsSprintStartDate) && projectEndDate.before(utilsSprintEndDate))) {
                    invalidDateRange += "Dates must not overlap with other sprints & it is overlapping with " + utils.toString(eachSprint.getSprintStartDate()) + " - " +
                            utils.toString(eachSprint.getSprintEndDate());
                    break;
                }
            }}
        return invalidDateRange;

    }

}
