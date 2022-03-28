package nz.ac.canterbury.seng302.portfolio.model;

import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Used by Project and Pprint to convert dates to yyyy-MM-dd format and vice versa
 * that is required by the date type input field.
 */
@Component
public class DateUtils {

    /**
     * Converts a Date type object to String with yyyy-MM-dd format.
     * @param date Date to be converted
     * @return Date as a String
     */
    public String toString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    /**
     * Converts a String in yyyy-MM-dd format to a Date object.
     * @param date String to be converted to Date
     * @return Date object
     * @throws ParseException If the String date is of unexpected format
     */
    public Date toDate(String date) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
}
