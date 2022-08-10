package nz.ac.canterbury.seng302.portfolio.utils;

import java.util.Date;
import java.util.List;

public class EventMessageOutput {
    private int id;
    private int parentProjectId;
    private String name;
    private String description;
    private List<String> sprintIds;
    private List<String> eventIds;
    private List<String> eventBoxIds;
    private Date startDate;
    private Date endDate;
    private String startDateString;
    private String endDateString;
    private String startColour;
    private String endColour;

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

    public List<String> getSprintIds() {
        return sprintIds;
    }

    public void setSprintIds(List<String> sprintIds) {
        this.sprintIds = sprintIds;
    }

    public List<String> getEventIds() {
        return eventIds;
    }

    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
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
