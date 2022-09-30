package nz.ac.canterbury.seng302.portfolio.controller.forms;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;

/**
 * Group form that is given to the user inside templates.
 */
public class GroupForm {

    @NotBlank(message = "Group short name cannot be empty")
    @Size(min = MIN_NAME_LENGTH, max = MAX_NAME_LENGTH,
            message = "Group short name must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters")
    private String shortName;

    @NotBlank(message = "Group long name cannot be empty")
    @Size(max = MAX_GROUP_LONG_NAME_LENGTH,
            message = "Group long name must not exceed " + MAX_GROUP_LONG_NAME_LENGTH + " characters")
    private String longName;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getLongName() {
        return longName;
    }

    public void setLongName(String longName) {
        this.longName = longName;
    }
}


