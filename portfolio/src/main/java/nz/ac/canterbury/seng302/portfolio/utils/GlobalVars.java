package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Contains literal values that will be shared across Portfolio classes.
 */
public class GlobalVars {

    private GlobalVars() {}

    public static final int MAX_DESC_LENGTH = 200;

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 32;

    public static final int MIN_PASSWORD_LENGTH = 7;
    public static final int MAX_PASSWORD_LENGTH = 20;

    public static final int MAX_USERNAME_LENGTH = 20;

    public static final int COLOUR_LENGTH = 7;

    public static final String EVENT_TYPE = "event";
    public static final String DEADLINE_TYPE = "deadline";
    public static final String MILESTONE_TYPE = "milestone";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DISPLAY_DATE_FORMAT = "dd/MMM/yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "dd/MMM/yyyy HH:mm:ss";
    public static final String DATETIME_ISO_FORMAT = "yyyy-MM-dd'T'hh:mm";  // Format accepted by browsers

}
