package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Sprint;

import java.util.Date;


/**
 * A message object sent through websockets holding information about an updated sprint
 * Contains all information used by javascript to update the sprint on the planner.
 */
public class SprintMessageOutput {

    private int id;
    private String name;
    private Date startDate;
    private Date endDate;

    /**
     * Empty constructor used to send an empty message for a nonexistent sprint
     */
    public SprintMessageOutput() {
    }

    /**
     * Constructor to create a message about an updated sprint
     * @param sprint the updated sprint to create a message for
     */
    public SprintMessageOutput(Sprint sprint) {
        this.id = sprint.getId();
        this.name = sprint.getSprintName();
        this.startDate = sprint.getSprintStartDate();
        this.endDate = sprint.getSprintEndDate();
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId(){
        return  id;
    }

    public void setName(String name) { this.name = name; }

    public String getName() { return this.name; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getStartDate() { return this.startDate; }

    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Date getEndDate() { return this.endDate; }
}
