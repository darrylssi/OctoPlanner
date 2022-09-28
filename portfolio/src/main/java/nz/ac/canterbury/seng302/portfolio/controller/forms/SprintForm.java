package nz.ac.canterbury.seng302.portfolio.controller.forms;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * <p>SprintForm class that's given to the user inside templates.</p>
 *
 * This proxy exists for security reasons, as directly giving Sprint objects from the
 * database flags Sonarqube errors.
 */
public class SprintForm {
    @NotBlank(message="Name cannot be blank")
    @Size(min=MIN_NAME_LENGTH,
            max=MAX_NAME_LENGTH,
            message="Name must be between "+MIN_NAME_LENGTH+"-"+MAX_NAME_LENGTH+" characters")
    private String name;

    @Size(max=MAX_DESC_LENGTH,
            message="Description must not exceed "+MAX_DESC_LENGTH+" characters")
    private String description;

    @NotNull(message="Start date cannot be blank")
    @DateTimeFormat(pattern=DATE_FORMAT)
    private LocalDate startDate;

    @NotNull(message="End date cannot be blank")
    @DateTimeFormat(pattern=DATE_FORMAT)
    private LocalDate endDate;

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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
