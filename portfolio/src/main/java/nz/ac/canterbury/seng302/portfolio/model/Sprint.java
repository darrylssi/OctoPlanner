package nz.ac.canterbury.seng302.portfolio.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import org.springframework.format.annotation.DateTimeFormat;

@Entity // this is an entity, assumed to be in a table called Sprint
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private int parentProjectId;

    @NotBlank
    @Column(nullable = false)
    @Size(min = 2, message = "The length must be minimum 2.")
    private String sprintName;

    private String sprintLabel = "Sprint ";
    private int sprintNum = 0;

    @Size(max = 200, message = "The maximum lenght can be 200.")
    private String sprintDescription;

    @NotBlank
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern="dd/MMM/yyyy")
    private Date sprintStartDate;

    @NotBlank
    @Column(nullable = false)
    @Temporal(TemporalType.DATE)
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
        this.sprintStartDate = Sprint.stringToDate(sprintStartDate);
        this.sprintEndDate = Sprint.stringToDate(sprintEndDate);
    }

    @Override
    public String toString() {
        return String.format(
                "Sprint[id=%d, parentProjectId='%d', sprintName='%s', sprintLabel='%s', sprintStartDate='%s', sprintEndDate='%s', sprintDescription='%s']",
                id, parentProjectId, sprintName, sprintLabel, sprintStartDate, sprintEndDate, sprintDescription);
    }

    /**
     * Gets the date form of the given date string
     *
     * @param dateString the string to read as a date in format 01/Jan/2000
     * @return the given date, as a date object
     */
    static Date stringToDate(String dateString) {
        Date date = null;
        try {
            date = new SimpleDateFormat("dd/MMM/yyyy").parse(dateString);
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
        }
        return date;
    }

    /**
     * Gets the string form of the given date in
     *
     * @param date the date to convert
     * @return the given date, as a string in format 01/Jan/2000
     */
    static String dateToString(Date date) {
        return new SimpleDateFormat("dd/MMM/yyyy").format(date);
    }

    /* Getters and Setters */

    public int getId(){
        return  id;
    }

    public int getParentProjectId() {
        return parentProjectId;
    }

    public void setSprintName(String sprintName) { this.sprintName = sprintName; }

    public String getName() {
        return sprintName;
    }

    public String getLabel() {
        return sprintLabel + String.valueOf(sprintNum+1);
    }

    public int getSprintNum() { return sprintNum; }

    public void setSprintDescription(String sprintDescription) { this.sprintDescription = sprintDescription; }

    public String getDescription(){
        return sprintDescription;
    }


    public Date getStartDate() {
        return sprintStartDate;
    }

    public String getStartDateString() {
        return Project.dateToString(this.sprintStartDate);
    }

    public int addSprintNum() { return sprintNum++; }

    public void setStartDate(Date newStartDate) { this.sprintStartDate = newStartDate; }

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
