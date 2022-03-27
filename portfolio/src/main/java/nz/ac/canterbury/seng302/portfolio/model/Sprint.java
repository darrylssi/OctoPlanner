package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity // this is an entity, assumed to be in a table called Sprint
@Table (name = "Sprint")
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column
    private int parentProjectId;

    @Column
    @Size(min=2, max=32, message="The character length must in range 2 and 32.") //TODO testing values
    private String sprintName;

    @Column(nullable = false)
    private String sprintLabel;

    @Column(nullable = true)
    @Size(max=200, message="The character length must not exceed 200.") //TODO testing values
    private String sprintDescription;

    // This is "org.springframework.format.annotation.DateTimeFormat"
    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintStartDate;

    @Column (nullable = false)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
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

    public void setParentProjectId(int id) {
        this.parentProjectId = id;
    }

    public int getParentProjectId() {
        return parentProjectId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String newName) {
        this.sprintName = newName;
    }

    public String getSprintLabel() {
        return sprintLabel;
    }

    public String getSprintDescription(){
        return sprintDescription;
    }

    public void setSprintDescription(String newDescription) {
        this.sprintDescription = newDescription;
    }

    public Date getSprintStartDate() {
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

    public Date getSprintEndDate() {
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

    public void setSprintLabel(String newLabel) { this.sprintLabel = newLabel; }


    public String validSprintDateRanges(Date sprintStartDate, Date sprintEndDate, Date projectStartDate, Date projectEndDate, List<Sprint> sprintList) {
        String invalidDateRange = "";

        if (sprintStartDate.before(projectStartDate) || sprintEndDate.after(projectEndDate)) {
            invalidDateRange += "Dates must be within the project dates of " + projectEndDate + "-" + projectEndDate;
        } else if (!sprintList.isEmpty()) {
            for (Sprint eachSprint: sprintList) {
                if (((sprintStartDate.after(eachSprint.getSprintStartDate())) && (sprintStartDate.before(eachSprint.getSprintEndDate()))) ||
                        (sprintEndDate.after(eachSprint.getSprintStartDate()) && sprintEndDate.before(eachSprint.getSprintEndDate()))) {
                    invalidDateRange += "Dates must not overlap with other sprints & it is overlapping with " + eachSprint.getSprintStartDate() + "-" +
                            eachSprint.getSprintEndDate();
                    break;
                }
            }
        }
        return invalidDateRange;

    }

}
