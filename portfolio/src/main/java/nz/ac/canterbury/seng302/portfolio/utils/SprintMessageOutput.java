package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;

import java.util.Date;


/**
 * A message object sent through websockets holding information about an updated sprint
 * Contains all information used by javascript to update the sprint on the planner.
 * If the specified sprint cannot be found, then only the id field will have a value.
 */
public class SprintMessageOutput {

    private int id;
    private String name;
    private String colour;
    private Date startDate;
    private Date endDate;

    /**
     * Empty constructor used to send an empty message for a nonexistent sprint
     */
    public SprintMessageOutput() { }

    /**
     * Constructor to create a message about an updated sprint
     * @param sprint the updated sprint to create a message for
     */
    public SprintMessageOutput(Sprint sprint) {
        this.id = sprint.getId();
        this.name = sprint.getSprintName();
        this.startDate = sprint.getSprintStartDate();
        this.endDate = sprint.getSprintEndDate();
        this.colour = sprint.getSprintColour();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
    }

    public void setName(String name) { this.name = name; }

    public String getName() { return this.name; }

    public void setColour(String colour) { this.colour = colour; }

    public String getColour() { return this.colour; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getStartDate() { return this.startDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getEndDate() { return this.endDate; }
}
