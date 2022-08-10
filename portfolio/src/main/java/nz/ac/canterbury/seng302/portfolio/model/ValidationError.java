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

    public ValidationError() {
        this.errorFlag = false;
        this.errorMessages = new ArrayList<>();
    }
    /**
     * Checks if there are any errors
     * @return `true` if an error was added (without being manually set to false)
     */
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
     * <p>Adds an error message to the list of error messages, and sets the errorFlag to `true`</p>
     * @param errorMessage an error message as a string
     */
    public void addErrorMessage(String errorMessage) {
        this.errorFlag = true;
        this.errorMessages.add(errorMessage);
    }

    /**
     * Returns the first error message in the list of such messages.
     * @return first error message if errorFlag is true, else an empty string ("")
     */
    public String getFirstError() {
        if ( !errorMessages.isEmpty() && errorFlag ) {
            return errorMessages.get(0);
        } else {
            return "";
        }
    }
}
