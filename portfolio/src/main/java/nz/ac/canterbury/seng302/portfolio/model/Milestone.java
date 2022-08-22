package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Represents a milestone object.
 * Milestone objects are stored in a table called Milestone, as it is an @Entity.
 */
@Entity
public class Milestone implements Schedulable {

    public static final String DEFAULT_COLOUR = "#ff3823";

    /** The id of this milestone. This id should be unique between all milestones.*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_project_id", nullable = false)
    private Project parentProject;

    @Column(nullable = false)
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The milestone name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    private String milestoneName;

    @Column (nullable = false)
    @Size(max=MAX_DESC_LENGTH, message="The milestone description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String milestoneDescription;

    @Column (nullable = false)
    @DateTimeFormat(pattern=DATETIME_FORMAT)
    private Date milestoneDate;


    public Milestone() {}

    /**
     * A constructor which set the given user data to the specified variables
     * @param milestoneName Gets the milestone name given by user
     * @param milestoneDescription Gets the milestone description given by the user
     * @param milestoneDate Gets the milestone date as a Date object
     */
    public Milestone(String milestoneName,  String milestoneDescription, Date milestoneDate) {
        this.milestoneName = milestoneName;
        this.milestoneDescription = milestoneDescription;
        this.milestoneDate = milestoneDate;
    }


    /**
     * Returns a string listing the attributes of the milestone in the form "Milestone[x, x, x]".
     * @return said string
     */
    @Override
    public String toString() {
        return String.format(
                "Milestone[id=%d, milestoneName='%s', milestoneDate='%s', milestoneDescription='%s']",
                id, milestoneName, milestoneDate, milestoneDescription);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Project getParentProject() {
        return parentProject;
    }

    public void setParentProject(Project parentProject) {
        this.parentProject = parentProject;
    }

    public String getName() {
        return milestoneName;
    }

    public void setName(String name) {
        this.milestoneName = name;
    }

    public String getDescription() {
        return milestoneDescription;
    }

    public void setDescription(String description) {
        this.milestoneDescription = description;
    }

    public Date getStartDate() {
        return milestoneDate;
    }

    public void setStartDate(Date date) {
        this.milestoneDate = date;
    }

    public Date getEndDate() {
        return getStartDate();
    }

    public void setEndDate(Date date) {
        setStartDate(date);
    }

    /**
     * Gets a String to identify the type of this object.
     * This is used to specify which type of thymeleaf fragment to display without having to have
     * an instanceof check and a div specifically for each type of schedulable object.
     * The returned String should directly match the name of the thymeleaf fragment it will be displayed in.
     * @return A String constant containing the type of this object.
     */
    public String getType(){
        return MILESTONE_TYPE;
    }

}
