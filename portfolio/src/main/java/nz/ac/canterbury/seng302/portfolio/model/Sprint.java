package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity // this is an entity, assumed to be in a table called Sprint
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @NotBlank(message = "Sprint parent project id cannot be empty")
    private int parentProjectId;
    @NotBlank(message = "Sprint name cannot be empty")
    private String sprintName;
    @NotBlank(message = "Sprint label cannot be empty")
    private String sprintLabel;
    @NotNull // may be empty, but not null
    private String sprintDescription;
    @NotBlank(message = "Sprint start date cannot be empty")
    private Date sprintStartDate;
    @NotBlank(message = "Sprint end date cannot be empty")
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
    public String getLabel() {
        return sprintLabel;
    }
    public String getDescription(){
        return sprintDescription;
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
        this.sprintStartDate = Project.stringToDate(date);
    }
}
