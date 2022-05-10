package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@Entity // this is an entity, assumed to be in a table called Project
@Table (name = "Project")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column (nullable = false)
    @Size (min=2, max = 32, message = "The character length must be between 2 and 32")
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

    protected Project() {}

    public Project(String projectName, String projectDescription, Date projectStartDate, Date projectEndDate) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = projectStartDate;
        this.projectEndDate = projectEndDate;
    }

    /**
     * Constructor taking dates as String objects. Date strings should be of the format dd/MON/yyyy, where MON is the
     * first three letters of the name of the month, e.g. Jan, Feb, Mar, Apr, etc. Day and year are numbers.
     * @param projectName
     * @param projectDescription
     * @param projectStartDate project start date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     *                         Must be before the end date.
     * @param projectEndDate project end date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     *                       Must be after the start date.
     */
    public Project(String projectName, String projectDescription, String projectStartDate, String projectEndDate) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = Project.stringToDate(projectStartDate);
        this.projectEndDate = Project.stringToDate(projectEndDate);
    }

    @Override
    /**
     * Returns a string listing the attributes of the project in the form "Project[x, x, x, ...]".
     */
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

    /* Getters/Setters */

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

    public String validEditProjectDateRanges(Date projectStartDate, Date projectEndDate, List<Sprint> sprintList) throws ParseException {
        String invalidDateRange = "";
        DateUtils utils = new DateUtils();

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
