package nz.ac.canterbury.seng302.portfolio.controller.forms;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * DeadlineForm class that's given to the user inside templates
 */
public class DeadlineForm {

    @Size(min=MIN_NAME_LENGTH, max=MAX_NAME_LENGTH,
            message="The deadline name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters.")
    private String name;

    @Size(max=MAX_DESC_LENGTH, message="The deadline description must not exceed " + MAX_DESC_LENGTH + " characters.")
    private String description;

    @DateTimeFormat(pattern=DATE_FORMAT)
    private LocalDate date;

    @DateTimeFormat(pattern="HH:mm")
    private LocalTime time;

    /**
     * Initializes a DeadlineForm object with the current date and time
     */
    public DeadlineForm () {
        this.date = LocalDate.now();
        this.time = LocalTime.now();
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
        this.description = description;
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
