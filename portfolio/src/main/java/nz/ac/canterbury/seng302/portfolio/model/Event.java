package nz.ac.canterbury.seng302.portfolio.model;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;


/**
 * Represents an event object. Event colour should be determined based on what it is being displayed with.
 * Event objects are stored in a table called Event, as it is an @Entity.
 */
@Entity
public class Event {

    /** The id of this event. This id should be unique between all events.*/
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // Is this necessary for an event?
    @Column
    private int parentProjectId;

    @Column(nullable = false)
    @Size(min=2, max=32, message="The event name must be between 2 and 32 characters.")
    private String eventName;

    @Column (nullable = false)
    @Size(max=200, message="The event description must not exceed 200 characters.")
    private String eventDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy HH:mm:ss")
    private Date eventStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy HH:mm:ss")
    private Date eventEndDate;

    public Event() {}

    /**
     * A constructor which set the given user data to the specified variables
     * @param parentProjectId Gets the project id
     * @param eventName Gets the event name given by user
     * @param eventDescription Gets the event description given by the user
     * @param eventStartDate Gets the event start date as a Date object
     * @param eventEndDate Gets the event end date as a Date object
     */
    public Event(int parentProjectId, String eventName,  String eventDescription, Date eventStartDate, Date eventEndDate) {
        this.parentProjectId = parentProjectId;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventStartDate = eventStartDate;
        this.eventEndDate = eventEndDate;
    }

    @Override
    /**
     * Returns a string listing the attributes of the event in the form "Event[x, x, x]".
     * @return said string
     */
    public String toString() {
        return String.format(
                "Event[id=%d, parentProjectId='%d', eventName='%s', eventStartDate='%s', eventEndDate='%s', eventDescription='%s']",
                id, parentProjectId, eventName, eventStartDate, eventEndDate, eventDescription);
    }

    /**
    * Sets the value of the event id 
    * @param id the value to set the id to
    */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the event id
     * @return event's id
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
     * Gets the event name
     * @return event's name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the event name
     * @param newName Gets the event name given by user
     */
    public void setEventName(String newName) {
        this.eventName = newName;
    }

    /**
     * Gets the event description
     * @return event's description
     */
    public String getEventDescription(){
        return eventDescription;
    }

    /**
     * Sets the event Description
     * @param newDescription Gets the event description given by the user
     */
    public void setEventDescription(String newDescription) {
        this.eventDescription = newDescription;
    }

    /**
     * Gets the event start date
     * @return event's start date
     */
    public Date getEventStartDate() {
        return eventStartDate;
    }

    /**
     * Sets the event start date
     * @param newStartDate The event start date given by the user
     */
    public void setStartDate(Date newStartDate) {
        this.eventStartDate = newStartDate;
    }

    /**
     * Gets the event end date
     * @return event's end date
     */
    public Date getEventEndDate() {
        return eventEndDate;
    }

    /**
     * Sets the event end date
     * @param newEndDate The event end date given by user
     */
    public void setEndDate(Date newEndDate) {
        this.eventEndDate = newEndDate;
    }

    /**
     * Determines the correct colour for this event based on the list of sprints.
     * Specifically, this function returns the colour of the first sprint it finds which
     * overlaps the start date of the event (or end date if the end paramter is true).
     * If it finds no sprint, it returns the default colour determined by the system.
     * @param sprints a List object of sprints to choose a colour from.
     * @param end {boolean} fetch the colour at the end of the event, instead of the start.
     */
    public String determineColour(List<Sprint> sprints, boolean end) {
        Date comparisonDate = eventStartDate;
        if (end) {
            comparisonDate = eventEndDate;
        }

        for(int i = 0; i < sprints.size(); i++) {
            Sprint checkedSprint = sprints.get(i);
            Date sprintStart = checkedSprint.getSprintStartDate();
            Date sprintEnd = checkedSprint.getSprintEndDate();

            /* Sprints are assumed to be active on their start and end dates, so we also check for equality */
            if ((sprintStart.before(comparisonDate) || sprintStart.equals(comparisonDate)) &&
                    (sprintEnd.after(comparisonDate) || sprintEnd.equals(comparisonDate))) {
                return checkedSprint.getSprintColour();
            }
        }

        return "#ff3823";             // Default colour
    }
}
