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

    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);
    /**
     * Converts a Date type object to String with yyyy-MM-dd format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public String toString(Date date) {
        return new SimpleDateFormat("dd/MMM/yyyy").format(date);
    }

    /**
     * Converts a String in yyyy-MM-dd format to a Date object.
     * @param date String to be converted to Date
     * @return Date object
     */
    public Date toDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            logger.error(String.format("Error parsing date: %s", e.getMessage()));
        }
        return null;
    }
}
