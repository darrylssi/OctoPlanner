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
 * DeadlineForm class that's given to the user inside templates
 */
public class DeadlineForm {

    @NotBlank(message="Deadline name cannot be blank")
    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The deadline name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    private String name;

    @Size(max=MAX_DESC_LENGTH, message="The deadline description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String description;

    // Due to Firefox's lack of <input type="datetime"> support,
    // we store this as two separate fields: Date and Time
    @NotNull(message="Deadline date cannot be blank")
    @DateTimeFormat(pattern=DATE_FORMAT)
    private LocalDate date;

    @NotNull(message="Deadline time cannot be blank")
    @DateTimeFormat(pattern="HH:mm")
    private LocalTime time;

    /**
     * Initializes a DeadlineForm object with the current date and time
     */
    public DeadlineForm () {
        this.date = LocalDate.now();
        this.time = LocalTime.now();
    }

    // https://stackoverflow.com/a/23885950
    /**
     * Creates a Date object from the Date & Time fields
     * @param userTimeZone The timezone ID of where the user is
     * @return A Date object of when the deadline starts
     */
    public Date datetimeToDate(TimeZone userTimeZone) {
        return DateUtils.localDateAndTimeToDate(date, time, userTimeZone);
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

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}
