package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


/**
 * Represents a sprint object. Sprints must have a parent project object that they are a part of.
 * Sprint objects are stored in a table called Sprint, as it is an @Entity.
 */
@Entity
@Table (name = "Sprint")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

    @Column(nullable = false)
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The sprint name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    @NotBlank(message="Sprint name is required.")
    private String sprintName;

    @Column(nullable = false)
    private String sprintLabel;

    @Column (nullable = false)
    @Size(max=MAX_DESC_LENGTH, message="The sprint description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String sprintDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern=DATE_FORMAT)
    private Date sprintStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern=DATE_FORMAT)
    private Date sprintEndDate;

    @Column (nullable = false)
    @Size(min=COLOUR_LENGTH, max=COLOUR_LENGTH)
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
        this.sprintStartDate = DateUtils.toDate(sprintStartDate);
        this.sprintEndDate = DateUtils.toDate(sprintEndDate);
        this.sprintColour = sprintColour;
    }

    /**
     * Returns a string listing the attributes of the sprint in the form "Sprint[x, x, x]".
     * @return said string
     */
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
    public String getStartDateString() {return DateUtils.toDisplayString(this.sprintStartDate);}

    public void setStartDate(Date newStartDate) {
        this.sprintStartDate = newStartDate;
    }

    /**
     * Sets the sprint start date in a string format
     * @param date Gets the sprint start date as a string given by the user
     */
    public void setStartDateString(String date) {
        this.sprintStartDate = DateUtils.toDate(date);
    }

    public Date getSprintEndDate() {
        return sprintEndDate;
    }

    /**
     * Gets the string format of a spring end date
     * @return sprint's end date as a string
     */
    public String getEndDateString() {
        return DateUtils.toDisplayString(this.sprintEndDate);
    }

    public void setEndDate(Date newEndDate) {
        this.sprintEndDate = newEndDate;
    }

    /**
     * Sets the sprint end date as a string format
     * @param date Gets the sprint start date as a string given by the user
     */
    public void setEndDateString(String date) {
        this.sprintEndDate = DateUtils.toDate(date);
    }

    public void setSprintLabel(String newLabel) { this.sprintLabel = newLabel; }

    public String getSprintColour() { return this.sprintColour;  }
    public void setSprintColour(String sprintColour) { this.sprintColour = sprintColour; }
}
