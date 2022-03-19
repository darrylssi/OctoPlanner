package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity // this is an entity, assumed to be in a table called Sprint
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

    @Column
    private String sprintName;

    @Column(nullable = false)
    private String sprintLabel;

    @Column
    private String sprintDescription;

    @Column
    private Date sprintStartDate;

    @Column
    private Date sprintEndDate;

    protected Sprint() {}

    public Sprint(int parentProjectId, String sprintName, String sprintLabel, String sprintDescription, Date sprintStartDate, Date sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintLabel = sprintLabel;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = sprintStartDate;
        this.sprintEndDate = sprintEndDate;
    }

    @Override
    public String toString() {
        return String.format(
                "Sprint[id=%d, parentProjectId='%d', sprintName='%s', sprintLabel='%s', sprintStartDate='%s', sprintEndDate='%s', sprintDescription='%s']",
                id, parentProjectId, sprintName, sprintLabel, sprintStartDate, sprintEndDate, sprintDescription);
    }


    public int getId(){
        return  id;
    }

    public int getParentProjectId() {
        return parentProjectId;
    }

    public String getName() {
        return sprintName;
    }

    public void setSprintName(String newName) {
        this.sprintName = newName;
    }

    public String getLabel() {
        return sprintLabel;
    }

    public String getDescription(){
        return sprintDescription;
    }

    public void setSprintDescription(String newDescription) {
        this.sprintDescription = newDescription;
    }

    public Date getStartDate() {
        return sprintStartDate;
    }

    public String getStartDateString() {
        return Project.dateToString(this.sprintStartDate);
    }

    public void setStartDate(Date newStartDate) {
        this.sprintStartDate = newStartDate;
    }

    public void setStartDateString(String date) {
        this.sprintStartDate = Project.stringToDate(date);
    }

    public Date getEndDate() {
        return sprintEndDate;
    }

    public String getEndDateString() {
        return Project.dateToString(this.sprintEndDate);
    }

    public void setEndDate(Date newEndDate) {
        this.sprintEndDate = newEndDate;
    }

    public void setEndDateString(String date) {
        this.sprintEndDate = Project.stringToDate(date);
    }

    public String getDates() {
        return getStartDateString() + " - " + getEndDateString();
    }
}
