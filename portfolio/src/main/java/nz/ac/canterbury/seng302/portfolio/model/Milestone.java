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
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="milestone_id")
    private int id;

    @ManyToOne
    @JoinColumn(name="id", nullable = false)
    private Project parentProject;

    @Column(nullable = false)
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The milestone name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    private String milestoneName;

    @Column (nullable = false)
    @Size(max=MAX_DESC_LENGTH, message="The milestone description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String milestoneDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMilestoneName() {
        return milestoneName;
    }

    public void setMilestoneName(String milestoneName) {
        this.milestoneName = milestoneName;
    }

    public String getMilestoneDescription() {
        return milestoneDescription;
    }

    public void setMilestoneDescription(String milestoneDescription) {
        this.milestoneDescription = milestoneDescription;
    }

    public Date getMilestoneDate() {
        return milestoneDate;
    }

    public void setMilestoneDate(Date milestoneDate) {
        this.milestoneDate = milestoneDate;
    }

    public Project getParentProject() {
        return parentProject;
    }

    public void setParentProject(Project parentProject) {
        this.parentProject = parentProject;
    }
}
