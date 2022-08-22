package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


/**
 * Represents an event object. Event colour should be determined based on what it is being displayed with.
 * Event objects are stored in a table called Event, as it is an @Entity.
 */
@Entity
public class Event implements Schedulable {

    public static final String DEFAULT_COLOUR = "#ff3823";

    /** The id of this event. This id should be unique between all events.*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne
    @JoinColumn(name = "parent_project_id", nullable = false)
    private Project parentProject;

    @Column(nullable = false)
    @NotBlank(message="Event name cannot be blank")
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The event name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    private String eventName;

    @Column (nullable = false)
    @Size(max=MAX_DESC_LENGTH, message="The event description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String eventDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern= DISPLAY_DATETIME_FORMAT)
    private Date eventStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern= DISPLAY_DATETIME_FORMAT)
    private Date eventEndDate;

    public Event() {}

    /**
     * A constructor which set the given user data to the specified variables
     * @param eventName Gets the event name given by user
     * @param eventDescription Gets the event description given by the user
     * @param eventStartDate Gets the event start date as a Date object
     * @param eventEndDate Gets the event end date as a Date object
     */
    public Event(String eventName,  String eventDescription, Date eventStartDate, Date eventEndDate) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
    }

    /**
     * Returns a string listing the attributes of the event in the form "Event[x, x, x]".
     * @return said string
     */
    @Override
    public String toString() {
        return String.format(
                "Event[id=%d, eventName='%s', eventStartDate='%s', eventEndDate='%s', eventDescription='%s']",
                id, eventName, eventStartDate, eventEndDate, eventDescription);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public String getName() {
        return eventName;
    }

    public void setName(String name) {
        this.eventName = name;
    }

    public String getDescription(){
        return eventDescription;
    }

    public void setDescription(String description) {
        this.eventDescription = description;
    }

    public Date getStartDate() {
        return eventStartDate;
    }

    public void setStartDate(Date newStartDate) {
        this.eventStartDate = newStartDate;
    }

    public Date getEndDate() {
        return eventEndDate;
    }

    public void setEndDate(Date newEndDate) {
        this.eventEndDate = newEndDate;
    }

    public Project getParentProject() {
        return parentProject;
    }

    public void setParentProject(Project parentProject) {
        this.parentProject = parentProject;
    }

    /**
     * Gets a String to identify the type of this object.
     * This is used to specify which type of thymeleaf fragment to display without having to have
     * an instanceof check and a div specifically for each type of schedulable object.
     * The returned String should directly match the name of the thymeleaf fragment it will be displayed in.
     * @return A String constant containing the type of this object.
     */
    public String getType(){
        return EVENT_TYPE;
    }

    /**
     * Determines the correct colour for this event based on the list of sprints.
     * Specifically, this function returns the colour of the first sprint it finds which
     * overlaps the start date of the event (or end date if the end parameter is true).
     * If it finds no sprint, it returns the default colour determined by the system.
     * @param sprints a List object of sprints to choose a colour from.
     * @param end {boolean} fetch the colour at the end of the event, instead of the start.
     */
    public String determineColour(List<Sprint> sprints, boolean end) {
        Date comparisonDate = eventStartDate;
        if (end) {
            comparisonDate = eventEndDate;
        }

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
