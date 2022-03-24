package nz.ac.canterbury.seng302.portfolio.model;

import org.thymeleaf.util.DateUtils;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

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
    @NotNull
    /**
     * This is the id number of the project that the sprint belongs to, and should reference an existing project.
     */
    private int parentProjectId;
    @NotBlank(message = "Sprint name cannot be empty")
    /** The name of the sprint. Just a string chosen by the user, but should default to the sprint label. */
    private String sprintName;
//    @NotBlank(message = "Sprint label cannot be empty") // TODO disabled for testing until auto-assign is implemented
    /**
     * Sprint labels must be unique, and are of the form "Sprint[x]", where [x] is a number, starting at 1.
     * Within a project, all sprints must have labels that increment, without gaps, from 1 to n in order of occurence.
     * This constraint should still be enforced if sprints are deleted, rearranged, added, or otherwise edited.
     */
    private String sprintLabel;
    @NotNull
    /** The description of the sprint. @NotNull means that it may be empty, but not null. */
    private String sprintDescription;
    @NotNull
    /** The sprint start date. Must be before the end date. */
    private Date sprintStartDate;
    @NotNull
    /** The sprint end date. Must be before the start date. */
    private Date sprintEndDate;

    protected Sprint() {}

    /**
     * Constructor taking dates as date objects
     * @param parentProjectId
     * @param sprintName
     * @param sprintLabel
     * @param sprintDescription
     * @param sprintStartDate start date, as a date object
     * @param sprintEndDate end date, as a date object
     */
    public Sprint(int parentProjectId, String sprintName, String sprintLabel, String sprintDescription, Date sprintStartDate, Date sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintLabel = sprintLabel;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = sprintStartDate;
        this.sprintEndDate = sprintEndDate;
    }

    /**
     * Constructor taking dates as strings
     * @param parentProjectId
     * @param sprintName
     * @param sprintLabel
     * @param sprintDescription
     * @param sprintStartDate start date, as a string
     * @param sprintEndDate end date, as a string
     */
    public Sprint(int parentProjectId, String sprintName, String sprintLabel, String sprintDescription, String sprintStartDate, String sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintLabel = sprintLabel;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = Project.stringToDate(sprintStartDate);
        this.sprintEndDate = Project.stringToDate(sprintEndDate);
    }

    @Override
    /**
     * Returns a string listing the attributes of the sprint in the form "Sprint[x, x, x]".
     */
    public String toString() {
        return String.format(
                "Sprint[id=%d, parentProjectId='%d', sprintName='%s', sprintLabel='%s', sprintStartDate='%s', sprintEndDate='%s', sprintDescription='%s']",
                id, parentProjectId, sprintName, sprintLabel, sprintStartDate, sprintEndDate, sprintDescription);
    }


    public int getId(){
        return  id;
    }
    public int getParentProjectId() {
        return parentProjectId;
    }
    public void setParentProjectId(int id) { this.parentProjectId = id; }
    public String getName() {
        return sprintName;
    }
    public void setSprintName(String name) { this.sprintName = name; }
    public String getLabel() {
        return sprintLabel;
    }
    public String getDescription(){
        return sprintDescription;
    }
    public void setSprintDescription(String description) { this.sprintDescription = description; }

    /** Returns the start date as a date object. */
    public Date getStartDate() {
        return sprintStartDate;
    }

    /** Returns the start date as a string in format dd/MON/yyyy. */
    public String getStartDateString() {
        return Project.dateToString(this.sprintStartDate);
    }

    /** Sets the start date with a date object. */
    public void setStartDate(Date newStartDate) {
        this.sprintStartDate = newStartDate;
    }

    /** Sets the start date with a string in format dd/MON/yyyy. */
    public void setStartDateString(String date) {
        this.sprintStartDate = Project.stringToDate(date);
    }

    /** Returns the end date as a date object. */
    public Date getEndDate() {
        return sprintEndDate;
    }

    /** Returns the end date as a string in format dd/MON/yyyy. */
    public String getEndDateString() {
        return Project.dateToString(this.sprintEndDate);
    }

    /** Sets the end date with a date object. */
    public void setEndDate(Date newEndDate) {
        this.sprintEndDate = newEndDate;
    }

    /** Sets the end date with a string in format dd/MON/yyyy. */
    public void setEndDateString(String date) {
        this.sprintEndDate = Project.stringToDate(date);
    }
}
