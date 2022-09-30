package nz.ac.canterbury.seng302.portfolio.utils;

import nz.ac.canterbury.seng302.portfolio.model.Project;

import java.util.Date;


/**
 * A message object sent through websockets holding information about an updated project
 * Contains all information used by javascript to update the sprint on the planner.
 * If the specified project cannot be found, then only the id field will have a value.
 */
public class ProjectMessageOutput {

    private int id;
    private String name;
    private Date startDate;
    private Date endDate;

    /**
     * Empty constructor used to send an empty message for a nonexistent project
     */
    public ProjectMessageOutput() { }

    /**
     * Constructor to create a message about an updated project
     * @param sprint the updated sprint to create a message for
     */
    public ProjectMessageOutput(Project project) {
        this.id = project.getId();
        this.name = project.getProjectName();
        this.startDate = project.getProjectStartDate();
        this.endDate = project.getProjectEndDate();
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
