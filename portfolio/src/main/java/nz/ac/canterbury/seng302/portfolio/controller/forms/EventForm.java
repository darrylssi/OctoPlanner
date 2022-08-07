package nz.ac.canterbury.seng302.portfolio.controller.forms;

import java.util.Date;

import javax.validation.constraints.*;

import org.springframework.format.annotation.DateTimeFormat;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * <p>EventForm class that's given to the user inside templates.</p>
 * 
 * This proxy exists for security reasons, as directly giving Event objects from the
 * database flags Sonarqube errors.
 */
public class EventForm {
    @NotNull
    @NotBlank(message="Event name cannot be blank")
    @Size(min=MAX_NAME_LENGTH,
            max=MAX_NAME_LENGTH,
            message="The event name must be between "+MIN_NAME_LENGTH+"-"+MAX_NAME_LENGTH+" characters")
    private String name;

    @Size(max=MAX_DESC_LENGTH,
            message="The event description must not exceed "+MAX_DESC_LENGTH+" characters")
    private String description;

    @NotNull
    @DateTimeFormat(pattern=DATETIME_ISO_FORMAT)
    private Date startTime;

    @NotNull
    @DateTimeFormat(pattern=DATETIME_ISO_FORMAT)
    private Date endTime;

    
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

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

}
