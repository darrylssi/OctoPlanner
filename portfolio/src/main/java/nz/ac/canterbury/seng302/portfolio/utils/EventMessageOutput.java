package nz.ac.canterbury.seng302.portfolio.utils;

import java.util.ArrayList;
import java.util.List;

public class EventMessageOutput {
    private int id;
    private int parentProjectId;
    private String name;
    private String description;
    private ArrayList<String> sprintIds;
    private String startDate;
    private String endDate;
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
        this.sprintIds = (ArrayList<String>)sprintIds;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
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
