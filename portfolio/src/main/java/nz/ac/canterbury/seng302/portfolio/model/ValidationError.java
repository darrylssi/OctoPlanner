package nz.ac.canterbury.seng302.portfolio.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A POJO for handling validation errors.
 * Contains a boolean flagging whether there is an error or not, and a list of string error messages.
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

    /**
     * Adds a string error message to the list of error messages.
     * @param errorMessage an error message as a string
     */
    public void addErrorMessage(String errorMessage) {
        this.errorMessages.add(errorMessage);
    }

    /**
     * Returns the first error message in the list of such messages.
     * @return first error message if errorFlag is true, else an empty string ("")
     */
    public String getFirstError() {
        if ( errorFlag ) {
            return errorMessages.get(0);
        } else {
            return "";
        }
    }
}
