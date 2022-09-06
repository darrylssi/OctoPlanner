package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Represents a deadline object.
 * Deadline objects are stored in a table called Deadline, as it is an @Entity.
 */
@Entity
public class Deadline implements Schedulable {

    public static final String DEFAULT_COLOUR = "#ff3823";

    /** The id of this deadline. This id should be unique between all deadlines.*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_project_id", nullable = false)
    private Project parentProject;

    @Column(nullable = false)
    @NotBlank(message="Deadline name cannot be blank")
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
     * @param deadlineName Gets the deadline name given by user
     * @param deadlineDescription Gets the deadline description given by the user
     * @param deadlineDate Gets the deadline date as a Date object
     */
    public Deadline(String deadlineName,  String deadlineDescription, Date deadlineDate) {
        this.deadlineName = deadlineName;
        this.deadlineDescription = deadlineDescription.trim();
        this.deadlineDate = deadlineDate;
    }


    /**
     * Returns a string listing the attributes of the deadline in the form "Deadline[x, x, x]".
     * @return said string
     */
    @Override
    public String toString() {
        return String.format(
                "Deadline[id=%d, deadlineName='%s', deadlineDate='%s', deadlineDescription='%s']",
                id, deadlineName, deadlineDate, deadlineDescription);
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
        return deadlineName;
    }

    public void setName(String name) {
        this.deadlineName = name;
    }

    public String getDescription() {
        return deadlineDescription;
    }

    public void setDescription(String description) {
        this.deadlineDescription = description.trim();
    }

    public Date getStartDate() {
        return deadlineDate;
    }

    public void setStartDate(Date date) {
        this.deadlineDate = date;
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
        return DEADLINE_TYPE;
    }

    /**
     * Determines the correct colour for this deadline based on the list of sprints.
     * Specifically, this function returns the colour of the first sprint it finds which
     * contains the date of the deadline. If it finds no sprint, it returns the default colour
     * determined by the system.
     * @param sprints a List object of sprints to choose a colour from.
     */
    public String determineColour(List<Sprint> sprints, boolean end) {
        for (Sprint checkedSprint : sprints) {
            Date sprintStart = checkedSprint.getSprintStartDate();
            Date sprintEnd = checkedSprint.getSprintEndDate();

            /* Sprints are assumed to be active on their start and end dates, so we also check for equality */
            if ((sprintStart.before(deadlineDate) || sprintStart.equals(deadlineDate)) &&
                    (sprintEnd.after(deadlineDate) || sprintEnd.equals(deadlineDate))) {
                return checkedSprint.getSprintColour();
            }
        }

        return DEFAULT_COLOUR;
    }
}
