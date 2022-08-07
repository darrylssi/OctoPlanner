package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;
import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.DATETIME_FORMAT;

/**
 * Represents a deadline object. Deadline colour should be determined based on what it is being displayed with.
 * Deadline objects are stored in a table called Deadline, as it is an @Entity.
 */
@Entity
public class Deadline {

    public static final String DEFAULT_COLOUR = "#ff3823";

    /** The id of this deadline. This id should be unique between all deadlines.*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

    @Column(nullable = false)
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The deadline name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    private String deadlineName;

    @Column (nullable = false)
    @Size(max=MAX_DESC_LENGTH, message="The deadline description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String deadlineDescription;

    @Column (nullable = false)
    @DateTimeFormat(pattern=DATETIME_FORMAT)
    private Date deadlineDate;


    public Deadline() {}

    /**
     * A constructor which set the given user data to the specified variables
     * @param parentProjectId Gets the project id
     * @param deadlineName Gets the deadline name given by user
     * @param deadlineDescription Gets the deadline description given by the user
     * @param deadlineDate Gets the deadline date as a Date object
     */
    public Deadline(int parentProjectId, String deadlineName,  String deadlineDescription, Date deadlineDate) {
        this.parentProjectId = parentProjectId;
        this.deadlineName = deadlineName;
        this.deadlineDescription = deadlineDescription;
        this.deadlineDate = deadlineDate;
    }


    /**
     * Returns a string listing the attributes of the deadline in the form "Deadline[x, x, x]".
     * @return said string
     */
    @Override
    public String toString() {
        return String.format(
                "Deadline[id=%d, parentProjectId='%d', deadlineName='%s', deadlineDate='%s', deadlineDescription='%s']",
                id, parentProjectId, deadlineName, deadlineDate, deadlineDescription);
    }

    /**
     * Sets the value of the deadline id
     * @param id the value to set the id to
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the deadline id
     * @return deadline's id
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
     * Gets the deadline name
     * @return deadline's name
     */
    public String getDeadlineName() {
        return deadlineName;
    }

    /**
     * Sets the deadline name
     * @param newName Gets the deadline name given by user
     */
    public void setDeadlineName(String newName) {
        this.deadlineName = newName;
    }

    /**
     * Gets the deadline description
     * @return deadline's description
     */
    public String getDeadlineDescription(){
        return deadlineDescription;
    }

    /**
     * Sets the deadline Description
     * @param newDescription Gets the deadline description given by the user
     */
    public void setDeadlineDescription(String newDescription) {
        this.deadlineDescription = newDescription;
    }

    /**
     * Gets the deadline date
     * @return deadline's date
     */
    public Date getDeadlineDate() {
        return deadlineDate;
    }

    /**
     * Sets the deadline date
     * @param newDate The deadline date given by the user
     */
    public void setDate(Date newDate) {
        this.deadlineDate = newDate;
    }

    /**
     * Determines the correct colour for this deadline based on the list of sprints.
     * Specifically, this function returns the colour of the first sprint it finds which
     * contains the date of the deadline. If it finds no sprint, it returns the default colour
     * determined by the system.
     * @param sprints a List object of sprints to choose a colour from.
     */
    public String determineColour(List<Sprint> sprints) {
        Date comparisonDate = deadlineDate;

        for (Sprint checkedSprint : sprints) {
            Date sprintStart = checkedSprint.getSprintStartDate();
            Date sprintEnd = checkedSprint.getSprintEndDate();

            /* Sprints are assumed to be active on their start and end dates, so we also check for equality */
            if ((sprintStart.before(comparisonDate) || sprintStart.equals(comparisonDate)) &&
                    (sprintEnd.after(comparisonDate) || sprintEnd.equals(comparisonDate))) {
                return checkedSprint.getSprintColour();
            }
        }

        return DEFAULT_COLOUR;
    }
}
