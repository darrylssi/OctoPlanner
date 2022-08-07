package nz.ac.canterbury.seng302.portfolio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static nz.ac.canterbury.seng302.portfolio.utils.GlobalVars.*;


/**
 * Used by Project and Sprint to convert dates to yyyy-MM-dd format and vice versa
 * that is required by the date type input field.
 */
@Component
public class DateUtils {

    private static final SimpleDateFormat backendDateFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
    private static final SimpleDateFormat displayDateTimeFormat = new SimpleDateFormat(DATETIME_FORMAT);

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    // Using a singleton pattern, because SonarLint doesn't like it when a static
    // utility class get initialized, but `DatesExpressionDialect` NEEDS an instance.
    private static DateUtils singleton = null;

    public static DateUtils getInstance() {
        if (singleton == null)
            singleton = new DateUtils();
        return singleton;
    }

    private DateUtils() {}
    /**
     * Converts a Date object to a String with dd/MMM/yyyy format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public static String toDisplayString(Date date) {
        return displayDateFormat.format(date);
    }

    /**
     * Converts a Date object to a String with dd/MMM/yyyy HH:mm:ss format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public static String toDisplayDateTimeString(Date date) {
        return displayDateTimeFormat.format(date);
    }

    /**
     * Converts a String to a Date in yyyy-MM-dd format.
     * @param date String to be converted to Date
     * @return Date object
     */
    public static Date toDate(String date) {
        try {
            return backendDateFormat.parse(date);
        } catch (ParseException e) {
            logger.error(String.format("Error parsing date: %s", e.getMessage()));
        }
        return null;
    }

    /**
     * Converts a Date object to a String with yyyy-MM-dd format.
     * @param date String to be converted to Date
     * @return Date object
     */
    public static String toString(Date date) {
        return backendDateFormat.format(date);
    }

    /**
     * Checks whether the first date given is the day after the second date given
     */
    public static boolean isDayAfter(Date dayAfter, Date dayBefore) {
        Calendar c = Calendar.getInstance();
        c.setTime(dayBefore);
        c.add(Calendar.DATE, 1);    // Add n days to the date
        return c.getTime().equals(dayAfter);
    }
}
