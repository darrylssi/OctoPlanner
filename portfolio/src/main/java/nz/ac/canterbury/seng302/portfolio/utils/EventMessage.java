package nz.ac.canterbury.seng302.portfolio.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class for sending event updates through websockets.
 */
public class EventMessage {

    private int id;
    private int parentProjectId;
    private String name;
    private String description;
    private ArrayList<Integer> sprintIds;
    private Date startDate;
    private Date endDate;
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

    public List<Integer> getSprintIds() {
        return sprintIds;
    }

    public void setSprintIds(List<Integer> sprintIds) {
        this.sprintIds = (ArrayList<Integer>)sprintIds;
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

    public String getEndColour() {
        return endColour;
    }

    public String getStartColour() {
        return startColour;
    }

    public void setEndColour(String endColour) {
        this.endColour = endColour;
    }

    public void setStartColour(String startColour) {
        this.startColour = startColour;
    }
}
