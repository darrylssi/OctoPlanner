package nz.ac.canterbury.seng302.portfolio.controller.forms;

import nz.ac.canterbury.seng302.portfolio.utils.DateUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * <p>SchedulableForm class that's given to the user inside templates.</p>
 * 
 * This proxy exists for security reasons, as directly giving Schedulable objects from the
 * database flags Sonarqube errors.
 */
public class SchedulableForm {
    @NotBlank(message="Schedulable name cannot be blank")
    @Size(min=MIN_NAME_LENGTH,
            max=MAX_NAME_LENGTH,
            message="The schedulable name must be between "+MIN_NAME_LENGTH+"-"+MAX_NAME_LENGTH+" characters")
    private String name;

    @Size(max=MAX_DESC_LENGTH,
            message="The schedulable description must not exceed "+MAX_DESC_LENGTH+" characters")
    private String description;
    
    // Due to Firefox's lack of <input type="datetime"> support,
    // we store this as two separate fields: Date and Time
    @NotNull(message="Schedulable start date cannot be blank")
    @DateTimeFormat(pattern=DATE_FORMAT)
    private LocalDate startDate;

    @NotNull(message="Schedulable start time cannot be blank")
    @DateTimeFormat(pattern="HH:mm")
    private LocalTime startTime;

    @NotNull(message="Schedulable end date cannot be blank")
    @DateTimeFormat(pattern=DATE_FORMAT)
    private LocalDate endDate;

    @NotNull(message="Schedulable end time cannot be blank")
    @DateTimeFormat(pattern="HH:mm")
    private LocalTime endTime;

    // https://stackoverflow.com/a/23885950
    /**
     * Creates a Date object from the startDate & startTime fields
     * @param userTimeZone The timezone ID of where the user is
     * @return A Date object of when the schedulable starts
     */
    public Date startDatetimeToDate(TimeZone userTimeZone) {
        return DateUtils.localDateAndTimeToDate(startDate, startTime, userTimeZone);
    }

    /**
     * Creates a Date object from the endDate & endTime fields
     * @param userTimeZone The timezone ID of where the user is
     * @return A Date object of when the schedulable ends
     */
    public Date endDatetimeToDate(TimeZone userTimeZone) {
        return DateUtils.localDateAndTimeToDate(endDate, endTime, userTimeZone);

    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }



}
