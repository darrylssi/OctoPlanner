package nz.ac.canterbury.seng302.portfolio.utils;

/**
 * Contains literal values that will be shared across Portfolio classes.
 */
public class GlobalVars {

    private GlobalVars() {}

    public static final int MAX_DESC_LENGTH = 200; // Maximum number of characters a description field can have

    public static final int MIN_NAME_LENGTH = 2;        // Minimum number of characters a name field can have
    public static final int MAX_NAME_LENGTH = 32;       // Maximum number of characters a name field can have
    public static final int MAX_OTHER_NAME_LENGTH = 20; // Maximum number of characters a middle name, nickname and pronouns can have

    public static final int MIN_PASSWORD_LENGTH = 7;    // Minimum number of characters a password field can have
    public static final int MAX_PASSWORD_LENGTH = 20;   // Maximum number of characters a password field can have

    public static final int MAX_USERNAME_LENGTH = 20;   // Maximum number of characters a username field can have

    public static final int MAX_GROUP_LONG_NAME_LENGTH = 128; // Maximum number of characters a group long name field can have


    public static final int COLOUR_LENGTH = 7;
    public static final String DEFAULT_COLOUR = "#ff3823";  // The default colour code for displaying schedulables outside sprints

    // Used instead of doing an instanceof check for the type of the schedulable
    public static final String EVENT_TYPE = "event";
    public static final String DEADLINE_TYPE = "deadline";
    public static final String MILESTONE_TYPE = "milestone";

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DISPLAY_DATE_FORMAT = "dd/MMM/yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "dd/MMM/yyyy HH:mm:ss";
    public static final String DATETIME_ISO_FORMAT = "yyyy-MM-dd'T'hh:mm";  // Format accepted by browsers

    public static final String DESC_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Z}]*$";
    public static final String NAME_REGEX = "^[\\p{L}\\p{N}\\p{P}\\p{Z}&&[^,]]{2,}$";
    public static final String DESC_ERROR_MESSAGE = "Description can only have letters, numbers, spaces and punctuation";
    public static final String NAME_ERROR_MESSAGE = "Name can only have letters, numbers, spaces and punctuation except for commas";


    public static final int TEACHER_GROUP_ID = 0;
    public static final int MEMBERS_WITHOUT_GROUPS_ID = 1;
    public static final String SHORT_NAME_ERROR_MESSAGE = "Group short name can only have letters, numbers, punctuations " +
            "except commas, and spaces.";
    public static final String LONG_NAME_ERROR_MESSAGE = "Group long name can only have letters, numbers, punctuations, and spaces.";
    public static final String GROUP_NOT_FOUND_ERROR_MESSAGE = "There is no group with id ";


}
