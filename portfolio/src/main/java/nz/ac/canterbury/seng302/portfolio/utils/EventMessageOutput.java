package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Event;
import nz.ac.canterbury.seng302.portfolio.model.Sprint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A message object to be sent through websockets when an event is updated.
 * Contains all information used by javascript to update the event display in html.
 */
public class EventMessageOutput {

    enum BoxLocation {FIRST, INSIDE, AFTER}

    public static final String LIST_IN_ID_FORMAT = "schedulables-in-%d"; // id of sprint box
    public static final String LIST_AFTER_ID_FORMAT = "schedulables-after-%d"; // id of box after sprint
    public static final String LIST_BEFORE_ALL_ID_NAME = "schedulables-before"; // id of first box
    public static final String NEXT_EVENT_ID_FORMAT = "event-%d"; // id of an event (used for the next event)
    public static final String EVENT_BEFORE_ALL_ID_FORMAT = "%d-before"; // id of event in first box
    public static final String EVENT_IN_ID_FORMAT = "%d-in-%d"; // id of event in sprint box
    public static final String EVENT_AFTER_ID_FORMAT = "%d-after-%d"; // id of event after sprint box

    private int id;
    // NOTE if you change the names of these lists (or any of these variables), you will need to change them in websocketHandler.js
    private List<String> eventListIds; // List of all display boxes that this event is included in
    private List<String> nextEventIds; // List of events displayed immediately after this event in each box
    private List<String> eventBoxIds; // List of ids of the event box being created

    /**
     * Empty constructor so we can send an output for a nonexistent event
     */
    public EventMessageOutput() {
    }

    /**
     * Constructor for an EventMessageOutput
     * @param event the Event to take parameters from
     * @param sprints list of all sprints for the project of this event
     * @param events list of all events for the project of this event
     */
    public EventMessageOutput(Event event, List<Sprint> sprints, List<Event> events) {
        this.id = event.getId();

        // Set the lists
        this.generateLists(event, sprints, events);
    }

    /**
     * Finds where the event should go on the page, setting the IDs of:
     * the list box it should go in,
     * the event immediately after this event, and
     * the id of the new box being created.
     * @param event the event that is being updated
     * @param sprints list of all sprints in this event's project
     * @param events list of all events in this event's project
     */
    private void generateLists(Event event, List<Sprint> sprints, List<Event> events) {
        sprints.sort(Comparator.comparing(Sprint::getSprintEndDate));
        events.sort(Comparator.comparing(Event::getStartDate));

        this.eventListIds = new ArrayList<>();
        this.nextEventIds = new ArrayList<>();
        this.eventBoxIds = new ArrayList<>();
        // check if the event occurs before any sprints
        if(sprints.isEmpty()){
            // get the id of the event that is displayed after this event so that they appear in the correct order
            nextEventIds.add(getNextEvent(events, event.getParentProject().getProjectStartDate(),
                    event.getParentProject().getProjectEndDate(), event));
            setEventLocationIds(BoxLocation.FIRST, -1);
        } else if(sprints.get(0).getSprintStartDate().after(event.getStartDate())) {
            // get the id of the event that is displayed after this event so that they appear in the correct order
            nextEventIds.add(getNextEvent(events, event.getParentProject().getProjectStartDate(),
                    sprints.get(0).getSprintStartDate(), event));
            setEventLocationIds(BoxLocation.FIRST, -1);
        }

        //get list of all event box ids to include the event on the project details page
        for (int i = 0; i < sprints.size(); i++) {
            // Check if the event overlaps with the sprint at index i
            if(DateUtils.timesOverlap(sprints.get(i).getSprintStartDate(), sprints.get(i).getSprintEndDate(),
                    event.getStartDate(), event.getEndDate())){
                nextEventIds.add(getNextEvent(events, sprints.get(i).getSprintStartDate(),
                        sprints.get(i).getSprintEndDate(), event));
                setEventLocationIds(BoxLocation.INSIDE, sprints.get(i).getId());
            }
            // Check if event occurs between the end of this sprint and the start of the next one
            if((sprints.size() > i+1) && DateUtils.timesOverlap(sprints.get(i).getSprintEndDate(), sprints.get(i+1).getSprintStartDate(),
                    event.getStartDate(), event.getEndDate())) {
                nextEventIds.add(getNextEvent(events, sprints.get(i).getSprintEndDate(),
                        sprints.get(i+1).getSprintStartDate(), event));
                setEventLocationIds(BoxLocation.AFTER, sprints.get(i).getId());
            }
            // If this sprint is the last sprint, check if the event occurs after the end of this sprint
            if(sprints.size() == i+1 && sprints.get(i).getSprintEndDate().before(event.getEndDate())){
                nextEventIds.add(getNextEvent(events, sprints.get(i).getSprintEndDate(),
                        event.getParentProject().getProjectEndDate(), event));
                setEventLocationIds(BoxLocation.AFTER, sprints.get(i).getId());
            }
        }
    }

    /**
     * Sets the list of id and event box id that the event in this message will occupy.
     * Should be called once the actual location has already been determined.
     * @param boxLocation the location, relative to sprints, of the updated event
     * @param sprintId the id of the sprint this event is located in or after. -1 if event is before all sprints
     */
    private void setEventLocationIds(BoxLocation boxLocation, int sprintId) {
        switch (boxLocation) {
            case FIRST -> {
                this.eventListIds.add(LIST_BEFORE_ALL_ID_NAME);
                this.eventBoxIds.add(String.format(EVENT_BEFORE_ALL_ID_FORMAT, this.id));
            }
            case INSIDE -> {
                this.eventListIds.add(String.format(LIST_IN_ID_FORMAT, sprintId));
                this.eventBoxIds.add(String.format(EVENT_IN_ID_FORMAT, this.id, sprintId));
            }
            case AFTER -> {
                this.eventListIds.add(String.format(LIST_AFTER_ID_FORMAT, sprintId));
                this.eventBoxIds.add(String.format(EVENT_AFTER_ID_FORMAT, this.id, sprintId));
            }
        }
    }

    /**
     * Gets the next event within a certain time period
     * @param events the list of all events
     * @param periodStart the start of the time period
     * @param periodEnd the end of the time period
     * @return the box id of the next event or -1 if there is no next event
     */
    private String getNextEvent(List<Event> events, Date periodStart, Date periodEnd, Event updatedEvent){
        for (Event event : events) {
            if (event.getStartDate().after(updatedEvent.getStartDate()) &&
                    DateUtils.timesOverlap(periodStart, periodEnd,
                            event.getStartDate(), event.getEndDate())) {
                return (String.format(NEXT_EVENT_ID_FORMAT, event.getId()));
            }
        }
        return "-1";
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
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

}
