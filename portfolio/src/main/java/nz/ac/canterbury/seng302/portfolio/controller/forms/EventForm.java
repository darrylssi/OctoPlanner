package nz.ac.canterbury.seng302.portfolio.controller.forms;

import java.util.Date;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

public class EventForm {
    @NotNull
    @Size(min = 2, max = 32, message = "The event name must be between 2-32 characters")
    private String name;

    @NotNull
    @Size(max = 200, message = "The event description must not exceed 200 characters")
    private String description;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'hh:mm")
    private Date startTime;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'hh:mm")
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
