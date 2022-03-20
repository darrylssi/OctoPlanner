package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Represents a project object.
 */
@Entity // this is an entity, assumed to be in a table called Project
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotBlank(message = "Project name cannot be empty")
    /** The name of the project. Just a string chosen by the user. */
    private String projectName;
    @NotNull
    /** The description of the project. @NotNull means that it may be empty, but not null. */
    private String projectDescription;
    @NotNull
    /** The project start date. Must be before the end date. */
    private Date projectStartDate;
    @NotNull
    /** The project end date. Must be before the start date. */
    private Date projectEndDate;

    protected Project() {}

    /**
     * Constructor taking dates as Date objects.
     * @param projectName
     * @param projectDescription
     * @param projectStartDate start date, as a Date object
     * @param projectEndDate end date, as a Date object
     */
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
    static Date stringToDate(String dateString) {
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

    public int getId(){
        return  id;
    }

    public String getName() {
        return projectName;
    }

    public void setName(String newName) {
        this.projectName = newName;
    }

    public String getDescription(){
        return projectDescription;
    }

    public void setDescription(String newDescription) {
        this.projectDescription = newDescription;
    }

    /* Dates have string get/set methods to interact with view */

    /** Returns the start date as a date object. */
    //date
    public Date getStartDate() {
        return projectStartDate;
    }

    /** Returns the start date as a string in format dd/MON/yyyy. */
    public String getStartDateString() {
        return Project.dateToString(this.projectStartDate);
    }

    /** Sets the start date with a date object. */
    public void setStartDate(Date newStartDate) {
        this.projectStartDate = newStartDate;
    }

    /** Sets the start date with a string in format dd/MON/yyyy. */
    public void setStartDateString(String date) { this.setStartDate(Project.stringToDate(date)); }

    /** Returns the end date as a date object. */
    public Date getEndDate() {
        return projectEndDate;
    }

    /** Returns the end date as a string in format dd/MON/yyyy. */
    public String getEndDateString() {
        return Project.dateToString(this.projectEndDate);
    }

    /** Sets the end date with a date object. */
    public void setEndDate(Date newEndDate) { this.projectEndDate = newEndDate; }

    /** Sets the end date with a string in format dd/MON/yyyy. */
    public void setEndDateString(String date) {
        this.setEndDate(Project.stringToDate(date));
    }
}
