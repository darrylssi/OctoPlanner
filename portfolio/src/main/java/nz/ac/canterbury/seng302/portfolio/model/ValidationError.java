package nz.ac.canterbury.seng302.portfolio.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO for handling validation errors.
 */
public class ValidationError {
    private boolean errorFlag; // True if there is an error, false if not
    private final List<String> errorMessages;

    public ValidationError(Boolean errorFlag) {
        this.errorFlag = errorFlag;
        this.errorMessages = new ArrayList<>();
    }

    public boolean isError() {
        return errorFlag;
    }

    public void setErrorFlag(boolean error) {
        errorFlag = error;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void addErrorMessage(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }

    public String getFirstError() {
        if ( errorFlag ) {
            return errorMessages.get(0);
        } else {
            return "";
        }
    }
}
