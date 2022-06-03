package nz.ac.canterbury.seng302.portfolio.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Used by Project and Sprint to convert dates to yyyy-MM-dd format and vice versa
 * that is required by the date type input field.
 */
@Component
public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final SimpleDateFormat backendDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MMM/yyyy");

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    /**
     * Converts a Date object to a String with dd/MMM/yyyy format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public static String toDisplayString(Date date) {
        return displayDateFormat.format(date);
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

}
