package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

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

    @Column (nullable = false)
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
     * @param projectName {String}The name of the project
     * @param projectDescription {String} The project description
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
     * @param projectName {String} The name of the project
     * @param projectDescription {String} The project description
     * @param projectStartDate {String} project start date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     *                         Must be before the end date.
     * @param projectEndDate {String} project end date in format dd/MON/yyyy, where MON is Jan, Feb, Mar, etc.
     *                       Must be after the start date.
     */
    public Project(String projectName, String projectDescription, String projectStartDate, String projectEndDate) {
        this.projectName = projectName;
        this.projectDescription = projectDescription;
        this.projectStartDate = DateUtils.toDate(projectStartDate);
        this.projectEndDate = DateUtils.toDate(projectEndDate);
        this.projectCreationDate = new Date();
    }

    /**
     * Returns a string listing the attributes of the project in the form "Project[x, x, x, ...]".
     */
    @Override
    public String toString() {
        return String.format(
                "Project[id=%d, projectName='%s', projectStartDate='%s', projectEndDate='%s', projectDescription='%s']",
                id, projectName, projectStartDate, projectEndDate, projectDescription);
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
        return DateUtils.toDisplayString(this.projectStartDate);
    }

    public void setStartDateString(String date) {
        this.projectStartDate = DateUtils.toDate(date);
    }

    public String getEndDateString() {
        return DateUtils.toDisplayString(this.projectEndDate);
    }

    public void setEndDateString(String date) {
        this.projectEndDate = DateUtils.toDate(date);
    }

    public void setProjectCreationDate(Date projectCreationDate) {
        this.projectCreationDate = projectCreationDate;
    }

    public Date getProjectCreationDate() {
        return projectCreationDate;
    }
}
