package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.*;
import java.util.Date;

@Entity // this is an entity, assumed to be in a table called Sprint
@Table (name = "Sprint")
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

    public Sprint() {}

    public Sprint(int parentProjectId, String sprintName,  String sprintDescription, Date sprintStartDate, Date sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = sprintStartDate;
        this.sprintEndDate = sprintEndDate;
    }

    public Sprint(int parentProjectId, String sprintName,  String sprintDescription, String sprintStartDate, String sprintEndDate) {
        this.parentProjectId = parentProjectId;
        this.sprintName = sprintName;
        this.sprintDescription = sprintDescription;
        this.sprintStartDate = Project.stringToDate(sprintStartDate);
        this.sprintEndDate = Project.stringToDate(sprintEndDate);
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

    public void setParentProjectId(int parentProjectId) {
        this.parentProjectId = parentProjectId;
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

    public boolean validWithProject(Project project) {
        try {
            return  this.sprintStartDate.after(project.getStartDate()) &&
                    this.sprintEndDate.before(project.getEndDate()) &&
                    this.sprintStartDate.before(this.sprintEndDate);
        } catch (Exception e) {
            return false;
        }
    }

//    @AssertTrue(message = "Start date must be before end date")
//    public boolean isPasswordsEqual() {
//        if (this.sprintStartDate.before(this.sprintEndDate)) {
//            return true;
//        }
//        return false;
//
//    }

}
