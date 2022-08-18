package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Represents a milestone object.
 * Milestone objects are stored in a table called Milestone, as it is an @Entity.
 */
@Entity
public class Milestone {
    public static final String DEFAULT_COLOUR = "#ff3823";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private int id;

    @ManyToOne
    @JoinColumn(name="parent_project_id", nullable = false)
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
    @DateTimeFormat(pattern=DATE_FORMAT)
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

    /**
     * Determines the correct colour for this milestone based on the list of sprints.
     * Specifically, this function returns the colour of the first sprint it finds which
     * contains the date of the milestone. If it finds no sprint, it returns the default colour
     * determined by the system.
     * @param sprints a List object of sprints to choose a colour from.
     */
    public String determineColour(List<Sprint> sprints) {

        for (Sprint checkedSprint : sprints) {
            Date sprintStart = checkedSprint.getSprintStartDate();
            Date sprintEnd = checkedSprint.getSprintEndDate();

            /* Sprints are assumed to be active on their start and end dates, so we also check for equality */
            if ((sprintStart.before(milestoneDate) || sprintStart.equals(milestoneDate)) &&
                    (sprintEnd.after(milestoneDate) || sprintEnd.equals(milestoneDate))) {
                return checkedSprint.getSprintColour();
            }
        }
        return DEFAULT_COLOUR;
    }

}
