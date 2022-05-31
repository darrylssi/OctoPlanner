package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;


/**
 * Represents a sprint object. Sprints must have a parent project object that they are a part of.
 * Sprint objects are stored in a table called Sprint, as it is an @Entity.
 */
@Entity
public class Sprint {
    @Transient
    private final DateUtils utils = new DateUtils();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

    @Column
    @Size(min=2, max=32, message="The character length must in range 2 and 32.") //TODO testing values
    private String sprintName;

    @Column(nullable = false)
    private String sprintLabel;

    @Column
    @Size(max=200, message="The character length must not exceed 200.") //TODO testing values
    private String sprintDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintEndDate;

    @Column (nullable = false)
    @Size(max=10)
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
        this.sprintStartDate = utils.toDate(sprintStartDate);
        this.sprintEndDate = utils.toDate(sprintEndDate);
        this.sprintColour = sprintColour;
    }


    @Override
    public String toString() {
        return String.format(
                "Sprint[id=%d, parentProjectId='%d', sprintName='%s', sprintLabel='%s', sprintStartDate='%s', sprintEndDate='%s', sprintDescription='%s', sprintColour='%s']",
                id, parentProjectId, sprintName, sprintLabel, sprintStartDate, sprintEndDate, sprintDescription, sprintColour);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
    }

    public void setParentProjectId(int id) {
        this.parentProjectId = id;
    }

    public int getParentProjectId() {
        return parentProjectId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String newName) {
        this.sprintName = newName;
    }

    public String getSprintLabel() {
        return sprintLabel;
    }

    public String getSprintDescription(){
        return sprintDescription;
    }

    public void setSprintDescription(String newDescription) {
        this.sprintDescription = newDescription;
    }

    public Date getSprintStartDate() {
        return sprintStartDate;
    }

    /**
     * Gets the string format of the sprint start date
     * @return sprint's start date as a string
     */
    public String getStartDateString() {return utils.toString(this.sprintStartDate);}

    public void setStartDate(Date newStartDate) {
        this.sprintStartDate = newStartDate;
    }

    /**
     * Sets the sprint start date in a string format
     * @param date Gets the sprint start date as a string given by the user
     */
    public void setStartDateString(String date) {
        this.sprintStartDate = utils.toDate(date);
    }

    public Date getSprintEndDate() {
        return sprintEndDate;
    }

    /**
     * Gets the string format of a spring end date
     * @return sprint's end date as a string
     */
    public String getEndDateString() {
        return utils.toString(this.sprintEndDate);
    }

    public void setEndDate(Date newEndDate) {
        this.sprintEndDate = newEndDate;
    }

    /**
     * Sets the sprint end date as a string format
     * @param date Gets the sprint start date as a string given by the user
     */
    public void setEndDateString(String date) {
        this.sprintEndDate = utils.toDate(date);
    }

    public void setSprintLabel(String newLabel) { this.sprintLabel = newLabel; }

    public String getSprintColour() { return this.sprintColour;  }
    public void setSprintColour(String sprintColour) { this.sprintColour = sprintColour; }
}
