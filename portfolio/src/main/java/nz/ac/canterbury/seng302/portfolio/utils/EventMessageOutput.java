package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Event;

import java.util.Date;
import java.util.List;

/**
 * A message object to be sent through websockets when an event is updated.
 * Contains all information used by javascript to update the event display in html.
 */
public class EventMessageOutput {
    private int id;
    private int parentProjectId;
    private String name;
    private String description;
    private List<String> eventListIds; // List of all display boxes that this event is included in
    private List<String> nextEventIds; // List of events displayed immediately after this event in each box
    private List<String> eventBoxIds; // List of ids of the event box being created

    // Dates used for editing events
    private Date startDate;
    private Date endDate;

    // Dates used to display events
    private String startDateString;
    private String endDateString;
    private String startColour;
    private String endColour;

    /**
     * Empty constructor so we can send an output for a nonexistent event
     */
    public EventMessageOutput() {
    }

    /**
     * Constructor for an EventMessageOutput
     * @param event the Event to take parameters from
     */
    public EventMessageOutput(Event event) {
        this.id = event.getId();
        this.parentProjectId = event.getParentProject().getId();
        this.name = event.getEventName();
        this.description = event.getEventDescription();
        this.startDate = event.getEventStartDate();
        this.endDate = event.getEventEndDate();
        this.startDateString = DateUtils.toDisplayDateTimeString(event.getEventStartDate());
        this.endDateString = DateUtils.toDisplayDateTimeString(event.getEventEndDate());
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

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    public List<String> getEventListIds() {
        return eventListIds;
    }

    public void setEventListIds(List<String> eventListIds) {
        this.eventListIds = eventListIds;
    }

    public List<String> getNextEventIds() {
        return nextEventIds;
    }

    public void setNextEventIds(List<String> nextEventIds) {
        this.nextEventIds = nextEventIds;
    }

    public List<String> getEventBoxIds() {
        return eventBoxIds;
    }

    public void setEventBoxIds(List<String> eventBoxIds) {
        this.eventBoxIds = eventBoxIds;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public void setStartColour(String startColour) {
        this.startColour = startColour;
    }

    public void setEndColour(String endColour) {
        this.endColour = endColour;
    }

    public String getStartColour() {
        return startColour;
    }

    public String getEndColour() {
        return endColour;
    }
}
