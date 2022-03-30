package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

/**
 * Represents a sprint object. Sprints must have a parent project object that they are a part of.
 * Sprint objects are stored in a table called Sprint, as it is an @Entity.
 */
@Entity
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    /** The id of this sprint. This id should be unique between all sprints, regardless of which project they
     * belong to. The id starts at 0 and should automatically increment.
     */
    private int id;

    @Column
    private int parentProjectId;

    @Column
    @Size(min=2, max=32, message="The character length must in range 2 and 32.") //TODO testing values
    private String sprintName;

    @Column(nullable = false)
    private String sprintLabel;

    @Column(nullable = true)
    @Size(max=200, message="The character length must not exceed 200.") //TODO testing values
    private String sprintDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintEndDate;


    public Sprint() {}

    /**
     * A constructor which set the given user data to the specified variables
     * @param parentProjectId Gets the project id
     * @param sprintName Gets the sprint name given by user
     * @param sprintDescription Gets the sprint description given by the user
     * @param sprintStartDate Gets the sprint start date as a Date object
     * @param sprintEndDate Gets the sprint end date as a Date object
     */
    public Sprint(int parentProjectId, String sprintName,  String sprintDescription, Date sprintStartDate, Date sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = sprintStartDate;
        this.sprintEndDate = sprintEndDate;
    }

    /**
     * A constructor which set the given user data to the specified variables
     * @param parentProjectId Gets the project id
     * @param sprintName Gets the sprint name given by user
     * @param sprintDescription Gets the sprint description given by the user
     * @param sprintStartDate Gets the sprint start date as a string
     * @param sprintEndDate Gets the sprint end date as a string
     */
    public Sprint(int parentProjectId, String sprintName,  String sprintDescription, String sprintStartDate, String sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = Project.stringToDate(sprintStartDate);
        this.sprintEndDate = Project.stringToDate(sprintEndDate);
    }


    /**
     * Returns the Sprint object as a string
     * @return
     */
    @Override
    /**
     * Returns a string listing the attributes of the sprint in the form "Sprint[x, x, x]".
     */
    public String toString() {
        return String.format(
                "Sprint[id=%d, parentProjectId='%d', sprintName='%s', sprintLabel='%s', sprintStartDate='%s', sprintEndDate='%s', sprintDescription='%s']",
                id, parentProjectId, sprintName, sprintLabel, sprintStartDate, sprintEndDate, sprintDescription);
    }


    /**
     * Gets the sprint id
     * @return sprint's id
     */
    public int getId(){
        return  id;
    }

    /**
     * Sets the parent project id
     * @param id Gets the Project's id
     */
    public void setParentProjectId(int id) {
        this.parentProjectId = id;
    }

    /**
     * Gets the parent project id
     * @return project's id
     */
    public int getParentProjectId() {
        return parentProjectId;
    }

    /**
     * Gets the sprint name
     * @return sprint's name
     */
    public String getSprintName() {
        return sprintName;
    }

    /**
     * Sets the sprint name
     * @param newName Gets the sprint name given by user
     */
    public void setSprintName(String newName) {
        this.sprintName = newName;
    }

    /**
     * Gets the sprint label
     * @return sprint's label
     */
    public String getSprintLabel() {
        return sprintLabel;
    }

    /**
     * Gets the sprint description
     * @return sprint's description
     */
    public String getSprintDescription(){
        return sprintDescription;
    }

    /**
     * Sets the sprint Description
     * @param newDescription Gets the sprint description given by the user
     */
    public void setSprintDescription(String newDescription) {
        this.sprintDescription = newDescription;
    }

    /**
     * Gets the sprint start date
     * @return sprint's start date
     */
    public Date getSprintStartDate() {
        return sprintStartDate;
    }

    /**
     * Gets the string format of the sprint start date
     * @return sprint's start date as a string
     */
    public String getStartDateString() {
        return Project.dateToString(this.sprintStartDate);
    }

    /**
     * Sets the sprint start date
     * @param newStartDate Gets the sprint start date given by the user
     */
    public void setStartDate(Date newStartDate) {
        this.sprintStartDate = newStartDate;
    }

    /**
     * Sets the sprint start date in a string format
     * @param date Gets the sprint start date as a string given by the user
     */
    public void setStartDateString(String date) {
        this.sprintStartDate = Project.stringToDate(date);
    }

    /**
     * Gets the sprint end date
     * @return sprint's end date
     */
    public Date getSprintEndDate() {
        return sprintEndDate;
    }

    /**
     * Gets the string format of a spring end date
     * @return sprint's end date as a string
     */
    public String getEndDateString() {
        return Project.dateToString(this.sprintEndDate);
    }

    /**
     * Sets the sprint end date
     * @param newEndDate Gets the sprint end date given by user
     */
    public void setEndDate(Date newEndDate) {
        this.sprintEndDate = newEndDate;
    }

    /**
     * Sets the sprint end date as a string format
     * @param date Gets the sprint start date as a string given by the user
     */
    public void setEndDateString(String date) {
        this.sprintEndDate = Project.stringToDate(date);
    }

    /**
     * Sets the sprint label
     * @param newLabel Gets the sprint label given by the user
     */
    public void setSprintLabel(String newLabel) { this.sprintLabel = newLabel; }


    /**
     * This function check for the validation for add/edit sprints page. Here, first it checks if the start date is after
     * the end date or end date is before the start date. Next, it checks if the sprint dates are within the project
     * dates. Lastly, it checks if the sprint dates are overlapping with other sprint dates.
     * @param sprintStartDate Gets the sprint start date given by the user
     * @param sprintEndDate Gets the sprint end date given by the user
     * @param projectStartDate Gets the project start date given by the user
     * @param projectEndDate Gets the project end date given by the user
     * @param sprintList Gets the sprint list that stores all the sprint objects for the project
     * @return either "" or an error message string
     */
    public String validSprintDateRanges(Date sprintStartDate, Date sprintEndDate, Date projectStartDate, Date projectEndDate, List<Sprint> sprintList) {
        String invalidDateRange = "";

        if (sprintStartDate.after(sprintEndDate) || sprintEndDate.before(sprintStartDate)) {
            invalidDateRange += "Start date must always be before end date";
        } else if (sprintStartDate.before(projectStartDate) || sprintEndDate.after(projectEndDate)) {
            invalidDateRange += "Dates must be within the project dates of " + Project.dateToString(projectStartDate) + " - " + Project.dateToString(projectEndDate);
        } else if (!sprintList.isEmpty()) {
            for (Sprint eachSprint: sprintList) {
                if (((sprintStartDate.after(eachSprint.getSprintStartDate())) && (sprintStartDate.before(eachSprint.getSprintEndDate()))) ||
                        (sprintEndDate.after(eachSprint.getSprintStartDate()) && sprintEndDate.before(eachSprint.getSprintEndDate())) ||
                        (sprintStartDate.after(eachSprint.getSprintStartDate()) && sprintEndDate.before(eachSprint.getSprintEndDate()))) {
                    invalidDateRange += "Dates must not overlap with other sprints & it is overlapping with " + Project.dateToString(eachSprint.getSprintStartDate()) + " - " +
                            Project.dateToString(eachSprint.getSprintEndDate());
                    break;
                }
            }
        }
        return invalidDateRange;

    }

    /**
     * This function check for the validation for add/edit sprints page. Here, first it checks if the start date is after
     * the end date or end date is before the start date. Next, it checks if the sprint dates are within the project
     * dates. Lastly, it checks if the sprint dates are overlapping with other sprint dates.
     * @param sprintStartDate Gets the sprint start date given by the user
     * @param sprintEndDate Gets the sprint end date given by the user
     * @param projectStartDate Gets the project start date given by the user
     * @param projectEndDate Gets the project end date given by the user
     * @param sprintList Gets the sprint list that stores all the sprint objects for the project
     * @return either "" or an error message string
     */
    public String validEditSprintDateRanges(int sprintId, Date sprintStartDate, Date sprintEndDate, Date projectStartDate, Date projectEndDate, List<Sprint> sprintList) {
        String invalidDateRange = "";

        if (sprintStartDate.after(sprintEndDate) || sprintEndDate.before(sprintStartDate)) {
            invalidDateRange += "Start date must always be before end date";
        } else if (sprintStartDate.before(projectStartDate) || sprintEndDate.after(projectEndDate)) {
            invalidDateRange += "Dates must be within the project dates of " + Project.dateToString(projectStartDate) + " - " + Project.dateToString(projectEndDate);
        } else if (!sprintList.isEmpty()) {
            for (Sprint eachSprint: sprintList) {
                if (eachSprint.getId() == sprintId) {
                    continue;
                } else {
                    if (((sprintStartDate.after(eachSprint.getSprintStartDate())) && (sprintStartDate.before(eachSprint.getSprintEndDate()))) ||
                            (sprintEndDate.after(eachSprint.getSprintStartDate()) && sprintEndDate.before(eachSprint.getSprintEndDate())) ||
                            (sprintStartDate.after(eachSprint.getSprintStartDate()) && sprintEndDate.before(eachSprint.getSprintEndDate()))) {
                        invalidDateRange += "Dates must not overlap with other sprints & it is overlapping with " + Project.dateToString(eachSprint.getSprintStartDate()) + " - " +
                                Project.dateToString(eachSprint.getSprintEndDate());
                        break;
                    }
                }
            }
        }
        return invalidDateRange;

    }

}
