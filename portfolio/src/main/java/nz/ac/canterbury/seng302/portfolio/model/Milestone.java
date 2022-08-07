package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


@Entity
public class Milestone {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

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
     * @param parentProjectId Gets the project id
     * @param milestoneName Gets the milestone name given by user
     * @param milestoneDescription Gets the milestone description given by the user
     * @param milestoneDate Gets the milestone date as a Date object
     */
    public Milestone(int parentProjectId, String milestoneName,  String milestoneDescription, Date milestoneDate) {
        this.parentProjectId = parentProjectId;
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

    public int getParentProjectId() {
        return parentProjectId;
    }

    public void setParentProjectId(int parentProjectId) {
        this.parentProjectId = parentProjectId;
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
}
