package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.text.ParseException;
import java.util.Date;
import java.util.List;


/**
 * Represents a sprint object. Sprints must have a parent project object that they are a part of.
 * Sprint objects are stored in a table called Sprint, as it is an @Entity.
 */
@Entity
public class Sprint {

    /** The id of this sprint. This id should be unique between all sprints, regardless of which project they
     * belong to. The id starts at 0 and should automatically increment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

    @Column(nullable = false)
    @Size(min=2, max=32, message="The character length must in range 2 and 32.")
    private String sprintName;

    @Column(nullable = false)
    private String sprintLabel;

    @Column (nullable = false)
    @Size(max=200, message="The character length must not exceed 200.")
    private String sprintDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintEndDate;

    @Column (nullable = false)
    @Size(min=7, max=7)
    private String sprintColour;

    public Sprint() {}

    /**
     * A constructor which set the given user data to the specified variables
     * @param parentProjectId Gets the project id
     * @param sprintName Gets the sprint name given by user
     * @param sprintDescription Gets the sprint description given by the user
     * @param sprintStartDate Gets the sprint start date as a Date object
     * @param sprintEndDate Gets the sprint end date as a Date object
     * @param sprintColour Gets the sprint colour given by the user
     */
    public Sprint(int parentProjectId, String sprintName,  String sprintDescription, Date sprintStartDate, Date sprintEndDate, String sprintColour) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = sprintStartDate;
        this.sprintEndDate = sprintEndDate;
        this.sprintColour = sprintColour;
    }

    /**
     * A constructor which set the given user data to the specified variables
     * @param parentProjectId Gets the project id
     * @param sprintName Gets the sprint name given by the user
     * @param sprintDescription Gets the sprint description given by the user
     * @param sprintStartDate Gets the sprint start date as a string
     * @param sprintEndDate Gets the sprint end date as a string
     * @param sprintColour Gets the sprint colour given by the user
     */
    public Sprint(int parentProjectId, String sprintName,  String sprintDescription, String sprintStartDate, String sprintEndDate, String sprintColour) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = Project.stringToDate(sprintStartDate);
        this.sprintEndDate = Project.stringToDate(sprintEndDate);
        this.sprintColour = sprintColour;
    }


    @Override
    /**
     * Returns a string listing the attributes of the sprint in the form "Sprint[x, x, x]".
     * @return said string
     */
    public String toString() {
        return String.format(
                "Sprint[id=%d, parentProjectId='%d', sprintName='%s', sprintLabel='%s', sprintStartDate='%s', sprintEndDate='%s', sprintDescription='%s', sprintColour='%s']",
                id, parentProjectId, sprintName, sprintLabel, sprintStartDate, sprintEndDate, sprintDescription, sprintColour);
    }

    public void setId(int id) {
        this.id = id;
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

    public String getSprintColour() { return this.sprintColour;  }
    public void setSprintColour(String sprintColour) { this.sprintColour = sprintColour; }

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
    public String validAddSprintDateRanges(Date sprintStartDate, Date sprintEndDate, Date projectStartDate, Date projectEndDate, List<Sprint> sprintList) throws ParseException {
        String invalidDateRange = "";
        DateUtils utils = new DateUtils();

        if (sprintStartDate.before(projectStartDate) || sprintEndDate.after(projectEndDate)) {
            invalidDateRange += "Dates must be within the project dates of " + utils.toString(projectStartDate) + " - " + utils.toString(projectEndDate);
        } else if (sprintStartDate.after(sprintEndDate) || sprintEndDate.before(sprintStartDate)) {
            invalidDateRange += "Start date must always be before end date";
        } else if (!sprintList.isEmpty()) {
            for (Sprint eachSprint: sprintList) {
                Date utilsSprintStartDate = utils.toDate(utils.toString(eachSprint.getSprintStartDate()));
                Date utilsSprintEndDate = utils.toDate(utils.toString(eachSprint.getSprintEndDate()));
                if (utilsSprintStartDate.equals(sprintStartDate) || utilsSprintStartDate.equals(sprintEndDate) || utilsSprintEndDate.equals(sprintStartDate) || utilsSprintEndDate.equals(sprintEndDate) ) {
                    invalidDateRange += "Dates must not overlap with other sprints & and it must not be same, it is overlapping with " + utils.toString(eachSprint.getSprintStartDate()) + " - " +
                            utils.toString(eachSprint.getSprintEndDate());
                    break;
                } else if (((sprintStartDate.after(utilsSprintStartDate)) && (sprintEndDate.before(utilsSprintEndDate))) ||
                        (sprintEndDate.after(utilsSprintStartDate) && sprintEndDate.before(utilsSprintEndDate)) ||
                        (sprintStartDate.after(utilsSprintStartDate) && sprintStartDate.before(utilsSprintEndDate))) {
                    invalidDateRange += "Dates must not overlap with other sprints & it is overlapping with " + utils.toString(eachSprint.getSprintStartDate()) + " - " +
                            utils.toString(eachSprint.getSprintEndDate());
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
    public String validEditSprintDateRanges(int sprintId, Date sprintStartDate, Date sprintEndDate, Date projectStartDate, Date projectEndDate, List<Sprint> sprintList) throws ParseException {
        String invalidDateRange = "";
        DateUtils utils = new DateUtils();

        if (sprintStartDate.before(projectStartDate) || sprintEndDate.after(projectEndDate)) {
            invalidDateRange += "Dates must be within the project dates of " + utils.toString(projectStartDate) + " - " + utils.toString(projectEndDate);
        } else if (sprintStartDate.after(sprintEndDate) || sprintEndDate.before(sprintStartDate)) {
            invalidDateRange += "Start date must always be before end date";
        } else if (!sprintList.isEmpty()) {
            for (Sprint eachSprint: sprintList) {
                if (eachSprint.getId() == sprintId) {
                    continue;
                } else {
                    Date utilsSprintStartDate = utils.toDate(utils.toString(eachSprint.getSprintStartDate()));
                    Date utilsSprintEndDate = utils.toDate(utils.toString(eachSprint.getSprintEndDate()));
                    if (utilsSprintStartDate.equals(sprintStartDate) || utilsSprintStartDate.equals(sprintEndDate) || utilsSprintEndDate.equals(sprintStartDate) || utilsSprintEndDate.equals(sprintEndDate)) {
                        invalidDateRange += "Sprint dates must not overlap with other sprints. Dates are overlapping with " + utils.toString(eachSprint.getSprintStartDate()) + " - " +
                                utils.toString(eachSprint.getSprintEndDate());
                        break;

                    } else if (
                            sprintStartDate.after(utilsSprintStartDate) && sprintStartDate.before(utilsSprintEndDate) ||
                                    sprintEndDate.after(utilsSprintStartDate) && sprintEndDate.before(utilsSprintEndDate) ||
                                    sprintStartDate.before(utilsSprintStartDate) && sprintEndDate.after(utilsSprintEndDate)
                    ) {
                        invalidDateRange += "Sprint dates must not overlap with other sprints. Dates are overlapping with " + utils.toString(eachSprint.getSprintStartDate()) + " - " +
                                utils.toString(eachSprint.getSprintEndDate());
                        break;
                    }
                }
            }
        }
        return invalidDateRange;

    }

}
